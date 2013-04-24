package org.webbitserver.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;
import org.webbitserver.handler.DateHeaderHandler;
import org.webbitserver.handler.HttpToEventSourceHandler;
import org.webbitserver.handler.HttpToWebSocketHandler;
import org.webbitserver.handler.PathMatchHandler;
import org.webbitserver.handler.ServerHeaderHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;
import org.webbitserver.handler.exceptions.SilentExceptionHandler;
import org.webbitserver.helpers.NamingThreadFactory;
import org.webbitserver.helpers.SslFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.jboss.netty.channel.Channels.pipeline;

public class NettyWebServer implements WebServer {
    private static final long DEFAULT_STALE_CONNECTION_TIMEOUT = 5000;

    private final SocketAddress socketAddress;
    private final URI publicUri;
    private final List<HttpHandler> handlers = new ArrayList<HttpHandler>();
    private final List<ExecutorService> executorServices = new ArrayList<ExecutorService>();
    private final Executor executor;

    private ServerBootstrap bootstrap;
    private Channel channel;
    private SSLContext sslContext;

    protected long nextId = 1;
    private Thread.UncaughtExceptionHandler exceptionHandler;
    private Thread.UncaughtExceptionHandler ioExceptionHandler;
    private ConnectionTrackingHandler connectionTrackingHandler;
    private StaleConnectionTrackingHandler staleConnectionTrackingHandler;
    private long staleConnectionTimeout = DEFAULT_STALE_CONNECTION_TIMEOUT;
    private int maxInitialLineLength = 4096;
    private int maxHeaderSize = 8192;
    private int maxChunkSize = 8192;
    private int maxContentLength = 65536;

