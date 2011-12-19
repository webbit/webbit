package org.webbitserver.rest;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.weborganic.furi.URIPattern;
import org.weborganic.furi.URIResolveResult;
import org.weborganic.furi.URIResolver;

import java.net.URI;

class UriTemplateHandler implements HttpHandler {

    private final URIPattern uriPattern;
    private final HttpHandler httpHandler;
    public static final String RESOLVED_VARIABLES = "RESOLVED_VARIABLES";

    public UriTemplateHandler(String uriTemplate, HttpHandler httpHandler) {
        this.uriPattern = new URIPattern(uriTemplate);
        this.httpHandler = httpHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        String path = URI.create(request.uri()).getPath();
        URIResolver uriResolver = new URIResolver(path);
        URIResolveResult resolveResult = uriResolver.resolve(uriPattern);
        if (resolveResult.getStatus() == URIResolveResult.Status.RESOLVED) {
            request.data(RESOLVED_VARIABLES, resolveResult);
            httpHandler.handleHttpRequest(request, response, control);
        } else {
            control.nextHandler();
        }
    }
}
