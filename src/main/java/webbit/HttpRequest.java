package webbit;

import java.util.Map;

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

}
