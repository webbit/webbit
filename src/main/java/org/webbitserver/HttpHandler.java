package org.webbitserver;

public interface HttpHandler {
    void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception;
}
