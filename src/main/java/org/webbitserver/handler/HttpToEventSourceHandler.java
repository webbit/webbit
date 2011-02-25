package org.webbitserver.handler;

import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class HttpToEventSourceHandler implements HttpHandler {
    private final EventSourceHandler eventSourceHandler;

    public HttpToEventSourceHandler(EventSourceHandler eventSourceHandler) {
        this.eventSourceHandler = eventSourceHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        control.upgradeToEventSourceConnection(eventSourceHandler);
    }
}
