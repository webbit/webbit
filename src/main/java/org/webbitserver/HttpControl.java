package org.webbitserver;

import java.util.concurrent.Executor;

public interface HttpControl extends Executor {

    void nextHandler();

    void nextHandler(HttpRequest request, HttpResponse response);

    void nextHandler(HttpRequest request, HttpResponse response, HttpControl control);

    WebSocketConnection upgradeToWebSocketConnection(WebSocketHandler handler);

    WebSocketConnection webSocketConnection();

    EventSourceConnection upgradeToEventSourceConnection(EventSourceHandler handler);

    EventSourceConnection eventSourceConnection();

    Executor handlerExecutor();
}
