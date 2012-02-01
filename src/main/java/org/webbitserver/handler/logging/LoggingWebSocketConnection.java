package org.webbitserver.handler.logging;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.wrapper.WebSocketConnectionWrapper;

class LoggingWebSocketConnection extends WebSocketConnectionWrapper {

    private final LogSink logSink;

    LoggingWebSocketConnection(LogSink logSink, WebSocketConnection connection) {
        super(connection);
        this.logSink = logSink;
    }

    @Override
    public WebSocketConnectionWrapper send(String message) {
        logSink.webSocketOutboundData(this, message);
        return super.send(message);
    }

    @Override
    public WebSocketConnectionWrapper send(byte[] message) {
        logSink.webSocketOutboundData(this, message);
        return super.send(message);
    }

    @Override
    public WebSocketConnectionWrapper ping(byte[] msg) {
        logSink.webSocketOutboundPing(this, msg);
        return super.ping(msg);
    }

    @Override
    public WebSocketConnectionWrapper pong(byte[] msg) {
        logSink.webSocketOutboundPong(this, msg);
        return super.pong(msg);
    }
}
