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
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;
import org.webbitserver.helpers.Base64;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
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
    private static final int MIN_HYBI_VERSION = 8;

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
            exceptionHandler.uncaughtException(Thread.currentThread(), new WebbitException(e));
        }
    }

    protected void prepareConnection(HttpRequest req, HttpResponse res, ChannelHandlerContext ctx) {
        Integer hybiVersion = getHybiVersion(req);
        if (hybiVersion != null) {
            // Instead of indicating what hybi-x spec version x, we indicate the version header number,
            // which confusingly is different. At the time of this writing it's between 8 and 13.
            this.webSocketConnection.setHybiWebSocketVersion(hybiVersion);
            upgradeResponseHybi(req, res, hybiVersion);
            ctx.getChannel().write(res);
            adjustPipelineToWebSocket(ctx, HybiWebSocketFrameDecoder.serverSide(), new HybiWebSocketFrameEncoder());
        } else if (isHixie76WebSocketRequest(req)) {
            this.webSocketConnection.setVersion("HIXIE-76");
            upgradeResponseHixie76(req, res);
            ctx.getChannel().write(res);
            adjustPipelineToWebSocket(ctx, new WebSocketFrameDecoder(), new WebSocketFrameEncoder());
        } else {
            this.webSocketConnection.setVersion("HIXIE-75");
            upgradeResponseHixie75(req, res);
            ctx.getChannel().write(res);
            adjustPipelineToWebSocket(ctx, new WebSocketFrameDecoder(), new WebSocketFrameEncoder());
        }
    }

    private void adjustPipelineToWebSocket(ChannelHandlerContext ctx, ChannelHandler webSocketFrameDecoder, ChannelHandler webSocketFrameEncoder) {
        ChannelPipeline p = ctx.getChannel().getPipeline();
        StaleConnectionTrackingHandler staleConnectionTracker = (StaleConnectionTrackingHandler) p.remove("staleconnectiontracker");
        staleConnectionTracker.stopTracking(ctx.getChannel());
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", webSocketFrameDecoder);
        p.replace("handler", "wshandler", this);
        p.replace("encoder", "wsencoder", webSocketFrameEncoder);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Thread thread = Thread.currentThread();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.onClose(webSocketConnection);
                } catch (Exception e1) {
                    exceptionHandler.uncaughtException(thread, WebbitException.fromException(e1, e.getChannel()));
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
                    ioExceptionHandler.uncaughtException(thread, WebbitException.fromExceptionEvent(e));
                }
            });
        }
    }

    private Integer getHybiVersion(HttpRequest req) {
        return req.containsHeader("Sec-WebSocket-Version") ? Integer.parseInt(req.getHeader("Sec-WebSocket-Version").trim()) : null;
    }

    private boolean isHixie76WebSocketRequest(HttpRequest req) {
        return req.containsHeader(SEC_WEBSOCKET_KEY1) && req.containsHeader(SEC_WEBSOCKET_KEY2);
    }

    private void upgradeResponseHybi(HttpRequest req, HttpResponse res, int version) {
        if (version < MIN_HYBI_VERSION) {
            res.setStatus(HttpResponseStatus.UPGRADE_REQUIRED);
            res.setHeader("Sec-WebSocket-Version", String.valueOf(MIN_HYBI_VERSION));
            return;
        }

        String key = req.getHeader("Sec-WebSocket-Key");
        if (key == null) {
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
            throw new WebbitException(e);
        }
    }

    private void upgradeResponseHixie75(HttpRequest req, HttpResponse res) {
        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.addHeader(UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, HttpHeaders.Values.UPGRADE);
        String origin = req.getHeader(ORIGIN);
        if(origin != null) {
            res.addHeader(WEBSOCKET_ORIGIN, origin);
        }
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
        Object message = e.getMessage();
        if (message instanceof DecodingHybiFrame) {
            DecodingHybiFrame frame = (DecodingHybiFrame) message;
            frame.dispatchMessage(handler, webSocketConnection, executor, exceptionHandler);
        } else {
            // Hixie 75/76
            final WebSocketFrame frame = (WebSocketFrame) message;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        handler.onMessage(webSocketConnection, frame.getTextData());
                    } catch (Throwable throwable) {
                        exceptionHandler.uncaughtException(Thread.currentThread(), WebbitException.fromException(throwable, e.getChannel()));
                    }
                }
            });
        }
    }
}
