package org.webbitserver.handler.logging;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

class LoggingWebSocketHandler implements WebSocketHandler {

    private final LogSink logSink;
    private final WebSocketConnection loggingConnection;
    private final WebSocketHandler handler;

    LoggingWebSocketHandler(LogSink logSink, WebSocketConnection loggingConnection, WebSocketHandler handler) {
        this.logSink = logSink;
        this.loggingConnection = loggingConnection;
        this.handler = handler;
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Throwable {
        logSink.webSocketConnectionOpen(connection);
        handler.onOpen(loggingConnection);
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Throwable {
        logSink.webSocketConnectionClose(connection);
        logSink.httpEnd(connection.httpRequest(), null);
        handler.onClose(loggingConnection);
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) throws Throwable {
        logSink.webSocketInboundData(connection, message);
        handler.onMessage(loggingConnection, message);
    }

    @Override
    public void onMessage(WebSocketConnection connection, byte[] message) throws Throwable {
        logSink.webSocketInboundData(connection, message);
        handler.onMessage(loggingConnection, message);
    }

    @Override
    public void onPing(WebSocketConnection connection, byte[] message) throws Throwable {
        logSink.webSocketInboundPing(connection, message);
        handler.onPing(loggingConnection, message);
    }

    @Override
    public void onPong(WebSocketConnection connection, byte[] message) throws Throwable {
        logSink.webSocketInboundPong(connection, message);
        handler.onPong(loggingConnection, message);
    }
}
