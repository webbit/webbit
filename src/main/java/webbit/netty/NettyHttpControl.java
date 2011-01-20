package webbit.netty;

import webbit.HttpHandler;
import webbit.HttpRequest;
import webbit.HttpResponse;
import webbit.HttpControl;

import java.util.Iterator;

public class NettyHttpControl implements HttpControl {

    private final Iterator<HttpHandler> handlerIterator;

    public NettyHttpControl(Iterator<HttpHandler> handlerIterator) {
        this.handlerIterator = handlerIterator;
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
}
