package org.webbitserver.handler;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.Date;

/**
 * Handler that sets the HTTP 'Server' response header.
 */
public class DateHeaderHandler implements HttpHandler {

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        if (!response.containsHeader(HttpHeaders.Names.DATE)) {
            response.header(HttpHeaders.Names.DATE, new Date());
        }
        control.nextHandler();
    }
}
