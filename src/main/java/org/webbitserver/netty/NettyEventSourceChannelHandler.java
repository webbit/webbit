package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.EventSourceHandler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;

public class NettyEventSourceChannelHandler extends SimpleChannelUpstreamHandler {
    protected final Executor executor;
    protected final NettyHttpRequest nettyHttpRequest;
    protected final NettyEventSourceConnection eventSourceConnection;
    protected final Thread.UncaughtExceptionHandler exceptionHandler;
    protected final Thread.UncaughtExceptionHandler ioExceptionHandler;
    protected final EventSourceHandler handler;

    public NettyEventSourceChannelHandler(
            Executor executor,
            EventSourceHandler handler,
            ChannelHandlerContext ctx,
            UncaughtExceptionHandler exceptionHandler,
            NettyHttpRequest nettyHttpRequest,
            UncaughtExceptionHandler ioExceptionHandler,
            NettyEventSourceConnection eventSourceConnection,
            HttpRequest req,
            HttpResponse res
    ) {
        this.handler = handler;
        this.exceptionHandler = exceptionHandler;
        this.nettyHttpRequest = nettyHttpRequest;
        this.executor = executor;
        this.ioExceptionHandler = ioExceptionHandler;
        this.eventSourceConnection = eventSourceConnection;

        prepareConnection(req, res);
        ctx.getChannel().write(res);

        adjustPipeline(ctx);

        try {
            handler.onOpen(this.eventSourceConnection);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    protected void prepareConnection(HttpRequest req, HttpResponse res) {
        res.setStatus(HttpResponseStatus.OK);
        res.addHeader("Content-Type", "text/event-stream");
        res.addHeader("Transfer-Encoding", "identity");
        res.addHeader("Connection", "keep-alive");
        res.addHeader("Cache-Control", "no-cache");
        res.setChunked(false);
    }

    protected void adjustPipeline(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getChannel().getPipeline();
        StaleConnectionTrackingHandler staleConnectionTracker = (StaleConnectionTrackingHandler) p.remove("staleconnectiontracker");
        staleConnectionTracker.stopTracking(ctx.getChannel());
        p.remove("aggregator");
        p.replace("handler", "ssehandler", this);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.onClose(eventSourceConnection);
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
