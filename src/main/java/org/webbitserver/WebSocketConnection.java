package org.webbitserver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public interface WebSocketConnection extends Executor {
    HttpRequest httpRequest();
    WebSocketConnection send(String message);
    WebSocketConnection close();

    /**
     * Arbitrary data that can be stored for the lifetime of the connection.
     */
    Map<String, Object> data();

    /**
     * Retrieve data value by key.
     *
     * @see #data()
     */
    Object data(String key);

    /**
     * Store data value by key.
     *
     * @see #data()
     */
    WebSocketConnection data(String key, Object value);

    /**
     * List data keys.
     *
     * @see #data()
     */
    Set<String> dataKeys();

    Executor handlerExecutor();

}
