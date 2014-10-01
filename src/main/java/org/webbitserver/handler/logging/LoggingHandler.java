package org.webbitserver.handler.logging;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.wrapper.HttpControlWrapper;
import org.webbitserver.wrapper.HttpResponseWrapper;

public class LoggingHandler implements HttpHandler {

    private final LogSink logSink;

    public LoggingHandler(LogSink logSink) {
        this.logSink = logSink;
    }

    public LogSink logSink() {
        return logSink;
    }

    @Override
    public void handleHttpRequest(final HttpRequest request, final HttpResponse response, HttpControl control) throws Exception {
        logSink.httpStart(request);

        HttpResponseWrapper responseWrapper = new HttpResponseWrapper(response) {
            @Override
            public HttpResponseWrapper end() {
                logSink.httpEnd(request, response);
                return super.end();
            }

            @Override
            public HttpResponseWrapper error(Throwable error) {
                logSink.httpEnd(request, response);
                logSink.error(request, error);
                return super.error(error);
            }
        };

        HttpControlWrapper controlWrapper = new HttpControlWrapper(control) {

            private LoggingWebSocketConnection loggingWebSocketConnection;
            private LoggingEventSourceConnection loggingEventSourceConnection;

            @Override
            public WebSocketConnection webSocketConnection() {
                return loggingWebSocketConnection;
            }

            @Override
            public WebSocketConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
                loggingWebSocketConnection = new LoggingWebSocketConnection(logSink, super.webSocketConnection());
                return super.upgradeToWebSocketConnection(new LoggingWebSocketHandler(logSink, loggingWebSocketConnection, handler));
            }

            @Override
            public EventSourceConnection eventSourceConnection() {
                return loggingEventSourceConnection;
            }

            @Override
            public EventSourceConnection upgradeToEventSourceConnection(EventSourceHandler handler) {
                loggingEventSourceConnection = new LoggingEventSourceConnection(logSink, super.eventSourceConnection());
                return super.upgradeToEventSourceConnection(new LoggingEventSourceHandler(logSink, loggingEventSourceConnection, handler));
            }
        };
        control.nextHandler(request, responseWrapper, controlWrapper);
    }


}
