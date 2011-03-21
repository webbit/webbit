package org.webbitserver.eventsource;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventSource {
    public static final long DEFAULT_RECONNECTION_TIME_MILLIS = 2000;
    private final ClientBootstrap bootstrap;
    private final EventSourceChannelHandler clientHandler;

    public EventSource(Executor executor, long reconnectionTimeMillis, final URI uri, EventSourceClientHandler eventSourceHandler) {
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newSingleThreadExecutor(),
                        Executors.newSingleThreadExecutor()));
        bootstrap.setOption("remoteAddress", new InetSocketAddress(uri.getHost(), uri.getPort()));

        clientHandler = new EventSourceChannelHandler(executor, reconnectionTimeMillis, bootstrap, uri, eventSourceHandler);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new HttpResponseDecoder());
                pipeline.addLast("encoder", new HttpRequestEncoder());
                pipeline.addLast("es-handler", clientHandler);
                return pipeline;
            }
        });
    }

    public EventSource(URI uri, EventSourceClientHandler eventSourceHandler) {
        this(Executors.newSingleThreadExecutor(), DEFAULT_RECONNECTION_TIME_MILLIS, uri, eventSourceHandler);
    }

    public ChannelFuture connect() {
        return bootstrap.connect();
    }

    /**
     * Close the connection
     *
     * @return self
     */
    public EventSource close() {
        clientHandler.close();
        return this;
    }

    /**
     * Wait for until the connection is closed
     *
     * @return self
     * @throws InterruptedException if waiting was interrupted
     */
    public EventSource join() throws InterruptedException {
        clientHandler.join();
        return this;
    }
}
