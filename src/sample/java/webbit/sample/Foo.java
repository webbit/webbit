package webbit.sample;

import org.jetlang.fibers.ThreadFiber;
import webbit.WebServer;
import webbit.WebSocketConnection;
import webbit.WebSocketHandler;
import webbit.handler.DelayedHttpHandler;
import webbit.handler.RoutingHttpHandler;
import webbit.handler.StaticDirectoryHttpHandler;
import webbit.handler.StringHttpHandler;
import webbit.netty.NettyWebServer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static webbit.route.Route.*;

public class Foo {


    public static void main(String... args) throws Exception {
        ThreadFiber fiber = new ThreadFiber();

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
        };

        RoutingHttpHandler handler = route(
                new StaticDirectoryHttpHandler(fiber, new File("./src/sample/java/webbit/sample/content")),
                get("/page", new StringHttpHandler("text/html", "Hello World")),
                get("/slow", new DelayedHttpHandler(fiber, 3000, new StringHttpHandler("text/html", "Sloooow"))),
                socket("/ws", wsHandler));

        fiber.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (WebSocketConnection connection : connections) {
                    connection.send("" + System.currentTimeMillis());
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        fiber.start();

        WebServer webServer = new NettyWebServer(fiber, 8080, handler);
        webServer.start();
        System.out.println("Listening on: " + webServer.getUri());

        fiber.join();
    }

}
