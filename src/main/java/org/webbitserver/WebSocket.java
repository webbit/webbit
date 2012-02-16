package org.webbitserver;

public interface WebSocket extends Endpoint<WebSocket> {
    WebSocket reconnectEvery(long millis);
}
