package webbit.handler;

import webbit.*;

public class HttpToWebSocketHandler implements HttpHandler {

    private final WebSocketHandler webSocketHandler;

    public HttpToWebSocketHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
        control.upgradeToWebSocketConnection(webSocketHandler);
    }
}
