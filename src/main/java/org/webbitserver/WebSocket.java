package org.webbitserver;

public interface WebSocket {
    WebSocket start();

    WebSocket close();

    void reconnectEvery(long millis);
}
