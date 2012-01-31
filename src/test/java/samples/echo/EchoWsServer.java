package samples.echo;

import org.webbitserver.WebServer;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.HttpToWebSocketHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;

import java.io.IOException;
import java.net.URI;

import static org.webbitserver.WebServers.createWebServer;

public class EchoWsServer {

    private final WebServer webServer;

    public EchoWsServer(int port) throws IOException {
        this(createWebServer(port));
    }

    public EchoWsServer(WebServer webServer) throws IOException {
        this.webServer = webServer;
        webServer.add(new HttpToWebSocketHandler(new EchoHandler())).connectionExceptionHandler(new PrintStackTraceExceptionHandler());
    }

    public void start() throws IOException {
        webServer.start();
    }

    public URI uri() throws IOException {
        return webServer.getUri();
    }

    public void stop() throws IOException, InterruptedException {
        webServer.stop().join();
    }

    private static class EchoHandler implements WebSocketHandler {
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
    }
}
