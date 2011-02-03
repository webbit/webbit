package webbit.stub;

import webbit.HttpRequest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of HttpRequest that is easy to construct manually and populate.
 * Useful for testing.
 */
public class StubHttpRequest implements HttpRequest {

    private String uri = "/";
    private String method = "GET";
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, Object> data = new HashMap<String, Object>();
    private SocketAddress remoteAddress = new InetSocketAddress("localhost", 0);
    private Object id = "StubID";
    private long timestamp = 0;

    public StubHttpRequest() {
    }

    public StubHttpRequest(String uri) {
        this.uri = uri;
    }

    @Override
    public String uri() {
        return uri;
    }

    public StubHttpRequest uri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String header(String name) {
        return headers.get(name);
    }

    @Override
    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String method() {
        return method;
    }

    public StubHttpRequest method(String method) {
        this.method = method;
        return this;
    }

    public StubHttpRequest header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }

    @Override
    public Object data(String key) {
        return data().get(key);
    }

    @Override
    public StubHttpRequest data(String key, Object value) {
        data().put(key, value);
        return this;
    }

    @Override
    public Set<String> dataKeys() {
        return data().keySet();
    }

    @Override
    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public Object id() {
        return id;
    }

    public StubHttpRequest id(Object id) {
        this.id = id;
        return this;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    public StubHttpRequest timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public StubHttpRequest remoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }
}
