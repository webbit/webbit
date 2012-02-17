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
    public WebSocketConnectionWrapper send(byte[] message, int offset, int length) {
        byte[] sent = new byte[length];
        System.arraycopy(message, offset, sent, 0, length);
        logSink.webSocketOutboundData(this, sent);
        return super.send(message, offset, length);
    }

    @Override
    public WebSocketConnectionWrapper ping(byte[] message) {
        logSink.webSocketOutboundPing(this, message);
        return super.ping(message);
    }

    @Override
    public WebSocketConnectionWrapper pong(byte[] message) {
        logSink.webSocketOutboundPong(this, message);
        return super.pong(message);
    }
}
