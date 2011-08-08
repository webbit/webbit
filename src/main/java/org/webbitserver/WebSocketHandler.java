package org.webbitserver;

public interface WebSocketHandler {
    void onOpen(WebSocketConnection connection) throws Exception;

    void onClose(WebSocketConnection connection) throws Exception;

    void onMessage(WebSocketConnection connection, String msg) throws Throwable;

    void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable;

    void onPong(WebSocketConnection connection, String msg) throws Throwable;
}
