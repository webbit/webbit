package samples.echo;

import org.webbitserver.WebServer;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import static org.webbitserver.WebServers.createWebServer;

/**
 * Opens websocket on /echo and simply echos any incoming message.
 * Useful for testing (and load-testing).
 */
public class Main {

    private static void log(char c) {
        System.out.print(c + "");
    }

    public static void main(String[] args) throws Exception {
        WebServer webServer = createWebServer(9888)
                .add("/echo", new WebSocketHandler() {
                    @Override
                    public void onOpen(WebSocketConnection connection) throws Exception {
                      connection.send("CONNECT");
                      log('C');
                    }
                    @Override
                    public void onClose(WebSocketConnection connection) throws Exception {
                      log('D');
                    }
                    @Override
                    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
                      connection.send(msg);
                      log('.');
                    }
                })
                .start();

        System.out.println("Echo server running on: " + webServer.getUri());
    }

}
