package org.webbitserver.handler.logging;

import org.webbitserver.*;
import org.webbitserver.wrapper.CometConnectionWrapper;
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

            private LoggingCometConnection loggingCometConnection;

            @Override
            public CometConnection createWebSocketConnection() {
                return loggingCometConnection;
            }

            @Override
            public CometConnection upgradeToWebSocketConnection(CometHandler handler) {
                loggingCometConnection = new LoggingCometConnection(super.createWebSocketConnection());
                return super.upgradeToWebSocketConnection(
                        new LoggingCometHandler(loggingCometConnection, handler));
            }

            @Override
            public CometConnection createEventSourceConnection() {
                return loggingCometConnection;
            }

            @Override
            public CometConnection upgradeToEventSourceConnection(CometHandler handler) {
                loggingCometConnection = new LoggingCometConnection(super.createEventSourceConnection());
                return super.upgradeToEventSourceConnection(
                        new LoggingCometHandler(loggingCometConnection, handler));
            }
        };
        control.nextHandler(request, responseWrapper, controlWrapper);
    }


    private class LoggingCometConnection extends CometConnectionWrapper {

        LoggingCometConnection(CometConnection connection) {
            super(connection);
        }

        @Override
        public CometConnectionWrapper send(String message) {
            logSink.cometOutboundData(this, message);
            return super.send(message);
        }

    }

    private class LoggingCometHandler implements CometHandler {

        private final CometConnection loggingConnection;
        private final CometHandler handler;

        LoggingCometHandler(CometConnection loggingConnection, CometHandler handler) {
            this.loggingConnection = loggingConnection;
            this.handler = handler;
        }

        @Override
        public void onOpen(CometConnection connection) throws Exception {
            logSink.cometConnectionOpen(connection);
            handler.onOpen(loggingConnection);
        }

        @Override
        public void onClose(CometConnection connection) throws Exception {
            logSink.cometConnectionClose(connection);
            logSink.httpEnd(connection.httpRequest());
            handler.onClose(loggingConnection);
        }

        @Override
        public void onMessage(CometConnection connection, String message) throws Exception {
            logSink.webSocketInboundData(connection, message);
            handler.onMessage(loggingConnection, message);
        }
    }

}
