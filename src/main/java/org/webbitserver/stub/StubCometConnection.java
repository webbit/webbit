package org.webbitserver.stub;

import org.webbitserver.CometConnection;
import org.webbitserver.HttpRequest;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * Implementation of WebSocketConnection that is easy to construct, and inspect results.
 * Useful for testing.
 */
public class StubCometConnection extends StubDataHolder implements CometConnection {
    private final List<String> sentMessages = new LinkedList<String>();
    private boolean closed = false;
    private HttpRequest httpRequest;

    public StubCometConnection(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public StubCometConnection() {
        this(new StubHttpRequest());
    }

    @Override
    public HttpRequest httpRequest() {
        return httpRequest;
    }

    public StubCometConnection httpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    @Override
    public StubCometConnection send(String message) {
        sentMessages.add(message);
        return this;
    }

    @Override
    public StubCometConnection close() {
        closed = true;
        return this;
    }

    public boolean closed() {
        return closed;
    }

    @Override
    public StubCometConnection data(String key, Object value) {
        super.data(key, value);
        return this;
    }

    @Override
    public Executor handlerExecutor() {
        return this;
    }

    @Override
    public String protocol() {
        return "cometstub";
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
