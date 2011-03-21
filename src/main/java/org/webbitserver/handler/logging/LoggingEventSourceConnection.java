package org.webbitserver.handler.logging;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.wrapper.EventSourceConnectionWrapper;

class LoggingEventSourceConnection extends EventSourceConnectionWrapper {

    private final LogSink logSink;

    LoggingEventSourceConnection(LogSink logSink, EventSourceConnection connection) {
        super(connection);
        this.logSink = logSink;
    }

    @Override
    public EventSourceConnectionWrapper send(String message) {
        logSink.eventSourceOutboundData(this, message);
        return super.send(message);
    }

}
