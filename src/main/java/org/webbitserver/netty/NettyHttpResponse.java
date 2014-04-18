package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.util.CharsetUtil;
import org.webbitserver.WebbitException;
import org.webbitserver.helpers.DateHelper;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

public class NettyHttpResponse implements org.webbitserver.HttpResponse {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private final ChannelHandlerContext ctx;
    private final HttpResponse response;
    private final boolean isKeepAlive;
    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final ChannelBuffer responseBuffer;
    private Charset charset;

    public NettyHttpResponse(ChannelHandlerContext ctx,
                             HttpResponse response,
                             boolean isKeepAlive,
                             Thread.UncaughtExceptionHandler exceptionHandler) {
        this.ctx = ctx;
        this.response = response;
        this.isKeepAlive = isKeepAlive;
        this.exceptionHandler = exceptionHandler;
        this.charset = DEFAULT_CHARSET;
        responseBuffer = ChannelBuffers.dynamicBuffer();
    }

    @Override
    public NettyHttpResponse charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public Charset charset() {
        return charset;
    }

    @Override
    public NettyHttpResponse status(int status) {
        response.setStatus(HttpResponseStatus.valueOf(status));
        return this;
    }

    @Override
    public NettyHttpResponse chunked() {
        response.setHeader(Names.TRANSFER_ENCODING, Values.CHUNKED);
        response.setChunked(true);
        ctx.getChannel().write(response);
        return this;
    }

    @Override
    public int status() {
        return response.getStatus().getCode();
    }

    @Override
    public NettyHttpResponse header(String name, String value) {
        if (value == null) {
            response.removeHeader(name);
        } else {
            response.addHeader(name, value);
        }
        return this;
    }

    @Override
    public NettyHttpResponse header(String name, long value) {
        response.addHeader(name, value);
        return this;
    }

    @Override
    public NettyHttpResponse header(String name, Date value) {
        response.addHeader(name, DateHelper.rfc1123Format(value));
        return this;
    }

    @Override
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    @Override
    public NettyHttpResponse cookie(HttpCookie httpCookie) {
        Cookie nettyCookie = new DefaultCookie(httpCookie.getName(),httpCookie.getValue());
        nettyCookie.setDomain(httpCookie.getDomain());
        nettyCookie.setPath(httpCookie.getPath());
        nettyCookie.setSecure(httpCookie.getSecure());
        nettyCookie.setMaxAge((int)httpCookie.getMaxAge());
        nettyCookie.setVersion(httpCookie.getVersion());
        nettyCookie.setDiscard(httpCookie.getDiscard());
        CookieEncoder encoder = new CookieEncoder(true);
        encoder.addCookie(nettyCookie);

        // IE compat.
        String c = encoder.encode();
        c = c + "; Expires="
          + DateHelper.rfc850Format(new Date(System.currentTimeMillis() + httpCookie.getMaxAge() * 1000));

        return header(HttpHeaders.Names.SET_COOKIE, c);
    }

    @Override
    public NettyHttpResponse content(String content) {
        return content(copiedBuffer(content, charset()));
    }

    @Override
    public NettyHttpResponse content(byte[] content) {
        return content(copiedBuffer(content));
    }

    @Override
    public NettyHttpResponse content(ByteBuffer buffer) {
        return content(wrappedBuffer(buffer));
    }

    private NettyHttpResponse content(ChannelBuffer content) {
        if (response.isChunked()) {
            throw new UnsupportedOperationException();
        }
        responseBuffer.writeBytes(content);
        return this;
    }

    @Override
    public long contentLength() {
        return responseBuffer.array().length;
    }

    @Override
    public NettyHttpResponse write(String content) {
        if (response.isChunked()) {
            ctx.getChannel().write(new DefaultHttpChunk(wrappedBuffer(content.getBytes(CharsetUtil.UTF_8))));
        } else {
            write(copiedBuffer(content, CharsetUtil.UTF_8));
        }
        return this;
    }

    @Override
    public NettyHttpResponse error(Throwable error) {
        if (error instanceof TooLongFrameException) {
            response.setStatus(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
        } else {
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
//        String message = getStackTrace(error);
        header("Content-Type", "text/plain");
//        content(message);
				content("Internal server error. We are notified about the problem and will fix it. Sorry for any inconvenience.");
        try{
            flushResponse();
        }catch (IllegalStateException e){
            return null;
        }catch (WebbitException e){
            return null;
        }
        exceptionHandler.uncaughtException(Thread.currentThread(),
                WebbitException.fromException(error, ctx.getChannel()));

        return this;
    }

    private String getStackTrace(Throwable error) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        error.printStackTrace(writer);
        writer.flush();
        return buffer.toString();
    }

    @Override
    public NettyHttpResponse end() {
        flushResponse();
        return this;
    }

    private void flushResponse() {
        try {

					if (!ctx.getChannel().isOpen()) {
						System.err.println("channel is closed, channel: " + ctx.getChannel());
						ctx.getChannel().disconnect();
						ctx.getChannel().close();

						return;
					}

					// mymod. WebbitException: cannot send more responses than requests
					if (!ctx.getChannel().isWritable()) return;

					// TODO: Shouldn't have to do this, but without it we sometimes seem to get two Content-Length headers in the response.
					header("Content-Length", (String) null);
					header("Content-Length", responseBuffer.readableBytes());
            ChannelFuture  future = response.isChunked() ? ctx.getChannel().write(new DefaultHttpChunk(ChannelBuffers.EMPTY_BUFFER)) : write(responseBuffer);
            if (!isKeepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            exceptionHandler.uncaughtException(Thread.currentThread(),
                    WebbitException.fromException(e, ctx.getChannel()));
        }
    }

    private ChannelFuture write(ChannelBuffer responseBuffer) {
        response.setContent(responseBuffer);
        return ctx.getChannel().write(response);
    }

}
