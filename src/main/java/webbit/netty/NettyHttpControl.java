package webbit.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.*;
import webbit.*;
import webbit.HttpRequest;
import webbit.HttpResponse;

import java.util.Iterator;
import java.util.concurrent.Executor;

public class NettyHttpControl implements HttpControl {

    private final Iterator<HttpHandler> handlerIterator;
    private final Executor executor;
    private final ChannelHandlerContext ctx;
    private final NettyHttpRequest nettyHttpRequest;
    private final org.jboss.netty.handler.codec.http.HttpRequest httpRequest;
    private final DefaultHttpResponse defaultHttpResponse;

    public NettyHttpControl(Iterator<HttpHandler> handlerIterator,
                            Executor executor,
                            ChannelHandlerContext ctx,
                            NettyHttpRequest nettyHttpRequest,
                            org.jboss.netty.handler.codec.http.HttpRequest httpRequest,
                            DefaultHttpResponse defaultHttpResponse) {
        this.handlerIterator = handlerIterator;
        this.executor = executor;
        this.ctx = ctx;
        this.nettyHttpRequest = nettyHttpRequest;
        this.httpRequest = httpRequest;
        this.defaultHttpResponse = defaultHttpResponse;
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response) {
        if (handlerIterator.hasNext()) {
            HttpHandler handler = handlerIterator.next();
            try {
                handler.handleHttpRequest(request, response, this);
            } catch (Exception e) {
                response.error(e);
            }
        } else {
            response.status(404).end();
        }
    }

    @Override
    public void upgradeToWebSocketConnection(WebSocketHandler handler) {
         new NettyWebSocketConnection(executor, ctx, nettyHttpRequest, httpRequest, defaultHttpResponse, handler);
    }

}
