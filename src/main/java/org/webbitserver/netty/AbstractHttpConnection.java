package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.webbitserver.HttpConnection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public abstract class AbstractHttpConnection implements HttpConnection {
    private final Executor executor;
    private final NettyHttpRequest nettyHttpRequest;
    private final ChannelHandlerContext ctx;

    public AbstractHttpConnection(ChannelHandlerContext ctx, NettyHttpRequest nettyHttpRequest, Executor executor) {
        this.ctx = ctx;
        this.nettyHttpRequest = nettyHttpRequest;
        this.executor = executor;
    }

    protected ChannelFuture writeMessage(Object message) {
        final ChannelFuture write = ctx.getChannel().write(message);
        write.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        return write;
    }

    protected void closeChannel() {
        ctx.getChannel().write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    protected void putData(String key, Object value) {
        data().put(key, value);
    }

    @Override
    public NettyHttpRequest httpRequest() {
        return nettyHttpRequest;
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
    public Set<String> dataKeys() {
        return data().keySet();
    }

    @Override
    public Executor handlerExecutor() {
        return executor;
    }

    @Override
    public void execute(Runnable command) {
        handlerExecutor().execute(command);
    }
}
