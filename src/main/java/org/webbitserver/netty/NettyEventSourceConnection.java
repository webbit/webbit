package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.util.CharsetUtil;
import org.webbitserver.EventSourceConnection;

import java.util.concurrent.Executor;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

public class NettyEventSourceConnection extends AbstractHttpConnection implements EventSourceConnection {
    public NettyEventSourceConnection(Executor executor, NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx) {
        super(ctx, nettyHttpRequest, executor);
    }

    @Override
    public NettyEventSourceConnection send(org.webbitserver.EventSourceMessage message) {
        writeMessage(copiedBuffer(message.build(), CharsetUtil.UTF_8));
        return this;
    }

    @Override
    public NettyEventSourceConnection data(String key, Object value) {
        putData(key, value);
        return this;
    }

    @Override
    public NettyEventSourceConnection close() {
        closeChannel();
        return this;
    }
}
