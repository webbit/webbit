package org.webbitserver.stub;

import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Implementation of WebSocketConnection that is easy to construct, and inspect results.
 * Useful for testing.
 */
public class StubWebSocketConnection extends StubDataHolder implements WebSocketConnection {
    private final List<String> sentMessages = new LinkedList<String>();
    private boolean closed = false;
    private HttpRequest httpRequest;

    public StubWebSocketConnection(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public StubWebSocketConnection() {
        this(new StubHttpRequest());
    }

    @Override
    public HttpRequest httpRequest() {
        return httpRequest;
    }

    public StubWebSocketConnection httpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    @Override
    public StubWebSocketConnection send(String message) {
        sentMessages.add(message);
        return this;
    }

    @Override
    public StubWebSocketConnection close() {
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
    public StubWebSocketConnection data(String key, Object value) {
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
