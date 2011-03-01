package org.webbitserver.handler;

import org.webbitserver.*;
import org.webbitserver.CometHandler;

public class HttpToEventSourceHandler implements HttpHandler {
    private final CometHandler cometHandler;

    public HttpToEventSourceHandler(CometHandler cometHandler) {
        this.cometHandler = cometHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
        control.upgradeToEventSourceConnection(cometHandler);
    }
}
