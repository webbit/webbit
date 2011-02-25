package org.webbitserver;

public interface EventSourceHandler {
    void onOpen(EventSourceConnection connection) throws Exception;
}
