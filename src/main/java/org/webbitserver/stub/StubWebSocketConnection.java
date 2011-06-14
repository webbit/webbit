package org.webbitserver.stub;

import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Implementation of WebSocketConnection that is easy to construct, and inspect results.
 * Useful for testing.
 *
 * @deprecated use {@link StubConnection}
 */
@Deprecated
public class StubWebSocketConnection extends StubConnection {
    public StubWebSocketConnection(HttpRequest httpRequest) {
        super(httpRequest);
    }

    public StubWebSocketConnection() {
        super();
    }
}
