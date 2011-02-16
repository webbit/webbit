package org.webbitserver;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

public interface HttpRequest extends DataHolder {

    String uri();

    /**
     * Retrieve the value single HTTP header.
     *
     * If the header is not found, null is returned.
     *
     * If there are multiple headers with the same name, it will return one of them, but it is not
     * defined which one. Instead, use {@link #headers(String)}.
     */
    String header(String name);

    /**
     * Retrieve all values for an HTTP header. If no values are found, an empty List is returned.
     */
    List<String> headers(String name);

    /**
     * Whether a specific HTTP header was present in the request.
     */
    boolean hasHeader(String name);

    /**
     * Returns all headers sent from client.
     */
    List<Map.Entry<String, String>> allHeaders();

    /**
     * HTTP method (e.g. "GET" or "POST")
     */
    String method();

    HttpRequest data(String key, Object value); // Override DataHolder to provide more specific return type.

    SocketAddress remoteAddress();

    /**
     * A unique identifier for this request. This should be treated as an opaque object,
     * that can be used to track the lifecycle of a request.
     */
    Object id();

    /**
     * Timestamp (millis since epoch) of when this request was first received by the server.
     */
    long timestamp();
}
