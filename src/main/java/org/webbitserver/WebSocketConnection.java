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
     * Sends a binary frame
     *
     * @param message frame payload
     * @param offset  The offset within the array of the first byte to be written; must be non-negative and no larger than <code>message.length</code>
     * @param length  The maximum number of bytes to be written to the given array; must be non-negative and no larger than <code>message.length - offset</code>
     * @return this
     */
    WebSocketConnection send(byte[] message, int offset, int length);

    /**
     * Sends a ping frame
     *
     * @param message the payload of the ping
     * @return this
     */
    WebSocketConnection ping(byte[] message);

    /**
     * Sends a pong frame
     *
     * @param message the payload of the ping
     * @return this
     */
    WebSocketConnection pong(byte[] message);

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
