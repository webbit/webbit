package org.webbitserver.stub;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.netty.contrib.EventSourceMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link EventSourceConnection} and {@link WebSocketConnection} that is easy to construct and
 * makes it easy to inspect results. Useful for testing.
 */
public class StubConnection extends StubDataHolder implements EventSourceConnection, WebSocketConnection {

    private final List<String> sentMessages = new LinkedList<String>();
    private boolean closed = false;
    private HttpRequest httpRequest;

    public StubConnection(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public StubConnection() {
        this(new StubHttpRequest());
    }

    @Override
    public HttpRequest httpRequest() {
        return httpRequest;
    }

    @Override
    public StubConnection send(EventSourceMessage message) {
        return send(message.build()).send("\n");
    }

    public StubConnection httpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    @Override
    public StubConnection send(String message) {
        sentMessages.add(message);
        return this;
    }

    @Override
    public StubConnection close() {
        closed = true;
        return this;
    }

    public boolean closed() {
        return closed;
    }

    public List<String> sentMessages() {
        return sentMessages;
    }

    @Override
    public StubConnection data(String key, Object value) {
        super.data(key, value);
        return this;
    }

    @Override
    public Executor handlerExecutor() {
        return this;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
