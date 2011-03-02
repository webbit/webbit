package samples.chatroom;

import com.google.gson.Gson;
import org.webbitserver.CometConnection;
import org.webbitserver.CometHandler;

import java.util.HashSet;
import java.util.Set;

public class Chatroom implements CometHandler {

    private final Gson json = new Gson();

    public static final String USERNAME_KEY = "username";

    static class Incoming {
        enum Action {LOGIN, SAY}

        Action action;
        String loginUsername;
        String message;
    }

    static class Outgoing {
        enum Action {JOIN, LEAVE, SAY}

        Action action;
        String username;
        String message;
    }

    private Set<CometConnection> connections = new HashSet<CometConnection>();

    @Override
    public void onOpen(CometConnection connection) throws Exception {
        connections.add(connection);
    }

    @Override
    public void onMessage(CometConnection connection, String msg) throws Exception {
        Incoming incoming = json.fromJson(msg, Incoming.class);
        switch (incoming.action) {
            case LOGIN:
                login(connection, incoming.loginUsername);
                break;
            case SAY:
                say(connection, incoming.message);
                break;
        }
    }

    private void login(CometConnection connection, String username) {
        connection.data(USERNAME_KEY, username); // associate username with connection

        Outgoing outgoing = new Outgoing();
        outgoing.action = Outgoing.Action.JOIN;
        outgoing.username = username;
        broadcast(outgoing);
    }

    private void say(CometConnection connection, String message) {
        String username = (String) connection.data(USERNAME_KEY);
        if (username != null) {
            Outgoing outgoing = new Outgoing();
            outgoing.action = Outgoing.Action.SAY;
            outgoing.username = username;
            outgoing.message = message;
            broadcast(outgoing);
        }
    }

    private void broadcast(Outgoing outgoing) {
        String jsonStr = this.json.toJson(outgoing);
        for (CometConnection connection : connections) {
            if (connection.data(USERNAME_KEY) != null) { // only broadcast to those who have completed login
                connection.send(jsonStr);
            }
        }
    }

    @Override
    public void onClose(CometConnection connection) throws Exception {
        String username = (String) connection.data(USERNAME_KEY);
        if (username != null) {
            Outgoing outgoing = new Outgoing();
            outgoing.action = Outgoing.Action.LEAVE;
            outgoing.username = username;
            broadcast(outgoing);
        }
        connections.remove(connection);
    }

}
