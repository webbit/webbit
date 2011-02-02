package webbit.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import webbit.HttpHandler;
import webbit.WebServer;
import webbit.WebSocketHandler;
import webbit.handler.HttpToWebSocketHandler;
import webbit.handler.PathMatchHandler;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class NettyWebServer implements WebServer {
    private final ServerBootstrap bootstrap;
    private final SocketAddress socketAddress;
    private final URI publicUri;
    private final List<HttpHandler> handlers = new ArrayList<HttpHandler>();
    private final Executor executor;

    public NettyWebServer(int port) {
        this(Executors.newSingleThreadScheduledExecutor(), port);
    }

    public NettyWebServer(final Executor executor, int port) {
        this(executor, new InetSocketAddress(port), localUri(port));
    }

    public NettyWebServer(final Executor executor, SocketAddress socketAddress, URI publicUri) {
        this.executor = executor;
        this.socketAddress = socketAddress;
        this.publicUri = publicUri;

        // Configure the server.
        bootstrap = new ServerBootstrap();

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("handler", new NettyHttpChannelHandler(executor, handlers));
                return pipeline;
            }
        });
    }

    @Override
    public URI getUri() {
        return publicUri;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public NettyWebServer add(HttpHandler handler) {
        handlers.add(handler);
        return this;
    }

    @Override
    public NettyWebServer add(String path, HttpHandler handler) {
        return add(new PathMatchHandler(path, handler));
    }

    @Override
    public NettyWebServer add(String path, WebSocketHandler handler) {
        return add(path, new HttpToWebSocketHandler(handler));
    }

    @Override
    public NettyWebServer start() {
        bootstrap.setFactory(new NioServerSocketChannelFactory(
                Executors.newSingleThreadExecutor(),
                Executors.newSingleThreadExecutor(), 1));
        bootstrap.bind(socketAddress);
        return this;
    }

    @Override
    public NettyWebServer stop() throws IOException {
        // TODO
        return this;
    }

    public NettyWebServer join() throws InterruptedException {
        // TODO
        return this;
    }

    private static URI localUri(int port) {
        try {
            return URI.create("http://" + InetAddress.getLocalHost().getHostName() + (port == 80 ? "" : (":" + port)) + "/");
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
