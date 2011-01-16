package webbit.netty;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import webbit.WebSocketHandler;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

public class NettyHttpResponse implements webbit.HttpResponse {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private final ChannelHandlerContext ctx;
    private final NettyHttpRequest nettyHttpRequest;
    private final HttpRequest request;
    private final HttpResponse response;
    private final Executor executor;
    private Charset charset;

    public NettyHttpResponse(Executor executor,
                             ChannelHandlerContext ctx,
                             NettyHttpRequest nettyHttpRequest,
                             HttpRequest request,
                             HttpResponse response) {
        this.executor = executor;
        this.ctx = ctx;
        this.nettyHttpRequest = nettyHttpRequest;
        this.request = request;
        this.response = response;
        this.charset = DEFAULT_CHARSET;
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
    public int status() {
        return response.getStatus().getCode();
    }

    @Override
    public NettyHttpResponse header(String name, String value) {
        response.setHeader(name, value);
        return this;
    }

    @Override
    public NettyHttpResponse header(String name, int value) {
        response.setHeader(name, value);
        return this;
    }

    @Override
    public NettyHttpResponse content(String content) {
        response.setContent(copiedBuffer(content, charset()));
        return this;
    }

    @Override
    public NettyHttpResponse content(byte[] content) {
        response.setContent(copiedBuffer(content));
        return this;
    }

    @Override
    public NettyWebSocketConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
         return new NettyWebSocketConnection(executor, ctx, nettyHttpRequest, request, response, handler);
    }

    @Override
    public NettyHttpResponse end() {
        // Generate an error page if response status code is not OK (200).
        if (response.getStatus().getCode() != 200) {
            response.setContent(copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8));
            header("Content-Length", response.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(response);
        if (!isKeepAlive(request) || response.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }

        return this;
    }

}
