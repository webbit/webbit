package webbit.wrapper;

import webbit.HttpRequest;

import java.util.Map;

public class HttpRequestWrapper implements HttpRequest {

    private HttpRequest request;

    public HttpRequestWrapper(HttpRequest request) {
        this.request = request;
    }

    public HttpRequest underlyingRequest() {
        return request;
    }

    public HttpRequestWrapper underlyingRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    public HttpRequest originalRequest() {
        if (request instanceof HttpRequestWrapper) {
            HttpRequestWrapper wrapper = (HttpRequestWrapper) request;
            return wrapper.originalRequest();
        } else {
            return request;
        }
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public String header(String name) {
        return request.header(name);
    }

    @Override
    public boolean hasHeader(String name) {
        return request.hasHeader(name);
    }

    @Override
    public String method() {
        return request.method();
    }

    @Override
    public Map<String, Object> data() {
        return request.data();
    }
}
