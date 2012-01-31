package org.webbitserver;

import java.io.InputStream;

public interface WebSocket {
    WebSocket start();

    WebSocket close();

    WebSocket reconnectEvery(long millis);

    WebSocket setupSsl(InputStream resourceAsStream, String webbit);
}
