package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.WebbitException;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;

public class EventSourceConnectionHandler extends SimpleChannelUpstreamHandler {
    protected final Executor executor;
    protected final NettyEventSourceConnection eventSourceConnection;
    protected final Thread.UncaughtExceptionHandler exceptionHandler;
    protected final Thread.UncaughtExceptionHandler ioExceptionHandler;
    protected final EventSourceHandler eventSourceHandler;

    public EventSourceConnectionHandler(
            NettyEventSourceConnection eventSourceConnection,
            UncaughtExceptionHandler exceptionHandler,
            UncaughtExceptionHandler ioExceptionHandler,
            EventSourceHandler eventSourceHandler, Executor executor
    ) {
        this.eventSourceHandler = eventSourceHandler;
        this.exceptionHandler = exceptionHandler;
        this.executor = executor;
        this.ioExceptionHandler = ioExceptionHandler;
        this.eventSourceConnection = eventSourceConnection;
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    eventSourceHandler.onClose(eventSourceConnection);
                } catch (Exception e1) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), WebbitException.fromException(e1, e.getChannel()));
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ClosedChannelException) {
            e.getChannel().close();
        } else {
            final Thread thread = Thread.currentThread();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ioExceptionHandler.uncaughtException(thread, WebbitException.fromExceptionEvent(e));
                }
            });
        }
    }
}
