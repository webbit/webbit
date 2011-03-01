package org.webbitserver.handler.logging;

import org.webbitserver.*;
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
            public CometConnection createWebSocketConnection() {
                return loggingWebSocketConnection;
            }

            @Override
            public CometConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
                loggingWebSocketConnection = new LoggingWebSocketConnection(super.createWebSocketConnection());
                return super.upgradeToWebSocketConnection(
                        new LoggingWebSocketHandler(loggingWebSocketConnection, handler));
            }
        };
        control.nextHandler(request, responseWrapper, controlWrapper);
    }


    private class LoggingWebSocketConnection extends WebSocketConnectionWrapper {

        LoggingWebSocketConnection(CometConnection connection) {
            super(connection);
        }

        @Override
        public WebSocketConnectionWrapper send(String message) {
            logSink.webSocketOutboundData(this, message);
            return super.send(message);
        }

    }

    private class LoggingWebSocketHandler implements WebSocketHandler {

        private final CometConnection loggingConnection;
        private final WebSocketHandler handler;

        LoggingWebSocketHandler(CometConnection loggingConnection, WebSocketHandler handler) {
            this.loggingConnection = loggingConnection;
            this.handler = handler;
        }

        @Override
        public void onOpen(CometConnection connection) throws Exception {
            logSink.webSocketOpen(connection);
            handler.onOpen(loggingConnection);
        }

        @Override
        public void onMessage(CometConnection connection, String message) throws Exception {
            logSink.webSocketInboundData(connection, message);
            handler.onMessage(loggingConnection, message);
        }

        @Override
        public void onClose(CometConnection connection) throws Exception {
            logSink.webSocketClose(connection);
            logSink.httpEnd(connection.httpRequest());
            handler.onClose(loggingConnection);
        }

    }

}
