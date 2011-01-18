package chatroom;

import org.jetlang.fibers.ThreadFiber;
import webbit.WebServer;
import webbit.WebSocketConnection;
import webbit.WebSocketHandler;
import webbit.netty.NettyWebServer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static webbit.route.Route.*;

public class Chatroom implements WebSocketHandler {

    private Map<WebSocketConnection, String> usernames = new HashMap<WebSocketConnection, String>();

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        // Don't do anything until we receive a LOGIN message.
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
        String[] tokens = msg.split("\\|");
        if (tokens.length == 0) {
            return;
        } else if (tokens[0].equals("LOGIN") && tokens.length == 2) {
            String username = tokens[1];
            usernames.put(connection, username);
            broadcast("* User '" + username + "' has entered.");
        } else if (tokens[0].equals("SAY") && tokens.length == 2) {
            String message = tokens[1];
            String username = usernames.get(connection);
            if (username != null) {
                broadcast("[" + username + "] " + message);
            }
        }
    }

    private void broadcast(String text) {
        System.out.println(text);
        for (WebSocketConnection connection : usernames.keySet()) {
            connection.send(text);
        }
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        broadcast("* User '" + usernames.get(connection) + "' has left.");
        usernames.remove(connection);
    }

    public static void main(String[] args) throws Exception {
        ThreadFiber fiber = new ThreadFiber();

        WebServer webServer = new NettyWebServer(fiber, 9876, route(
                directory(fiber, "./src/sample/java/chatroom/content"),
                socket("/chatsocket", new Chatroom())));

        webServer.start();
        fiber.start();
        System.out.println("Chat room running on: " + webServer.getUri());
        fiber.join();
    }

}
