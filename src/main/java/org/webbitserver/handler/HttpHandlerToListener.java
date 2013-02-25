package org.webbitserver.handler;

import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.HttpListener;

public class HttpHandlerToListener implements HttpHandler {
    private final HttpListener handler;

    public HttpHandlerToListener(HttpListener handler) {
        this.handler = handler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        control.listener(handler);
        handler.handleHttpRequest(request, response, control);
    }
}
