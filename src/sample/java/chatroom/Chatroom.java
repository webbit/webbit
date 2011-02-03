package chatroom;

import com.google.gson.Gson;
import webbit.WebSocketConnection;
import webbit.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

public class Chatroom implements WebSocketHandler {

    private final Gson json = new Gson();

    static class Incoming {
        enum Action { LOGIN, SAY }
        Action action;
        String loginUsername;
        String message;
    }

    static class Outgoing {
        enum Action { JOIN, LEAVE, SAY }
        Action action;
        String username;
        String message;
    }

    private Map<WebSocketConnection, String> usernames = new HashMap<WebSocketConnection, String>();

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        // Don't do anything until we receive a LOGIN message.
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
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

    private void login(WebSocketConnection connection, String username) {
        usernames.put(connection, username);
        Outgoing outgoing = new Outgoing();
        outgoing.action = Outgoing.Action.JOIN;
        outgoing.username = username;
        broadcast(outgoing);
    }

    private void say(WebSocketConnection connection, String message) {
        String username = usernames.get(connection);
        if (username != null) {
            Outgoing outgoing = new Outgoing();
            outgoing.action = Outgoing.Action.SAY;
            outgoing.username = username;
            outgoing.message = message;
            broadcast(outgoing);
        }
    }

    private void broadcast(Outgoing outgoing) {
        String json = this.json.toJson(outgoing);
        for (WebSocketConnection connection : usernames.keySet()) {
            connection.send(json);
        }
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        String username = usernames.get(connection);
        if (username != null) {
            Outgoing outgoing = new Outgoing();
            outgoing.action = Outgoing.Action.LEAVE;
            outgoing.username = username;
            broadcast(outgoing);
            usernames.remove(connection);
        }
    }

    @Override
    public void onError(WebSocketConnection connection, Exception error) throws Exception {
        error.printStackTrace();
    }

}
