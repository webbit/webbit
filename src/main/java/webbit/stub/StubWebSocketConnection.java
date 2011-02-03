package webbit.stub;

import webbit.HttpRequest;
import webbit.WebSocketConnection;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * Implementation of WebSocketConnection that is easy to construct, and inspect results.
 * Useful for testing.
 */
public class StubWebSocketConnection implements WebSocketConnection {
    private final Map<String, Object> data = new HashMap<String, Object>();
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

    @Override
    public Map<String, Object> data() {
        return data;
    }

    @Override
    public Object data(String key) {
        return data().get(key);
    }

    @Override
    public StubWebSocketConnection data(String key, Object value) {
        data().put(key, value);
        return this;
    }

    @Override
    public Set<String> dataKeys() {
        return data().keySet();
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
