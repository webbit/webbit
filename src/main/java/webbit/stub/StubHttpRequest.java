package webbit.stub;

import webbit.HttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of HttpRequest that is easy to construct manually and populate.
 * Useful for testing.
 */
public class StubHttpRequest implements HttpRequest {

    private String uri = "/";
    private String method = "GET";
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, Object> data = new HashMap<String, Object>();

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
}
