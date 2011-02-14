package org.webbitserver;

import java.net.SocketAddress;

public interface HttpRequest extends DataHolder {
    String uri();

    String header(String name);

    boolean hasHeader(String name);

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
