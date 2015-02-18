package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpListener;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executor;

public class ListenerConnectionHandler extends SimpleChannelUpstreamHandler {
    private final ConnectionHelper connectionHelper;

    public ListenerConnectionHandler(
            Executor executor,
            UncaughtExceptionHandler exceptionHandler,
            UncaughtExceptionHandler ioExceptionHandler,
            final HttpListener listener,
            final Integer channelId

    ) {
        this.connectionHelper = new ConnectionHelper(executor, exceptionHandler, ioExceptionHandler) {
            @Override
            protected void fireOnClose() throws Exception {
                listener.onClose(channelId);
            }
        };
    }

    @Override
    public void  channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) {
        connectionHelper.fireOnClose(e); 
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        connectionHelper.fireConnectionException(e);
    }
}
