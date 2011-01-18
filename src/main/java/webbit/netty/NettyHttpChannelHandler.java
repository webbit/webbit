package webbit.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import webbit.HttpHandler;

import java.util.concurrent.Executor;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyHttpChannelHandler extends SimpleChannelUpstreamHandler {

    private final Executor executor;
    private final HttpHandler httpHandler;

    public NettyHttpChannelHandler(Executor executor, HttpHandler httpHandler) {
        this.executor = executor;
        this.httpHandler = httpHandler;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        final HttpRequest httpRequest = (HttpRequest) messageEvent.getMessage();
        final NettyHttpRequest nettyHttpRequest = new NettyHttpRequest(messageEvent, httpRequest);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    httpHandler.handleHttpRequest(
                            nettyHttpRequest,
                            new NettyHttpResponse(executor, ctx, nettyHttpRequest, httpRequest, new DefaultHttpResponse(HTTP_1_1, OK)));
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
        // TODO
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

}
