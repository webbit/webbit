package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.webbitserver.CometConnection;
import org.webbitserver.CometHandler;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;

public abstract class CometChannelHandler extends SimpleChannelUpstreamHandler {
    protected final Executor executor;
    protected final NettyHttpRequest nettyHttpRequest;
    protected final CometHandler handler;
    protected final CometConnection cometConnection;
    protected final Thread.UncaughtExceptionHandler exceptionHandler;
    protected final Thread.UncaughtExceptionHandler ioExceptionHandler;

    public CometChannelHandler(CometHandler handler, ChannelHandlerContext ctx, Thread.UncaughtExceptionHandler exceptionHandler, NettyHttpRequest nettyHttpRequest, Executor executor, Thread.UncaughtExceptionHandler ioExceptionHandler, CometConnection cometConnection, HttpRequest req, HttpResponse res) {
        this.handler = handler;
        this.exceptionHandler = exceptionHandler;
        this.nettyHttpRequest = nettyHttpRequest;
        this.executor = executor;
        this.ioExceptionHandler = ioExceptionHandler;
        this.cometConnection = cometConnection;

        prepareConnection(req, res);
        ctx.getChannel().write(res);

        adjustPipeline(ctx);

        try {
            handler.onOpen(this.cometConnection);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    protected abstract void prepareConnection(HttpRequest req, HttpResponse res);

    protected abstract void adjustPipeline(ChannelHandlerContext ctx);

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.onClose(cometConnection);
                } catch (Exception e1) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), e1);
                }
            }
        });
    }

    @Override
    public String toString() {
        return nettyHttpRequest.toString();
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
                    ioExceptionHandler.uncaughtException(thread, e.getCause());
                }
            });
        }
    }
}
