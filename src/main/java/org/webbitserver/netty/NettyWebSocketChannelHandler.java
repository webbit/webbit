package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.helpers.Base64;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY1;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY2;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

public class NettyWebSocketChannelHandler extends SimpleChannelUpstreamHandler {
    private static final MessageDigest SHA_1;
    static {
        try {
            SHA_1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("SHA-1 not supported on this platform");
        }
    } 
    private static final Charset ASCII = Charset.forName("ASCII");
    protected final Executor executor;
    protected final NettyHttpRequest nettyHttpRequest;
    protected final NettyWebSocketConnection webSocketConnection;
    protected final Thread.UncaughtExceptionHandler exceptionHandler;
    protected final Thread.UncaughtExceptionHandler ioExceptionHandler;
    protected final WebSocketHandler handler;
    private static final String ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public NettyWebSocketChannelHandler(
            Executor executor,
            WebSocketHandler handler,
            ChannelHandlerContext ctx,
            UncaughtExceptionHandler exceptionHandler,
            NettyHttpRequest nettyHttpRequest,
            UncaughtExceptionHandler ioExceptionHandler,
            NettyWebSocketConnection webSocketConnection,
            HttpRequest req,
            HttpResponse res
    ) {
        this.handler = handler;
        this.exceptionHandler = exceptionHandler;
        this.nettyHttpRequest = nettyHttpRequest;
        this.executor = executor;
        this.ioExceptionHandler = ioExceptionHandler;
        this.webSocketConnection = webSocketConnection;

        prepareConnection(req, res, ctx);

        try {
            handler.onOpen(this.webSocketConnection);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    protected void prepareConnection(HttpRequest req, HttpResponse res, ChannelHandlerContext ctx) {
        if (isHybi10WebSocketRequest(req)) {
            this.webSocketConnection.setVersion(WebSocketConnection.Version.HYBI_10);
            upgradeResponseHybi10(req, res);
            ctx.getChannel().write(res);
            adjustPipelineToHybi(ctx);
        } else if (isHixie76WebSocketRequest(req)) {
            this.webSocketConnection.setVersion(WebSocketConnection.Version.HIXIE_76);
            upgradeResponseHixie76(req, res);
            ctx.getChannel().write(res);
            adjustPipelineToHixie(ctx);
        } else {
            this.webSocketConnection.setVersion(WebSocketConnection.Version.HIXIE_75);
            upgradeResponseHixie75(req, res);
            ctx.getChannel().write(res);
            adjustPipelineToHixie(ctx);
        }
    }

    protected void adjustPipelineToHixie(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getChannel().getPipeline();
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", new WebSocketFrameDecoder());
        p.replace("handler", "wshandler", this);
        p.replace("encoder", "wsencoder", new WebSocketFrameEncoder());
    }

    protected void adjustPipelineToHybi(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getChannel().getPipeline();
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", new Hybi10WebSocketFrameDecoder());
        p.replace("handler", "wshandler", this);
        p.replace("encoder", "wsencoder", new Hybi10WebSocketFrameEncoder());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.onClose(webSocketConnection);
                } catch (Exception e1) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), e1);
                }
            }
        });
    }

    @Override
    public String toString() {
        return nettyHttpRequest.toString();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ClosedChannelException) {
            e.getChannel().close();
        } else {
            final Thread thread = Thread.currentThread();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ioExceptionHandler.uncaughtException(thread, e.getCause());
                }
            });
        }
    }

    private boolean isHybi10WebSocketRequest(HttpRequest req) {
        return req.containsHeader("Sec-WebSocket-Version");
    }

    private boolean isHixie76WebSocketRequest(HttpRequest req) {
        return req.containsHeader(SEC_WEBSOCKET_KEY1) && req.containsHeader(SEC_WEBSOCKET_KEY2);
    }

    private void upgradeResponseHybi10(HttpRequest req, HttpResponse res) {
        String version = req.getHeader("Sec-WebSocket-Version");
        if(!"8".equals(version)) {
            res.setStatus(HttpResponseStatus.UPGRADE_REQUIRED);
            res.setHeader("Sec-WebSocket-Version", "8");
            return;
        }

        String key = req.getHeader("Sec-WebSocket-Key");
        if(key == null) {
            res.setStatus(HttpResponseStatus.BAD_REQUEST);
            return;
        }

        String accept = Base64.encode(sha1(key + ACCEPT_GUID));

        res.setStatus(new HttpResponseStatus(101, "Switching Protocols"));
        res.addHeader(UPGRADE, WEBSOCKET.toLowerCase());
        res.addHeader(CONNECTION, UPGRADE);
        res.addHeader("Sec-WebSocket-Accept", accept);
    }

    private byte[] sha1(String s) {
        return SHA_1.digest(s.getBytes(ASCII));
    }

    private void upgradeResponseHixie76(HttpRequest req, HttpResponse res) {
        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.addHeader(UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, UPGRADE);
        res.addHeader(SEC_WEBSOCKET_ORIGIN, req.getHeader(ORIGIN));
        res.addHeader(SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
        String protocol = req.getHeader(SEC_WEBSOCKET_PROTOCOL);
        if (protocol != null) {
            res.addHeader(SEC_WEBSOCKET_PROTOCOL, protocol);
        }

        // Calculate the answer of the challenge.
        String key1 = req.getHeader(SEC_WEBSOCKET_KEY1);
        String key2 = req.getHeader(SEC_WEBSOCKET_KEY2);
        int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
        int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
        long c = req.getContent().readLong();
        ChannelBuffer input = ChannelBuffers.buffer(16);
        input.writeInt(a);
        input.writeInt(b);
        input.writeLong(c);
        try {
            ChannelBuffer output = ChannelBuffers.wrappedBuffer(
                    MessageDigest.getInstance("MD5").digest(input.array()));
            res.setContent(output);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void upgradeResponseHixie75(HttpRequest req, HttpResponse res) {
        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.addHeader(UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, HttpHeaders.Values.UPGRADE);
        res.addHeader(WEBSOCKET_ORIGIN, req.getHeader(ORIGIN));
        res.addHeader(WEBSOCKET_LOCATION, getWebSocketLocation(req));
        String protocol = req.getHeader(WEBSOCKET_PROTOCOL);
        if (protocol != null) {
            res.addHeader(WEBSOCKET_PROTOCOL, protocol);
        }
    }

    private String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + req.getUri();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(e.getMessage() instanceof Pong) {
                        handler.onPong(webSocketConnection, ((WebSocketFrame) e.getMessage()).getTextData());
                    } else {
                        WebSocketFrame frame = (WebSocketFrame) e.getMessage();
                        if(frame.isText()) {
                            handler.onMessage(webSocketConnection, frame.getTextData());
                        } else {
                            handler.onMessage(webSocketConnection, frame.getBinaryData().array());
                        }
                    }
                } catch (Throwable t) {
                    // TODO
                    t.printStackTrace();
                }
            }
        });
    }
}
