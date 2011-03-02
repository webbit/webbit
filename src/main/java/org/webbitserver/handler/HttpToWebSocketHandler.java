package org.webbitserver.handler;

import org.webbitserver.*;

public class HttpToWebSocketHandler implements HttpHandler {
    private final CometHandler webSocketHandler;

    public HttpToWebSocketHandler(CometHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
        control.upgradeToWebSocketConnection(webSocketHandler);
    }
}
