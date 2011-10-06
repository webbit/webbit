package samples.echo;

import org.webbitserver.WebServer;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.HttpToWebSocketHandler;

import static org.webbitserver.WebServers.createWebServer;

/**
 * Simple Echo server to be used with the Autobahn test suite.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9000).add(new HttpToWebSocketHandler(new WebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Exception {
                System.out.println("connection = " + connection.version());
                System.out.print("O");
            }

            @Override
            public void onClose(WebSocketConnection connection) throws Exception {
                System.out.print("C");
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Exception {
                System.out.print("S");
                connection.send(msg);
            }

            @Override
            public void onMessage(WebSocketConnection connection, byte[] msg) {
                System.out.print("B");
                connection.send(msg);
            }

            @Override
            public void onPong(WebSocketConnection connection, String msg) {
                System.out.print("P");
                connection.ping(msg);
            }
        })).start();

        System.out.println("Echo server running on: " + webServer.getUri());
    }

}
