package org.webbitserver;

import java.util.concurrent.Executor;

public interface HttpControl extends Executor {

    void nextHandler();

    void nextHandler(HttpRequest request, HttpResponse response);

    void nextHandler(HttpRequest request, HttpResponse response, HttpControl control);

    CometConnection upgradeToWebSocketConnection(CometHandler handler);

    CometConnection createWebSocketConnection();

    CometConnection upgradeToEventSourceConnection(CometHandler handler);

    CometConnection createEventSourceConnection();

    Executor handlerExecutor();
}
