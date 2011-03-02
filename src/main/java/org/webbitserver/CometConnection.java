package org.webbitserver;

import java.util.concurrent.Executor;

/**
 * A connection to a client that allows server push.
 */
public interface CometConnection extends Executor, DataHolder {
    String EVENT_SOURCE = "eventsource";
    String WEB_SOCKET = "websocket";

    HttpRequest httpRequest();

    CometConnection send(String message);

    CometConnection close();

    @Override
    CometConnection data(String key, Object value); // Override DataHolder to provide more specific return type.

    Executor handlerExecutor();

    String protocol();
}
