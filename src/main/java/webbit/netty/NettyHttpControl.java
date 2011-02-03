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
    private final NettyHttpResponse nettyHttpResponse;
    private final org.jboss.netty.handler.codec.http.HttpRequest httpRequest;
    private final DefaultHttpResponse defaultHttpResponse;

    private HttpRequest defaultRequest;
    private HttpResponse defaultResponse;

    public NettyHttpControl(Iterator<HttpHandler> handlerIterator,
                            Executor executor,
                            ChannelHandlerContext ctx,
                            NettyHttpRequest nettyHttpRequest,
                            NettyHttpResponse nettyHttpResponse,
                            org.jboss.netty.handler.codec.http.HttpRequest httpRequest,
                            DefaultHttpResponse defaultHttpResponse) {
        this.handlerIterator = handlerIterator;
        this.executor = executor;
        this.ctx = ctx;
        this.nettyHttpRequest = nettyHttpRequest;
        this.nettyHttpResponse = nettyHttpResponse;
        this.httpRequest = httpRequest;
        this.defaultHttpResponse = defaultHttpResponse;
        defaultRequest = nettyHttpRequest;
        defaultResponse = nettyHttpResponse;
    }

    @Override
    public void nextHandler() {
        nextHandler(defaultRequest, defaultResponse, this);
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response) {
        nextHandler(request, response, this);
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response, HttpControl control) {
        this.defaultRequest = request;
        this.defaultResponse = response;
        if (handlerIterator.hasNext()) {
            HttpHandler handler = handlerIterator.next();
            try {
                handler.handleHttpRequest(request, response, control);
            } catch (Exception e) {
                response.error(e);
            }
        } else {
            response.status(404).end();
        }
    }

    @Override
    public NettyWebSocketConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
        NettyWebSocketConnection webSocketConnection = createWebSocketConnection();
        new NettyWebSocketChannelHandler(executor, ctx, nettyHttpRequest, httpRequest, defaultHttpResponse, handler, webSocketConnection);
        return webSocketConnection;
    }

    @Override
    public NettyWebSocketConnection createWebSocketConnection() {
        return new NettyWebSocketConnection(executor, nettyHttpRequest, ctx);
    }

    @Override
    public Executor handlerExecutor() {
        return executor;
    }

    @Override
    public void execute(Runnable command) {
        handlerExecutor().execute(command);
    }
}
