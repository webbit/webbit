package webbit.sample;

import org.jetlang.fibers.ThreadFiber;
import webbit.WebServer;
import webbit.WebSocketConnection;
import webbit.WebSocketHandler;
import webbit.handler.DelayedHttpHandler;
import webbit.handler.HttpToWebSocketHandler;
import webbit.netty.NettyWebServer;
import webbit.handler.RoutingHttpHandler;
import webbit.handler.StringHttpHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Foo {

    public static void main(String... args) throws InterruptedException {
        ThreadFiber fiber = new ThreadFiber();

        final Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();

        RoutingHttpHandler handler = new RoutingHttpHandler();
        handler.map("/page", new StringHttpHandler("text/html", "Hello World"));
        handler.map("/slow", new DelayedHttpHandler(fiber, 3000, new StringHttpHandler("text/html", "Sloooow")));
        handler.map("/ws", new HttpToWebSocketHandler(new WebSocketHandler() {
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
        }));

        fiber.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (WebSocketConnection connection : connections) {
                    connection.send("" + System.currentTimeMillis());
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        fiber.start();

        WebServer web = new NettyWebServer(8080, handler, fiber);
        System.out.println("Listening on: " + web.getUri());

        new CountDownLatch(1).await();
    }

}
