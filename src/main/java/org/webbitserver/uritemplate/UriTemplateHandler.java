package org.webbitserver.uritemplate;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.wrapper.HttpRequestWrapper;
import org.weborganic.furi.URIPattern;
import org.weborganic.furi.URIResolveResult;
import org.weborganic.furi.URIResolver;

import java.net.URI;

public class UriTemplateHandler implements HttpHandler {

    private final URIPattern uriPattern;
    private final HttpHandler httpHandler;

    public UriTemplateHandler(String uriTemplate, HttpHandler httpHandler) {
        this.uriPattern = new URIPattern(uriTemplate);
        this.httpHandler = httpHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        UriPatternRequest uriPatternRequest = new UriPatternRequest(request);
        if (uriPatternRequest.matches()) {
            httpHandler.handleHttpRequest(uriPatternRequest, response, control);
        } else {
            control.nextHandler();
        }
    }

    private class UriPatternRequest extends HttpRequestWrapper {
        private URIResolveResult resolveResult;

        public UriPatternRequest(HttpRequest request) {
            super(request);
            String path = URI.create(request.uri()).getPath();
            URIResolver uriResolver = new URIResolver(path);
            resolveResult = uriResolver.resolve(uriPattern);
        }

        @Override
        public Object data(String key) {
            Object data = super.data(key);
            if (data == null) {
                data = resolveResult.get(key);
            }
            return data;
        }

        public boolean matches() {
            return resolveResult.getStatus() == URIResolveResult.Status.RESOLVED;
        }
    }
}
