package org.webbitserver;

public interface WebSocketConnection extends HttpConnection {

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

    /**
     * @return the WebSocket protocol version
     */
    String version();

    // Override methods to provide more specific return type.

    @Override
    WebSocketConnection close();

    @Override
    WebSocketConnection data(String key, Object value);
}
