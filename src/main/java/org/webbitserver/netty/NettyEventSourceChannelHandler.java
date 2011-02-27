package org.webbitserver.netty;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.EventSourceHandler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;

public class NettyEventSourceChannelHandler extends SimpleChannelUpstreamHandler {
    private final Executor executor;
    private final NettyHttpRequest nettyHttpRequest;
    private final EventSourceHandler handler;
    private final NettyEventSourceConnection eventSourceConnection;
    private final UncaughtExceptionHandler exceptionHandler;
    private final UncaughtExceptionHandler ioExceptionHandler;

    public NettyEventSourceChannelHandler(Executor executor, ChannelHandlerContext ctx, NettyHttpRequest nettyHttpRequest, HttpResponse response, EventSourceHandler handler, NettyEventSourceConnection eventSourceConnection, UncaughtExceptionHandler exceptionHandler, UncaughtExceptionHandler ioExceptionHandler) {
        this.executor = executor;
        this.nettyHttpRequest = nettyHttpRequest;
        this.handler = handler;
        this.eventSourceConnection = eventSourceConnection;
        this.exceptionHandler = exceptionHandler;
        this.ioExceptionHandler = ioExceptionHandler;

        declareEventStream(response);
        ctx.getChannel().write(response);

        ChannelPipeline p = ctx.getChannel().getPipeline();
        p.remove("aggregator");
        p.replace("handler", "ssehandler", this);

        try {
            handler.onOpen(this.eventSourceConnection);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    private void declareEventStream(HttpResponse res) {
        res.setStatus(HttpResponseStatus.OK);
        res.addHeader("Content-Type", "text/event-stream");
        res.addHeader("Cache-Control", "no-cache");
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
    }}
