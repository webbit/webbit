package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.WebbitException;

import java.util.List;
import java.util.concurrent.Executor;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyHttpChannelHandler extends SimpleChannelUpstreamHandler {

    private final Executor executor;
    private final List<HttpHandler> httpHandlers;
    private final Object id;
    private final long timestamp;
    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler ioExceptionHandler;
    private final ConnectionHelper connectionHelper;

    public NettyHttpChannelHandler(Executor executor,
                                   List<HttpHandler> httpHandlers,
                                   Object id,
                                   long timestamp,
                                   Thread.UncaughtExceptionHandler exceptionHandler,
                                   Thread.UncaughtExceptionHandler ioExceptionHandler) {
        this.executor = executor;
        this.httpHandlers = httpHandlers;
        this.id = id;
        this.timestamp = timestamp;
        this.exceptionHandler = exceptionHandler;
        this.ioExceptionHandler = ioExceptionHandler;

        connectionHelper = new ConnectionHelper(executor, exceptionHandler, ioExceptionHandler) {
            @Override
            protected void fireOnClose() throws Exception {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        if (messageEvent.getMessage() instanceof HttpRequest) {
            handleHttpRequest(ctx, messageEvent, (HttpRequest) messageEvent.getMessage());
        } else {
            super.messageReceived(ctx, messageEvent);
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, MessageEvent messageEvent, HttpRequest httpRequest) {
        final NettyHttpRequest nettyHttpRequest = new NettyHttpRequest(messageEvent, httpRequest, id, timestamp);
        final NettyHttpResponse nettyHttpResponse = new NettyHttpResponse(
                ctx, new DefaultHttpResponse(HTTP_1_1, OK), isKeepAlive(httpRequest), exceptionHandler);
        final HttpControl control = new NettyHttpControl(httpHandlers.iterator(), executor, ctx,
                nettyHttpRequest, nettyHttpResponse, httpRequest, new DefaultHttpResponse(HTTP_1_1, OK),
                exceptionHandler, ioExceptionHandler);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    control.nextHandler(nettyHttpRequest, nettyHttpResponse);
                } catch (Exception exception) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), WebbitException.fromException(exception, ctx.getChannel()));
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent e) {
        connectionHelper.fireConnectionException(e);
    }

}
