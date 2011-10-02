package org.webbitserver.wrapper;

import org.webbitserver.HttpRequest;

import java.net.HttpCookie;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public HttpRequestWrapper uri(String uri) {
        request.uri(uri);
        return this;
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
    public List<HttpCookie> cookies() {
        return request.cookies();
    }

    @Override
    public HttpCookie cookie(String name) {
        return request.cookie(name);
    }

    @Override
    public String queryParam(String key) {
        return request.queryParam(key);
    }

    @Override
    public List<String> queryParams(String key) {
        return request.queryParams(key);
    }

    @Override
    public Set<String> queryParamKeys() {
        return request.queryParamKeys();
    }

    @Override
    public String postParam(String key) {
        return request.postParam(key);
    }

    @Override
    public List<String> postParams(String key) {
        return request.postParams(key);
    }

    @Override
    public Set<String> postParamKeys() {
        return request.postParamKeys();
    }

    @Override
    public String cookieValue(String name) {
        return request.cookieValue(name);
    }

    @Override
    public List<String> headers(String name) {
        return request.headers(name);
    }

    @Override
    public List<Map.Entry<String, String>> allHeaders() {
        return request.allHeaders();
    }

    @Override
    public String method() {
        return request.method();
    }

    @Override
    public String body() {
        return request.body();
    }

    @Override
    public byte[] bodyAsBytes() {
        return request.bodyAsBytes();
    }

    @Override
    public Map<String, Object> data() {
        return request.data();
    }

    @Override
    public Object data(String key) {
        return request.data(key);
    }

    @Override
    public HttpRequestWrapper data(String key, Object value) {
        request.data(key, value);
        return this;
    }

    @Override
    public Set<String> dataKeys() {
        return request.dataKeys();
    }

    @Override
    public SocketAddress remoteAddress() {
        return request.remoteAddress();
    }

    @Override
    public Object id() {
        return request.id();
    }

    @Override
    public long timestamp() {
        return request.timestamp();
    }
}
