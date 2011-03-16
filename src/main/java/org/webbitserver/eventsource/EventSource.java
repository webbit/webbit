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
import java.util.concurrent.Executors;

public class EventSource {

    private final ClientBootstrap bootstrap;
    private final URI uri;
    private final EventSourceClientHandler clientHandler;

    public EventSource(final URI uri, EventSourceHandler eventSourceHandler) {
        this.uri = uri;
        bootstrap = new ClientBootstrap(
                    new NioClientSocketChannelFactory(
                            Executors.newSingleThreadExecutor(),
                            Executors.newSingleThreadExecutor()));
        clientHandler = new EventSourceClientHandler(uri, eventSourceHandler);

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

    public ChannelFuture connect() {
        return bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
    }

    public ChannelFuture disconnect() {
        return clientHandler.close();
    }
}
