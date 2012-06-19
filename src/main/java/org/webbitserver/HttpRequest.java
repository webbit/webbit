package org.webbitserver;

import java.net.HttpCookie;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Retrieves information about inbound HTTP request.
 *
 * @author Joe Walnes
 * @see HttpHandler
 * @see HttpResponse
 */
public interface HttpRequest extends DataHolder {

    public String COOKIE_HEADER = "Cookie";

    String uri();

    /**
     * Modify uri
     *
     * @param uri new uri
     */
    HttpRequest uri(String uri);

    /**
     * Retrieve the value single HTTP header.
     * <p/>
     * If the header is not found, null is returned.
     * <p/>
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
     * @return all inbound cookies
     */
    List<HttpCookie> cookies();

    /**
     * Get a cookie with a specific name
     *
     * @param name cookie name
     * @return cookie with that name
     */
    HttpCookie cookie(String name);

    /**
     * Get query parameter value.
     *
     * @param key parameter name
     * @return the value of the parameter
     * @see #queryParams(String)
     */
    String queryParam(String key);

    /**
     * Get all query parameter values.
     *
     * @param key parameter name
     * @return the values of the parameter
     * @see #queryParam(String)
     */
    List<String> queryParams(String key);

    /**
     * List all query parameter keys.
     *
     * @see #queryParam(String)
     */
    Set<String> queryParamKeys();

    /**
     * Get post parameter value.
     *
     * @param key parameter name
     * @return the value of the parameter
     * @see #postParams(String)
     */
    String postParam(String key);

    /**
     * Get all post parameter values.
     *
     * @param key parameter name
     * @return the values of the parameter
     * @see #postParam(String)
     */
    List<String> postParams(String key);

    /**
     * List all post parameter keys.
     *
     * @see #postParam(String)
     */
    Set<String> postParamKeys();

    /**
     * Get the value of named cookie
     *
     * @param name cookie name
     * @return cookie value, or null if the cookie does not exist.
     */
    String cookieValue(String name);

    /**
     * Returns all headers sent from client.
     */
    List<Map.Entry<String, String>> allHeaders();

    /**
     * HTTP method (e.g. "GET" or "POST")
     */
    String method();

    /**
     * The body
     */
    String body();

    /**
     * The body's byte array
     */
    byte[] bodyAsBytes();

    @Override
    HttpRequest data(String key, Object value); // Override DataHolder to provide more specific return type.

    /**
     * Remote address of connection (i.e. the host of the client).
     */
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
