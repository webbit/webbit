package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.webbitserver.EventSourceHandler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executor;

public class EventSourceConnectionHandler extends SimpleChannelUpstreamHandler {
    private final ConnectionHelper connectionHelper;

    public EventSourceConnectionHandler(
            Executor executor,
            UncaughtExceptionHandler exceptionHandler,
            UncaughtExceptionHandler ioExceptionHandler,
            final NettyEventSourceConnection eventSourceConnection,
            final EventSourceHandler eventSourceHandler
    ) {
        this.connectionHelper = new ConnectionHelper(executor, exceptionHandler, ioExceptionHandler) {
            @Override
            protected void fireOnClose() throws Exception {
                eventSourceHandler.onClose(eventSourceConnection);
            }
        };
    }

    @Override
    public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) {
        connectionHelper.fireOnClose(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        connectionHelper.fireConnectionException(e);
    }
}
