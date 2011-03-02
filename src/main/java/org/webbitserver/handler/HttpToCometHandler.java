package org.webbitserver.handler;

import org.webbitserver.CometHandler;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class HttpToCometHandler implements HttpHandler {
    private final CometHandler cometHandler;

    public HttpToCometHandler(CometHandler cometHandler) {
        this.cometHandler = cometHandler;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        if(isEventSource(request)) {
            control.upgradeToEventSourceConnection(cometHandler);
        } else if(isWebsocketUpgrade(request)) {
            control.upgradeToWebSocketConnection(cometHandler);
        } else {
            // Try EventSource then (Opera supports ES. not WS, but doesn't send the right Accept header...)
            control.upgradeToEventSourceConnection(cometHandler);
        }
    }

    private boolean isEventSource(HttpRequest request) {
        return "text/event-stream".equals(request.header("Accept"));
    }

    private boolean isWebsocketUpgrade(HttpRequest request) {
        return "Upgrade".equalsIgnoreCase(request.header("Connection")) &&
                "WebSocket".equalsIgnoreCase(request.header("Upgrade"));
    }


}
