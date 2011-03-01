package org.webbitserver;

import java.util.concurrent.Executor;

public interface HttpControl extends Executor {

    void nextHandler();

    void nextHandler(HttpRequest request, HttpResponse response);

    void nextHandler(HttpRequest request, HttpResponse response, HttpControl control);

    CometConnection upgradeToWebSocketConnection(WebSocketHandler handler);

    CometConnection createWebSocketConnection();

    CometConnection upgradeToEventSourceConnection(CometHandler handler);

    Executor handlerExecutor();
}
