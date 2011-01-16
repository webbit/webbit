package webbit.sample;

import webbit.WebServer;
import webbit.WebSocketConnection;
import webbit.WebSocketHandler;
import webbit.handler.HttpToWebSocketHandler;
import webbit.netty.NettyWebServer;
import webbit.handler.RoutingHttpHandler;
import webbit.handler.StringHttpHandler;

public class Foo {

    public static void main(String[] args) {
        RoutingHttpHandler handler = new RoutingHttpHandler();
        handler.map("/page", new StringHttpHandler("text/html", "Hello World"));
        handler.map("/ws", new HttpToWebSocketHandler(new WebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) {
                System.out.println("onOpen    :  " + connection);
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) {
                System.out.println("onMessage :  " + connection + " - " + msg);
                connection.send(msg.toUpperCase());
            }

            @Override
            public void onClose(WebSocketConnection connection) {
                System.out.println("onClose   :  " + connection);
            }
        }));
        
        WebServer web = new NettyWebServer(8080, handler);

        web.start();
        System.out.println("Listening on: " + web.getUri());
    }

}
