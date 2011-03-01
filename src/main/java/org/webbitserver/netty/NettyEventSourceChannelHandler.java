package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.CometHandler;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executor;

public class NettyEventSourceChannelHandler extends CometChannelHandler {

    public NettyEventSourceChannelHandler(Executor executor,
                                          ChannelHandlerContext ctx,
                                          NettyHttpRequest nettyHttpRequest,
                                          HttpResponse response,
                                          CometHandler handler,
                                          NettyEventSourceConnection eventSourceConnection,
                                          UncaughtExceptionHandler exceptionHandler,
                                          UncaughtExceptionHandler ioExceptionHandler, HttpRequest req) {
        super(handler, ctx, exceptionHandler, nettyHttpRequest, executor, ioExceptionHandler, eventSourceConnection, req, response);
    }

    @Override
    protected void prepareConnection(HttpRequest req, HttpResponse res) {
        res.setStatus(HttpResponseStatus.OK);
        res.addHeader("Content-Type", "text/event-stream");
        res.addHeader("Cache-Control", "no-cache");
    }

    @Override
    protected void adjustPipeline(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.getChannel().getPipeline();
        p.remove("aggregator");
        p.replace("handler", "ssehandler", this);
    }
}
