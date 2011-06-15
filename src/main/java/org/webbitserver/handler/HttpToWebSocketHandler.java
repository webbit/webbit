package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebSocketHandler;

public class HttpToWebSocketHandler implements HttpHandler {
    private final WebSocketHandler handler;

    public HttpToWebSocketHandler(WebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        control.upgradeToWebSocketConnection(handler);
    }
}
