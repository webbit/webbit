package org.webbitserver.stub;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link EventSourceConnection} and {@link WebSocketConnection} that is easy to construct and
 * makes it easy to inspect results. Useful for testing.
 */
public class StubConnection extends StubDataHolder implements EventSourceConnection, WebSocketConnection {

    private final List<String> sentMessages = new LinkedList<String>();
    private final List<byte[]> sentBinaryMessages = new LinkedList<byte[]>();
    private final List<byte[]> sentPings = new LinkedList<byte[]>();
    private final List<byte[]> sentPongs = new LinkedList<byte[]>();
    private boolean closed = false;
    private HttpRequest httpRequest;
    private String version = null;

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
    public StubConnection send(org.webbitserver.EventSourceMessage message) {
        return send(message.build());
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
    public StubConnection send(byte[] message) {
        return send(message, 0, message.length);
    }

    @Override
    public StubConnection send(byte[] message, int offset, int length) {
        byte[] subMessage = new byte[length];
        System.arraycopy(message, offset, subMessage, 0, length);
        sentBinaryMessages.add(subMessage);
        return this;
    }

    @Override
    public StubConnection ping(byte[] message) {
        sentPings.add(message);
        return this;
    }

    @Override
    public StubConnection pong(byte[] message) {
        sentPongs.add(message);
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

    public List<byte[]> sentBinaryMessages() {
        return sentBinaryMessages;
    }

    public List<byte[]> sentPings() {
        return sentPings;
    }

    public List<byte[]> sentPongs() {
        return sentPongs;
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
    public String version() {
        return version;
    }

    public StubConnection version(String version) {
        this.version = version;
        return this;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
