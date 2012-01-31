package org.webbitserver;

public interface EventSourceConnection extends HttpConnection {
    EventSourceConnection send(EventSourceMessage message);

    // Override methods to provide more specific return type.

    @Override
    EventSourceConnection close();

    @Override
    EventSourceConnection data(String key, Object value);
}
