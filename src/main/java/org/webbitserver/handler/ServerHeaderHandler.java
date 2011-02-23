package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 * Handler that sets the HTTP 'Server' response header.
 */
public class ServerHeaderHandler implements HttpHandler {

    private final String value;

    /**
     * Value to set for HTTP Server header, or null to ensure the header is blank.
     */
    public ServerHeaderHandler(String value) {
        this.value = value;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        response.header("Server", value);
        control.nextHandler();
    }
}