    public NettyWebServer(int port) {
        this(Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory("WEBBIT-HANDLER-THREAD")), port);
    }

    private NettyWebServer(ExecutorService executorService, int port) {
        this((Executor) executorService, port);
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

        setupDefaultHandlers();
    }

    protected void setupDefaultHandlers() {
        add(new ServerHeaderHandler("Webbit"));
        add(new DateHeaderHandler());
    }

    @Override
    public NettyWebServer setupSsl(InputStream keyStore, String pass) throws WebbitException {
        return this.setupSsl(keyStore, pass, pass);
    }

    @Override
    public NettyWebServer setupSsl(InputStream keyStore, String storePass, String keyPass) throws WebbitException {
        this.sslContext = new SslFactory(keyStore, storePass).getServerContext(keyPass);
        return this;
    }

    @Override
    public URI getUri() {
        return publicUri;
    }

    @Override
    public int getPort() {
        if (publicUri.getPort() == -1) {
            return publicUri.getScheme().equalsIgnoreCase("https") ? 443 : 80;
        }
        return publicUri.getPort();
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public NettyWebServer staleConnectionTimeout(long millis) {
        staleConnectionTimeout = millis;
        return this;
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
    public NettyWebServer add(String path, EventSourceHandler handler) {
        return add(path, new HttpToEventSourceHandler(handler));
    }

    @Override
    public Future<NettyWebServer> start() {
        FutureTask<NettyWebServer> future = new FutureTask<NettyWebServer>(new Callable<NettyWebServer>() {
            @Override
            public NettyWebServer call() throws Exception {
                if (isRunning()) {
                    throw new IllegalStateException("Server already started.");
                }

                // Configure the server.
                bootstrap = new ServerBootstrap();

                // Set up the event pipeline factory.
                bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                    @Override
                    public ChannelPipeline getPipeline() throws Exception {
                        long timestamp = timestamp();
                        Object id = nextId();
                        ChannelPipeline pipeline = pipeline();
                        if (sslContext != null) {
                            SSLEngine sslEngine = sslContext.createSSLEngine();
                            sslEngine.setUseClientMode(false);
                            SslHandler ssl = new SslHandler(sslEngine);
                            ssl.setCloseOnSSLException(true);
                            pipeline.addLast("ssl", ssl);
                        }
                        pipeline.addLast("staleconnectiontracker", staleConnectionTrackingHandler);
                        pipeline.addLast("connectiontracker", connectionTrackingHandler);
                        pipeline.addLast("flashpolicydecoder", new FlashPolicyFileDecoder(executor, exceptionHandler, ioExceptionHandler, getPort()));
                        pipeline.addLast("decoder", new HttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize));
                        pipeline.addLast("aggregator", new HttpChunkAggregator(maxContentLength));
                        pipeline.addLast("decompressor", new HttpContentDecompressor());
                        pipeline.addLast("encoder", new HttpResponseEncoder());
                        pipeline.addLast("compressor", new HttpContentCompressor());
                        pipeline.addLast("handler", new NettyHttpChannelHandler(executor, handlers, id, timestamp, exceptionHandler, ioExceptionHandler));
                        return pipeline;
                    }
                });

                staleConnectionTrackingHandler = new StaleConnectionTrackingHandler(staleConnectionTimeout, executor);
                ScheduledExecutorService staleCheckExecutor = Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory("WEBBIT-STALE-CONNECTION-CHECK-THREAD"));
                staleCheckExecutor.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        staleConnectionTrackingHandler.closeStaleConnections();
                    }
                }, staleConnectionTimeout / 2, staleConnectionTimeout / 2, TimeUnit.MILLISECONDS);
                executorServices.add(staleCheckExecutor);

                connectionTrackingHandler = new ConnectionTrackingHandler();
                ExecutorService bossExecutor = Executors.newSingleThreadExecutor(new NamingThreadFactory("WEBBIT-BOSS-THREAD"));
                executorServices.add(bossExecutor);
                ExecutorService workerExecutor = Executors.newSingleThreadExecutor(new NamingThreadFactory("WEBBIT-WORKER-THREAD"));
                executorServices.add(workerExecutor);
                bootstrap.setFactory(new NioServerSocketChannelFactory(bossExecutor, workerExecutor, 1));
                channel = bootstrap.bind(socketAddress);
                return NettyWebServer.this;
            }
        });
        // don't use Executor here - it's just another resource we need to manage -
        // thread creation on startup should be fine
        final Thread thread = new Thread(future, "WEBBIT-STARTUP-THREAD");
        thread.start();
        return future;
    }

    public boolean isRunning() {
        return channel != null && channel.isBound();
    }

    @Override
    public Future<WebServer> stop() {
        FutureTask<WebServer> future = new FutureTask<WebServer>(new Callable<WebServer>() {
            @Override
            public WebServer call() throws Exception {
                if (channel != null) {
                    channel.close();
                }
                if (connectionTrackingHandler != null) {
                    connectionTrackingHandler.closeAllConnections();
                    connectionTrackingHandler = null;
                }
                if (bootstrap != null) {
                    bootstrap.releaseExternalResources();
                }

                // shut down all services & give them a chance to terminate
                for (ExecutorService executorService : executorServices) {
                    shutdownAndAwaitTermination(executorService);
                }

                bootstrap = null;

                if (channel != null) {
                    channel.getCloseFuture().await();
                }

                return NettyWebServer.this;
            }
        });
        // don't use Executor here - it's just another resource we need to manage -
        // thread creation on shutdown should be fine
        final Thread thread = new Thread(future, "WEBBIT-SHUTDOW-THREAD");
        thread.start();
        return future;
    }

    // See JavaDoc for ExecutorService
    private void shutdownAndAwaitTermination(ExecutorService executorService) {
        executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public NettyWebServer uncaughtExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public NettyWebServer connectionExceptionHandler(Thread.UncaughtExceptionHandler ioExceptionHandler) {
        this.ioExceptionHandler = ioExceptionHandler;
        return this;
    }

    /**
     * @see HttpRequestDecoder
     */
    public NettyWebServer maxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    /**
     * @see HttpChunkAggregator
     */
    public NettyWebServer maxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
        return this;
    }

    /**
     * @see HttpRequestDecoder
     */
    public NettyWebServer maxHeaderSize(int maxHeaderSize) {
        this.maxHeaderSize = maxHeaderSize;
        return this;
    }

    /**
     * @see HttpRequestDecoder
     */
    public NettyWebServer maxInitialLineLength(int maxInitialLineLength) {
        this.maxInitialLineLength = maxInitialLineLength;
        return this;
    }

    private static URI localUri(int port) {
        try {
            return URI.create("http://" + InetAddress.getLocalHost()
                    .getHostName() + (port == 80 ? "" : (":" + port)) + "/");
        } catch (UnknownHostException e) {
            throw new RuntimeException("can not create URI from localhost hostname - use constructor to pass an explicit URI", e);
        }
    }

    protected long timestamp() {
        return System.currentTimeMillis();
    }

    protected Object nextId() {
        return nextId++;
    }

}
