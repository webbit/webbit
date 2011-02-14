package org.webbitserver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public interface WebSocketConnection extends Executor, DataHolder {
    HttpRequest httpRequest();
    WebSocketConnection send(String message);
    WebSocketConnection close();

    WebSocketConnection data(String key, Object value); // Override DataHolder to provide more specific return type.

    Executor handlerExecutor();

}
