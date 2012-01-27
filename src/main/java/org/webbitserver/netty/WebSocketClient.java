package org.webbitserver.netty;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;
import org.webbitserver.helpers.Base64;
import org.webbitserver.netty.CatchingRunnable;
import org.webbitserver.netty.HybiWebSocketFrameDecoder;
import org.webbitserver.netty.HybiWebSocketFrameEncoder;
import org.webbitserver.netty.NettyWebSocketConnection;
import org.webbitserver.netty.WebSocketConnectionHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class WebSocketClient {
    private static final String ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final MessageDigest SHA_1;

    static {
        try {
            SHA_1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("SHA-1 not supported on this platform");
        }
    }

    private final URI uri;
    private final WebSocketHandler webSocketHandler;
    private final Executor executor;
    private final InetSocketAddress remoteAddress;
    private final String host;

    private ClientBootstrap bootstrap;
    private Channel channel;
    private String base64Nonce;

    public WebSocketClient(URI uri, WebSocketHandler webSocketHandler, Executor executor) {
        this.uri = uri;
        this.webSocketHandler = webSocketHandler;
        this.executor = executor;
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        host = uri.getHost() == null ? "localhost" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if (scheme.equalsIgnoreCase("ws")) {
                port = 80;
            }
        }

        if (!scheme.equalsIgnoreCase("ws")) {
            throw new IllegalArgumentException("Only ws(s) is supported.");
        }
        remoteAddress = new InetSocketAddress(host, port);
    }

    public void start() {
        final byte[] outboundMaskingKey = new byte[]{randomByte(), randomByte(), randomByte(), randomByte()};

        bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("decoder", new HttpResponseDecoder());
                pipeline.addLast("encoder", new HttpRequestEncoder());
                pipeline.addLast("inflater", new HttpContentDecompressor());
                pipeline.addLast("handshakeHandler", new HandshakeChannelHandler(outboundMaskingKey));
                return pipeline;
            }
        });
        ChannelFuture future = bootstrap.connect(remoteAddress);
        channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            close();
            throw new WebbitException(future.getCause());
        }

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString().replaceFirst("http", "ws"));
        request.setHeader(HttpHeaders.Names.HOST, host);
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        request.setHeader("Sec-WebSocket-Version", 13);

        base64Nonce = base64Nonce();
        request.setHeader("Sec-WebSocket-Key", base64Nonce);

        channel.write(request).awaitUninterruptibly();
    }

    private String base64Nonce() {
        byte[] nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = randomByte();
        }
        return Base64.encode(nonce);
    }

    private byte randomByte() {
        return (byte) (Math.random() * 256);
    }

    public void close() {
        channel.getCloseFuture().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }

    private class HandshakeChannelHandler extends SimpleChannelUpstreamHandler {
        private final byte[] outboundMaskingKey;

        public HandshakeChannelHandler(byte[] outboundMaskingKey) {
            this.outboundMaskingKey = outboundMaskingKey;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            HttpResponse response = (HttpResponse) e.getMessage();
            String webSocketAccept = response.getHeader("Sec-WebSocket-Accept");
            verifySecWebSocketAccept(webSocketAccept);
            adjustPipelineToWebSocket(ctx, HybiWebSocketFrameDecoder.clientSide(outboundMaskingKey), new HybiWebSocketFrameEncoder());
        }

        private void verifySecWebSocketAccept(String webSocketAccept) {
            if (webSocketAccept != null) {
                SHA_1.reset();
                SHA_1.update(base64Nonce.getBytes());
                SHA_1.update(ACCEPT_GUID.getBytes());
                String expectedKey = Base64.encode(SHA_1.digest());
                if (!webSocketAccept.equals(expectedKey)) {
                    throw new WebbitException("Sec-WebSocket-Accept header from server didn't match expected value of " + expectedKey);
                }
            } else {
                throw new WebbitException("Expected Sec-WebSocket-Accept header from server");
            }
        }

        private void adjustPipelineToWebSocket(ChannelHandlerContext ctx, ChannelHandler webSocketFrameDecoder, ChannelHandler webSocketFrameEncoder) {
            final NettyWebSocketConnection webSocketConnection = new NettyWebSocketConnection(executor, null, ctx, outboundMaskingKey);
            webSocketConnection.setHybiWebSocketVersion(13);

            // TODO: Allow users to specify these.
            // TODO: Document the difference between these 2 handlers - I'm not sure I get it myself (AH)
            final Thread.UncaughtExceptionHandler exceptionHandler = new PrintStackTraceExceptionHandler();
            final Thread.UncaughtExceptionHandler ioExceptionHandler = new PrintStackTraceExceptionHandler();

            ChannelHandler webSocketChannelHandler = new WebSocketConnectionHandler(webSocketConnection, exceptionHandler, ioExceptionHandler, webSocketHandler, executor);

            ChannelPipeline p = ctx.getChannel().getPipeline();
            p.remove("inflater");
            p.replace("decoder", "wsdecoder", webSocketFrameDecoder);
            p.replace("encoder", "wsencoder", webSocketFrameEncoder);
            p.replace("handshakeHandler", "wshandler", webSocketChannelHandler);

            executor.execute(new CatchingRunnable(exceptionHandler) {
                @Override
                public void go() throws Exception {
                    webSocketHandler.onOpen(webSocketConnection);
                }
            });
        }
    }

}
