package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class AliasHandler implements HttpHandler {
    private final String uri;

    public AliasHandler(String uri) {
        this.uri = uri;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        request.uri(uri);
        control.nextHandler(request, response);
    }
}
