package org.webbitserver.eventsource;

public interface EventSourceClientHandler {
    void onConnect();

    void onMessage(String event, MessageEvent message);

    void onDisconnect();

    void onError(Throwable t);
}
