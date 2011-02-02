package webbit;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

public interface HttpRequest {
    String uri();

    String header(String name);

    boolean hasHeader(String name);

    /**
     * HTTP method (e.g. "GET" or "POST")
     */
    String method();

    /**
     * Arbitrary data that can be stored for the lifetime of the request.
     */
    Map<String, Object> data();

    /**
     * Retrieve data value by key.
     *
     * @see #data()
     */
    Object data(String key);

    /**
     * Store data value by key.
     *
     * @see #data()
     */
    HttpRequest data(String key, Object value);

    /**
     * List data keys.
     *
     * @see #data()
     */
    Set<String> dataKeys();

    SocketAddress remoteAddress();
}
