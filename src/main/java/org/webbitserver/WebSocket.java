package org.webbitserver;

import java.util.concurrent.Future;

public interface WebSocket {
    Future<WebSocket> start();

    WebSocket close();

    void reconnectEvery(long millis);
}
