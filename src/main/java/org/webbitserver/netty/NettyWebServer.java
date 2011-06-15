package org.webbitserver.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.HttpToEventSourceHandler;
import org.webbitserver.handler.HttpToWebSocketHandler;
import org.webbitserver.handler.PathMatchHandler;
import org.webbitserver.handler.ServerHeaderHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;
import org.webbitserver.handler.exceptions.SilentExceptionHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class NettyWebServer implements WebServer {
    private final ServerBootstrap bootstrap;
    private final SocketAddress socketAddress;
    private final URI publicUri;
    private final List<HttpHandler> handlers = new ArrayList<HttpHandler>();
    private final List<ExecutorService> executorServices = new ArrayList<ExecutorService>();
    private final Executor executor;
    private Channel channel;

    protected long nextId = 1;

    private Thread.UncaughtExceptionHandler exceptionHandler;
    private Thread.UncaughtExceptionHandler ioExceptionHandler;

    public NettyWebServer(int port) {
        this(Executors.newSingleThreadScheduledExecutor(), port);
    }

    private NettyWebServer(ExecutorService executorService, int port) {
        this((Executor)executorService, port);
        // If we created the executor, we have to be responsible for tearing it down.
        executorServices.add(executorService);
    }

    public NettyWebServer(final Executor executor, int port) {
        this(executor, new InetSocketAddress(port), localUri(port));
    }

    public NettyWebServer(final Executor executor, SocketAddress socketAddress, URI publicUri) {
        this.executor = executor;
        this.socketAddress = socketAddress;
        this.publicUri = publicUri;

        // Uncaught exceptions from handlers get dumped to console by default.
        // To change, call uncaughtExceptionHandler()
        uncaughtExceptionHandler(new PrintStackTraceExceptionHandler());

        // Default behavior is to silently discard any exceptions caused
        // when reading/writing to the client. The Internet is flaky - it happens.
        connectionExceptionHandler(new SilentExceptionHandler());

        // Configure the server.
        bootstrap = new ServerBootstrap();

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                long timestamp = timestamp();
                Object id = nextId();
                ChannelPipeline pipeline = pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("handler", new NettyHttpChannelHandler(
                        executor, handlers, id, timestamp, exceptionHandler, ioExceptionHandler));
                return pipeline;
            }
        });

        setupDefaultHandlers();
    }

    protected void setupDefaultHandlers() {
        add(new ServerHeaderHandler("Webbit"));
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
    public WebServer add(String path, EventSourceHandler handler) {
        return add(path, new HttpToEventSourceHandler(handler));
    }

    @Override
    public synchronized NettyWebServer start() {
        ExecutorService exec1 = Executors.newSingleThreadExecutor();
        executorServices.add(exec1);
        ExecutorService exec2 = Executors.newSingleThreadExecutor();
        executorServices.add(exec2);
        bootstrap.setFactory(new NioServerSocketChannelFactory(exec1, exec2, 1));
        channel = bootstrap.bind(socketAddress);
        return this;
    }

    @Override
    public synchronized NettyWebServer stop() throws IOException {
        if (channel != null) {
            channel.close();
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
        for (ExecutorService executorService : executorServices) {
            executorService.shutdown();
        }
        return this;
    }

    @Override
    public synchronized NettyWebServer join() throws InterruptedException {
        if (channel != null) {
            channel.getCloseFuture().await();
        }
        return this;
    }

    @Override
    public WebServer uncaughtExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public WebServer connectionExceptionHandler(Thread.UncaughtExceptionHandler ioExceptionHandler) {
        this.ioExceptionHandler = ioExceptionHandler;
        return this;
    }

    private static URI localUri(int port) {
        try {
            return URI.create("http://" + InetAddress.getLocalHost().getHostName() + (port == 80 ? "" : (":" + port)) + "/");
        } catch (UnknownHostException e) {
            return null;
        }
    }

    protected long timestamp() {
        return System.currentTimeMillis();
    }

    protected Object nextId() {
        return nextId++;
    }

}
