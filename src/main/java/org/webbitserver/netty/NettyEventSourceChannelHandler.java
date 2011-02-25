package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.EventSourceHandler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executor;

public class NettyEventSourceChannelHandler {
    private final Executor executor;
    private final ChannelHandlerContext ctx;
    private final NettyHttpRequest nettyHttpRequest;
    private final HttpRequest httpRequest;
    private final EventSourceHandler handler;
    private final NettyEventSourceConnection eventSourceConnection;
    private final UncaughtExceptionHandler exceptionHandler;
    private final UncaughtExceptionHandler ioExceptionHandler;

    public NettyEventSourceChannelHandler(Executor executor, ChannelHandlerContext ctx, NettyHttpRequest nettyHttpRequest, HttpRequest request, HttpResponse response, EventSourceHandler handler, NettyEventSourceConnection eventSourceConnection, UncaughtExceptionHandler exceptionHandler, UncaughtExceptionHandler ioExceptionHandler) {
        this.executor = executor;
        this.ctx = ctx;
        this.nettyHttpRequest = nettyHttpRequest;
        this.httpRequest = request;
        this.handler = handler;
        this.eventSourceConnection = eventSourceConnection;
        this.exceptionHandler = exceptionHandler;
        this.ioExceptionHandler = ioExceptionHandler;

        declareEventStream(response);
        ctx.getChannel().write(response);
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
}
