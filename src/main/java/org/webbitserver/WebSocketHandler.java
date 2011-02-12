package org.webbitserver;

public interface WebSocketHandler {
    void onOpen(WebSocketConnection connection) throws Exception;
    void onMessage(WebSocketConnection connection, String msg) throws Exception;
    void onClose(WebSocketConnection connection) throws Exception;
}
