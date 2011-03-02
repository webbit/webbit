package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.webbitserver.CometConnection;
import org.webbitserver.netty.contrib.DefaultEventSourceFrame;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class NettyEventSourceConnection implements CometConnection {
    private final Executor executor;
    private final NettyHttpRequest nettyHttpRequest;
    private final ChannelHandlerContext ctx;

    public NettyEventSourceConnection(Executor executor, NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx) {
        this.executor = executor;
        this.nettyHttpRequest = nettyHttpRequest;
        this.ctx = ctx;
    }

    @Override
    public NettyHttpRequest httpRequest() {
        return nettyHttpRequest;
    }

    @Override
    public CometConnection send(String message) {
        ctx.getChannel().write(new DefaultEventSourceFrame(message).toChannelBuffer());
        return this;
    }

    @Override
    public CometConnection close() {
        ctx.getChannel().close();
        return this;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public Map<String, Object> data() {
        return nettyHttpRequest.data();
    }

    @Override
    public Object data(String key) {
        return data().get(key);
    }

    @Override
    public NettyEventSourceConnection data(String key, Object value) {
        data().put(key, value);
        return this;
    }

    @Override
    public Set<String> dataKeys() {
        return data().keySet();
    }

    @Override
    public Executor handlerExecutor() {
        return executor;
    }

    @Override
    public String protocol() {
        return EVENT_SOURCE;
    }
}
