package org.webbitserver.stub;

import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;

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
