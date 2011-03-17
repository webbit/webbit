package org.webbitserver.eventsource;

public interface EventSourceHandler {
    void onConnect();
    void onMessage(String event, MessageEvent message);
    void onDisconnect();
    void onError(Throwable t);
}
