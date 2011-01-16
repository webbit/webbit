package webbit.handler;

import webbit.HttpHandler;
import webbit.HttpRequest;
import webbit.HttpResponse;
import webbit.WebSocketHandler;

public class HttpToWebSocketHandler implements HttpHandler {

    private final WebSocketHandler webSocketHandler;

    public HttpToWebSocketHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response) {
        response.upgradeToWebSocketConnection(webSocketHandler);
    }
}
