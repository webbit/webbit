package samples.echo;

import org.webbitserver.WebServer;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.handler.HttpToWebSocketHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;
import org.webbitserver.handler.logging.LoggingHandler;
import org.webbitserver.handler.logging.SimpleLogSink;

import java.io.IOException;
import java.net.URI;

import static org.webbitserver.WebServers.createWebServer;

public class EchoWsServer {

    private final WebServer webServer;

    public EchoWsServer(int port) throws IOException {
        webServer = createWebServer(port)
                .add(new HttpToWebSocketHandler(new EchoHandler())).connectionExceptionHandler(new PrintStackTraceExceptionHandler());
    }

    public URI start() throws IOException {
        webServer.start();
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
