package webbit;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executor;

public interface WebServer {
    WebServer add(HttpHandler handler);
    WebServer add(String path, HttpHandler handler);
    WebServer add(String path, WebSocketHandler handler);

    WebServer start() throws IOException;
    WebServer stop() throws IOException;
    WebServer join() throws InterruptedException;

    WebServer handleExceptions(Thread.UncaughtExceptionHandler handler);

    URI getUri();
    Executor getExecutor();
}
