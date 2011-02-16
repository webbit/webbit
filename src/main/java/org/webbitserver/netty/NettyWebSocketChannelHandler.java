package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import java.nio.channels.ClosedChannelException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

public class NettyWebSocketChannelHandler extends SimpleChannelUpstreamHandler {
    private final Executor executor;
    private final NettyHttpRequest nettyHttpRequest;
    private final WebSocketHandler handler;
    private final WebSocketConnection webSocketConnection;
    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler ioExceptionHandler;

    public NettyWebSocketChannelHandler(Executor executor,
                                        ChannelHandlerContext ctx,
                                        NettyHttpRequest nettyHttpRequest,
                                        HttpRequest request,
                                        HttpResponse response,
                                        WebSocketHandler handler,
                                        WebSocketConnection webSocketConnection,
                                        Thread.UncaughtExceptionHandler exceptionHandler,
                                        Thread.UncaughtExceptionHandler ioExceptionHandler) {
        this.executor = executor;
        this.nettyHttpRequest = nettyHttpRequest;
        this.handler = handler;
        this.webSocketConnection = webSocketConnection;
        this.exceptionHandler = exceptionHandler;
        this.ioExceptionHandler = ioExceptionHandler;

        if (!requestingWebsocketUpgrade(request)) {
            throw new RuntimeException("Expecting WebSocket upgrade. Looks like a standard HTTP request.");
        }

        // Support both commonly used versions of the WebSocket spec.
        if (isNewSkoolWebSocketRequest(request)) {
            upgradeResponseNewSkool(request, response);
        } else {
            upgradeResponseOldSkool(request, response);
        }

        ChannelPipeline p = ctx.getChannel().getPipeline();
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", new WebSocketFrameDecoder());
        p.replace("handler", "wshandler", this);

        ctx.getChannel().write(response);

        p.replace("encoder", "wsencoder", new WebSocketFrameEncoder());

        try {
            handler.onOpen(this.webSocketConnection);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }

    }

    private boolean requestingWebsocketUpgrade(HttpRequest request) {
        return UPGRADE.equalsIgnoreCase(request.getHeader(CONNECTION)) &&
                WEBSOCKET.equalsIgnoreCase(request.getHeader(UPGRADE));
    }

    private boolean isNewSkoolWebSocketRequest(HttpRequest req) {
        return req.containsHeader(SEC_WEBSOCKET_KEY1) && req.containsHeader(SEC_WEBSOCKET_KEY2);
    }

    private void upgradeResponseNewSkool(HttpRequest req, HttpResponse res) {
        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.addHeader(UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, HttpHeaders.Values.UPGRADE);
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

    private void upgradeResponseOldSkool(HttpRequest req, HttpResponse res) {
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
                    handler.onMessage(webSocketConnection, ((WebSocketFrame) e.getMessage()).getTextData());
                } catch (Exception e1) {
                    // TODO
                    e1.printStackTrace();
                }
            }
        });
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
}
