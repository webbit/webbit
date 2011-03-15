package org.webbitserver.es;

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

public abstract class EventSource {

    private final ClientBootstrap bootstrap;
    private final URI url;
    private final EventSourceClientHandler clientHandler;

    public EventSource(final URI url) {
        this.url = url;
        bootstrap = new ClientBootstrap(
                    new NioClientSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool()));
        clientHandler = new EventSourceClientHandler(url, this);

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
        return bootstrap.connect(new InetSocketAddress(url.getHost(), url.getPort()));
    }

    public ChannelFuture disconnect() {
        return clientHandler.close();
    }

    public abstract void onConnect();
    public abstract void onMessage(String message);
    public abstract void onDisconnect();
    public abstract void onError(Throwable t);
}
