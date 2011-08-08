package org.webbitserver;

import java.util.concurrent.Executor;

public interface WebSocketConnection extends Executor, DataHolder {
    HttpRequest httpRequest();

    /**
     * Sends a text frame
     * 
     * @param message frame payload
     * @return this
     */
    WebSocketConnection send(String message);

    /**
     * Sends a binary frame
     * 
     * @param message frame payload
     * @return this
     */
    WebSocketConnection send(byte[] message);

    /**
     * Sends a ping frame
     * 
     * @param message the payload of the ping
     * @return this
     */
    WebSocketConnection ping(String message);

    WebSocketConnection close();

    @Override
    WebSocketConnection data(String key, Object value); // Override DataHolder to provide more specific return type.

    Executor handlerExecutor();
}
