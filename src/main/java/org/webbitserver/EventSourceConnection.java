package org.webbitserver;

import org.webbitserver.netty.contrib.EventSourceMessage;

import java.util.concurrent.Executor;

public interface EventSourceConnection extends Executor, DataHolder {
    HttpRequest httpRequest();

    EventSourceConnection send(EventSourceMessage message);

    EventSourceConnection close();

    @Override
    EventSourceConnection data(String key, Object value); // Override DataHolder to provide more specific return type.

    Executor handlerExecutor();
}
