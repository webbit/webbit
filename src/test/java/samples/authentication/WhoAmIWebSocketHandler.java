package samples.authentication;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.authentication.BasicAuthenticationHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * WebSocket handler that keeps track of whos connected and broadcasts to other users.
 */
public class WhoAmIWebSocketHandler implements WebSocketHandler {

    private final Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        String username = (String) connection.data(BasicAuthenticationHandler.USERNAME);
        connection.send("Hello " + username);

        for (WebSocketConnection otherConnection : connections) {
            otherConnection.send("You have been joined by " + username);
        }
        connections.add(connection);
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        String username = (String) connection.data(BasicAuthenticationHandler.USERNAME);
        connections.remove(connection);

        for (WebSocketConnection otherConnection : connections) {
            otherConnection.send(username + " has left");
        }
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
        // Do nothing
    }

    @Override
    public void onMessage(WebSocketConnection connection, byte[] msg) {
        // Do nothing
    }

    @Override
    public void onPing(WebSocketConnection connection, byte[] msg) throws Throwable {
        connection.pong(msg);
    }

    @Override
    public void onPong(WebSocketConnection connection, byte[] msg) {
        // Do nothing
    }
}
