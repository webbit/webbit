package samples.echo;

import org.webbitserver.WebServer;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.HttpToWebSocketHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;

import static org.webbitserver.WebServers.createWebServer;

/**
 * Simple Echo server to be used with the Autobahn test suite.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9001).add(new HttpToWebSocketHandler(new WebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Exception {
            }

            @Override
            public void onClose(WebSocketConnection connection) throws Exception {
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Exception {
                connection.send(msg);
            }

            @Override
            public void onMessage(WebSocketConnection connection, byte[] msg) {
                connection.send(msg);
            }

            @Override
            public void onPong(WebSocketConnection connection, String msg) {
                connection.ping(msg);
            }
        })).connectionExceptionHandler(new PrintStackTraceExceptionHandler()).start();

        System.out.println("Echo server running on: " + webServer.getUri());
    }

}
