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

    public NettyHttpChannelHandler(Executor executor, List<HttpHandler> httpHandlers) {
        this.executor = executor;
        this.httpHandlers = httpHandlers;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        final HttpRequest httpRequest = (HttpRequest) messageEvent.getMessage();
        final NettyHttpRequest nettyHttpRequest = new NettyHttpRequest(messageEvent, httpRequest);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpControl control = new NettyHttpControl(httpHandlers.iterator(), executor, ctx, nettyHttpRequest, httpRequest, new DefaultHttpResponse(HTTP_1_1, OK));
                    NettyHttpResponse nettyHttpResponse = new NettyHttpResponse(ctx, httpRequest, new DefaultHttpResponse(HTTP_1_1, OK));
                    control.nextHandler(nettyHttpRequest, nettyHttpResponse);
                } catch (Exception exception) {
                    // TODO
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        if (e.getCause() instanceof ClosedChannelException) {
            e.getChannel().close();
        } else {
            super.exceptionCaught(ctx, e);
        }
    }

}
