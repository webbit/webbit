package org.webbitserver.wsclient;

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
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;
import org.webbitserver.netty.CatchingRunnable;
import org.webbitserver.netty.DecodingHybiFrame;
import org.webbitserver.netty.HybiWebSocketFrameDecoder;
import org.webbitserver.netty.HybiWebSocketFrameEncoder;
import org.webbitserver.netty.NettyWebSocketConnection;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class WebSocket {

    private final ClientBootstrap bootstrap;
    private final Channel channel;
    private final WebSocketHandler webSocketHandler;
    private final Executor executor;

    public WebSocket(URI uri, WebSocketHandler webSocketHandler, Executor executor) {
        this.webSocketHandler = webSocketHandler;
        this.executor = executor;
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        String host = uri.getHost() == null ? "localhost" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if (scheme.equalsIgnoreCase("ws")) {
                port = 80;
            } else if (scheme.equalsIgnoreCase("wss")) {
                // port = 443;
                throw new UnsupportedOperationException("SSL is not supported yet");
            }
        }

        if (!scheme.equalsIgnoreCase("ws") && !scheme.equalsIgnoreCase("wss")) {
            throw new IllegalArgumentException("Only ws(s) is supported.");
        }

        int mask = (int) (Math.random() * 0xFFFFFFFF);
        // TODO: Write the mask to buffer
        final byte[] outboundMaskingKey = new byte[]{1, 2, 3, 4};

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
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            close();
            throw new RuntimeException(future.getCause());
        }

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString().replaceFirst("http", "ws"));
        request.setHeader(HttpHeaders.Names.HOST, host);
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        request.setHeader("Sec-WebSocket-Version", 13);
        request.setHeader("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ=="); // TODO: Generate a random key here

        channel.write(request).awaitUninterruptibly();
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
            String webSocketAccept = response.getHeader("Sec-WebSocket-Accept"); // TODO: do something else if we didn't get one from the server
            adjustPipelineToWebSocket(ctx, HybiWebSocketFrameDecoder.clientSide(outboundMaskingKey), new HybiWebSocketFrameEncoder());
        }

        private void adjustPipelineToWebSocket(ChannelHandlerContext ctx, ChannelHandler webSocketFrameDecoder, ChannelHandler webSocketFrameEncoder) {
            final NettyWebSocketConnection webSocketConnection = new NettyWebSocketConnection(executor, null, ctx, outboundMaskingKey);
            webSocketConnection.setHybiWebSocketVersion(13);

            final Thread.UncaughtExceptionHandler exceptionHandler = new PrintStackTraceExceptionHandler();
            ChannelHandler webSocketChannelHandler = new WebSocketChannelHandler(webSocketConnection, exceptionHandler);

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

    private class WebSocketChannelHandler extends SimpleChannelUpstreamHandler {
        private final NettyWebSocketConnection webSocketConnection;
        private final Thread.UncaughtExceptionHandler exceptionHandler;

        public WebSocketChannelHandler(NettyWebSocketConnection webSocketConnection, Thread.UncaughtExceptionHandler exceptionHandler) {
            this.webSocketConnection = webSocketConnection;
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
            Object message = e.getMessage();
            if (message instanceof DecodingHybiFrame) {
                DecodingHybiFrame frame = (DecodingHybiFrame) message;
                frame.dispatchMessage(webSocketHandler, webSocketConnection, executor, exceptionHandler);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    exceptionHandler.uncaughtException(Thread.currentThread(), e.getCause());
                }
            });
        }
    }
}
