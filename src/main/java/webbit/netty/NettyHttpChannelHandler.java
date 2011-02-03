package webbit.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import webbit.HttpHandler;
import webbit.HttpControl;

import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.concurrent.Executor;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyHttpChannelHandler extends SimpleChannelUpstreamHandler {

    private final Executor executor;
    private final List<HttpHandler> httpHandlers;
    private final Object id;
    private final long timestamp;
    private final Thread.UncaughtExceptionHandler exceptionHandler;

    public NettyHttpChannelHandler(Executor executor,
                                   List<HttpHandler> httpHandlers,
                                   Object id,
                                   long timestamp,
                                   Thread.UncaughtExceptionHandler exceptionHandler) {
        this.executor = executor;
        this.httpHandlers = httpHandlers;
        this.id = id;
        this.timestamp = timestamp;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        final HttpRequest httpRequest = (HttpRequest) messageEvent.getMessage();
        final NettyHttpRequest nettyHttpRequest = new NettyHttpRequest(messageEvent, httpRequest, id, timestamp);
        final NettyHttpResponse nettyHttpResponse = new NettyHttpResponse(ctx, httpRequest, new DefaultHttpResponse(HTTP_1_1, OK));
        final HttpControl control = new NettyHttpControl(httpHandlers.iterator(), executor, ctx,
                nettyHttpRequest, nettyHttpResponse, httpRequest, new DefaultHttpResponse(HTTP_1_1, OK), exceptionHandler);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    control.nextHandler(nettyHttpRequest, nettyHttpResponse);
                } catch (Exception exception) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), exception);
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent e)
            throws Exception {
        if (e.getCause() instanceof ClosedChannelException) {
            e.getChannel().close();
        } else {
            final Thread thread = Thread.currentThread();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    exceptionHandler.uncaughtException(thread, e.getCause());
                }
            });
        }
    }

}
