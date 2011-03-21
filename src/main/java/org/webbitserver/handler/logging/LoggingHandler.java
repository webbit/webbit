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
import org.webbitserver.wrapper.WebSocketConnectionWrapper;

public class LoggingHandler implements HttpHandler {

    private final LogSink logSink;

    public LoggingHandler(LogSink logSink) {
        this.logSink = logSink;
    }

    public LogSink logSink() {
        return logSink;
    }

    @Override
    public void handleHttpRequest(final HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        logSink.httpStart(request);

        HttpResponseWrapper responseWrapper = new HttpResponseWrapper(response) {
            @Override
            public HttpResponseWrapper end() {
                logSink.httpEnd(request);
                return super.end();
            }

            @Override
            public HttpResponseWrapper error(Throwable error) {
                logSink.httpEnd(request);
                logSink.error(request, error);
                return super.error(error);
            }
        };

        HttpControlWrapper controlWrapper = new HttpControlWrapper(control) {

            private LoggingWebSocketConnection loggingWebSocketConnection;

            @Override
            public WebSocketConnection createWebSocketConnection() {
                return loggingWebSocketConnection;
            }

            @Override
            public WebSocketConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
                loggingWebSocketConnection = new LoggingWebSocketConnection(super.createWebSocketConnection());
                return super.upgradeToWebSocketConnection(
                        new LoggingWebSocketHandler(loggingWebSocketConnection, handler));
            }

            @Override
            public EventSourceConnection createEventSourceConnection() {
                throw new UnsupportedOperationException();
//                return loggingPushConnection;
            }

            @Override
            public EventSourceConnection upgradeToEventSourceConnection(EventSourceHandler handler) {
                throw new UnsupportedOperationException();
//                loggingPushConnection = new LoggingWebSocketConnection(super.createEventSourceConnection());
//                return super.upgradeToEventSourceConnection(
//                        new LoggingWebSocketHandler(loggingPushConnection, handler));
            }
        };
        control.nextHandler(request, responseWrapper, controlWrapper);
    }


    private class LoggingWebSocketConnection extends WebSocketConnectionWrapper {
        LoggingWebSocketConnection(WebSocketConnection connection) {
            super(connection);
        }

        @Override
        public WebSocketConnectionWrapper send(String message) {
            logSink.webSocketOutboundData(this, message);
            return super.send(message);
        }

    }

    private class LoggingWebSocketHandler implements WebSocketHandler {

        private final WebSocketConnection loggingConnection;
        private final WebSocketHandler handler;

        LoggingWebSocketHandler(WebSocketConnection loggingConnection, WebSocketHandler handler) {
            this.loggingConnection = loggingConnection;
            this.handler = handler;
        }

        @Override
        public void onOpen(WebSocketConnection connection) throws Exception {
            logSink.webSocketConnectionOpen(connection);
            handler.onOpen(loggingConnection);
        }

        @Override
        public void onClose(WebSocketConnection connection) throws Exception {
            logSink.webSocketConnectionClose(connection);
            logSink.httpEnd(connection.httpRequest());
            handler.onClose(loggingConnection);
        }

        @Override
        public void onMessage(WebSocketConnection connection, String message) throws Exception {
            logSink.webSocketInboundData(connection, message);
            handler.onMessage(loggingConnection, message);
        }
    }

}
