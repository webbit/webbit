package samples.echo;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.handler.HttpToWebSocketHandler;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class EchoWsServer {

    private final WebServer webServer;

    public EchoWsServer(WebServer webServer) throws IOException {
        this.webServer = webServer;
        webServer.add(new HttpToWebSocketHandler(new EchoHandler())).connectionExceptionHandler(new PrintStackTraceExceptionHandler());
    }

    public void start() throws ExecutionException, InterruptedException {
        webServer.start().get();
    }

    public URI uri() throws IOException {
        return webServer.getUri();
    }

    public void stop() throws ExecutionException, InterruptedException {
        webServer.stop().get();
    }

    private static class EchoHandler extends BaseWebSocketHandler {
        @Override
        public void onMessage(WebSocketConnection connection, String msg) throws Exception {
            connection.send(msg);
        }

        @Override
        public void onMessage(WebSocketConnection connection, byte[] msg) {
            connection.send(msg);
        }
    }
}
