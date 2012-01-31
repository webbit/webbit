package org.webbitserver.netty;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
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
import org.jboss.netty.handler.ssl.SslHandler;
import org.webbitserver.WebSocket;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;
import org.webbitserver.handler.ReconnectingWebSocketHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;
import org.webbitserver.handler.exceptions.SilentExceptionHandler;
import org.webbitserver.helpers.Base64;
import org.webbitserver.helpers.SslFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class WebSocketClient implements WebSocket {
    private static final String ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final MessageDigest SHA_1;

    static {
        try {
            SHA_1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("SHA-1 not supported on this platform");
        }
    }

    private static long nextId = 1;

    private WebSocketHandler webSocketHandler;
    private final Executor executor;
    private final InetSocketAddress remoteAddress;
    private final HttpRequest request;
    private final boolean ssl;

    private ClientBootstrap bootstrap;
    private Channel channel;
    private String base64Nonce;
    private Thread.UncaughtExceptionHandler exceptionHandler;
    private Thread.UncaughtExceptionHandler ioExceptionHandler;
    private SslFactory sslFactory;

    public WebSocketClient(URI uri, WebSocketHandler webSocketHandler) {
        this(uri, webSocketHandler, Executors.newSingleThreadExecutor());
    }

    public WebSocketClient(URI uri, WebSocketHandler webSocketHandler, Executor executor) {
        this.webSocketHandler = webSocketHandler;
        this.executor = executor;

        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        String host = uri.getHost() == null ? "localhost" : uri.getHost();
        int port = uri.getPort();
        ssl = scheme.equalsIgnoreCase("wss");
        if (port == -1) {
            if (scheme.equalsIgnoreCase("ws")) {
                port = 80;
            } else if (ssl) {
                port = 443;
            }
        }
        remoteAddress = new InetSocketAddress(host, port);
        request = createNettyHttpRequest(uri.toASCIIString().replaceFirst("http", "ws"), host);

        uncaughtExceptionHandler(new PrintStackTraceExceptionHandler());
        connectionExceptionHandler(new SilentExceptionHandler());
    }

    /**
     * What to do when an exception gets thrown in a handler.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler}.
     * It is suggested that apps supply their own implementation (e.g. to log somewhere).
     */
    WebSocket uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        this.exceptionHandler = handler;
        return this;
    }

    /**
     * What to do when an exception occurs when attempting to read/write data
     * from/to the underlying connection. e.g. If an HTTP request disconnects
     * before it was expected.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.SilentExceptionHandler}
     * as this is a common thing to happen on a network, and most systems should not care.
     */
    WebSocket connectionExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        this.ioExceptionHandler = handler;
        return this;
    }

    public WebSocketClient setupSsl(InputStream keyStore, String pass) {
        sslFactory = new SslFactory(keyStore, pass);
        return this;
    }

    @Override
    public WebSocketClient start() {
        final byte[] outboundMaskingKey = new byte[]{randomByte(), randomByte(), randomByte(), randomByte()};

        bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                if (ssl) {
                    if (sslFactory == null) {
                        throw new WebbitException("You need to call setupSsl first");
                    }
                    SSLContext sslContext = sslFactory.getClientContext();
                    SSLEngine sslEngine = sslContext.createSSLEngine();
                    sslEngine.setUseClientMode(true);
                    pipeline.addLast("ssl", new SslHandler(sslEngine));
                }
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
        } else {
            channel.write(request).awaitUninterruptibly();
        }
        return this;
    }

    private HttpRequest createNettyHttpRequest(String uri, String host) {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        request.setHeader(HttpHeaders.Names.HOST, host);
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        request.setHeader(Hybi.SEC_WEBSOCKET_VERSION, 13);

        base64Nonce = base64Nonce();
        request.setHeader(Hybi.SEC_WEBSOCKET_KEY, base64Nonce);
        return request;
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

    @Override
    public WebSocket close() {
        channel.getCloseFuture().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocketHandler.onClose(null);
                } catch (Exception e) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), WebbitException.fromException(e, channel));
                }
            }
        });
        return this;
    }

    @Override
    public WebSocketClient reconnectEvery(long reconnectIntervalMillis) {
        webSocketHandler = new ReconnectingWebSocketHandler(webSocketHandler, WebSocketClient.this, reconnectIntervalMillis);
        return this;
    }

    private class HandshakeChannelHandler extends SimpleChannelUpstreamHandler {
        private final byte[] outboundMaskingKey;

        public HandshakeChannelHandler(byte[] outboundMaskingKey) {
            this.outboundMaskingKey = outboundMaskingKey;
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

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            HttpResponse response = (HttpResponse) e.getMessage();
            String webSocketAccept = response.getHeader(Hybi.SEC_WEBSOCKET_ACCEPT);
            verifySecWebSocketAccept(webSocketAccept);
            adjustPipelineToWebSocket(ctx, e, HybiWebSocketFrameDecoder.clientSide(outboundMaskingKey), new HybiWebSocketFrameEncoder());
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

        private void adjustPipelineToWebSocket(ChannelHandlerContext ctx, MessageEvent messageEvent, ChannelHandler webSocketFrameDecoder, ChannelHandler webSocketFrameEncoder) {
            NettyHttpRequest httpRequest = new NettyHttpRequest(messageEvent, request, nextId(), timestamp());
            final NettyWebSocketConnection webSocketConnection = new NettyWebSocketConnection(executor, httpRequest, ctx, outboundMaskingKey);
            webSocketConnection.setHybiWebSocketVersion(17);

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

        private long timestamp() {
            return System.currentTimeMillis();
        }
    }

    private static Object nextId() {
        return nextId++;
    }
}
