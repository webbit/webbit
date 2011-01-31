package webbit.sample;

import webbit.WebServer;
import webbit.WebSocketConnection;
import webbit.WebSocketHandler;
import webbit.handler.DelayedHttpHandler;
import webbit.handler.StringHttpHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static webbit.WebServers.createWebServer;

public class Foo {


    public static void main(String... args) throws Exception {
        ScheduledExecutorService executor = newSingleThreadScheduledExecutor();

        final Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();

        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) {
                connections.add(connection);
                System.out.println("onOpen    :  " + connection);
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) {
                System.out.println("onMessage :  " + connection + " - " + msg);
                connection.send(msg.toUpperCase());
            }

            @Override
            public void onClose(WebSocketConnection connection) {
                connections.remove(connection);
                System.out.println("onClose   :  " + connection);
            }

            @Override
            public void onError(WebSocketConnection connection, Exception error) throws Exception {
                error.printStackTrace();
            }
        };

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (WebSocketConnection connection : connections) {
                    connection.send("" + System.currentTimeMillis());
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        WebServer webServer = createWebServer(executor, 8080)
                .add("/page", new StringHttpHandler("text/html", "Hello World"))
                .add("/slow", new DelayedHttpHandler(executor, 3000, new StringHttpHandler("text/html", "Sloooow")))
                .add("/ws", wsHandler)
                .staticResources("./src/sample/java/webbit/sample/content")
                .start();
        System.out.println("Listening on: " + webServer.getUri());
    }

}
