package webbit;

import java.util.Map;

public interface WebSocketConnection {
    HttpRequest httpRequest();
    WebSocketConnection send(String message);
    WebSocketConnection close();

    /**
     * Arbitrary data that can be stored for the lifetime of the connection.
     */
    Map<String, Object> data();

}
