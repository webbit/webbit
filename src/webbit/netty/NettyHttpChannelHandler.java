package webbit.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import webbit.HttpHandler;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyHttpChannelHandler extends SimpleChannelUpstreamHandler {

    private final HttpHandler httpHandler;

    public NettyHttpChannelHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest httpRequest = (HttpRequest) e.getMessage();
        NettyHttpRequest nettyHttpRequest = new NettyHttpRequest(e, httpRequest);
        httpHandler.handleHttpRequest(
                nettyHttpRequest,
                new NettyHttpResponse(ctx, nettyHttpRequest, httpRequest, new DefaultHttpResponse(HTTP_1_1, OK)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

}
