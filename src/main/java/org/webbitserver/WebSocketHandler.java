package org.webbitserver;

import org.webbitserver.netty.NettyWebSocketConnection;

public interface WebSocketHandler {
    void onOpen(WebSocketConnection connection) throws Exception;

    /**
     * Called when a connection is closed.
     *
     * @param connection the connection that was closed. Beware that the connection will be null if this handler is used in a {@link WebSocket} that fails to connect.
     * @throws Exception
     */
    void onClose(WebSocketConnection connection) throws Exception;

    void onMessage(WebSocketConnection connection, String msg) throws Throwable;

    void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable;

    void onPing(WebSocketConnection connection, byte[] msg) throws Throwable;

    void onPong(WebSocketConnection connection, byte[] msg) throws Throwable;
}
