package org.webbitserver;

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

    /**
     * What to do when an exception gets thrown in a handler.
     *
     * Defaults to using {@link org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler}.
     * It is suggested that apps supply their own implementation (e.g. to log somewhere).
     */
    WebServer uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler);

    /**
     * What to do when an exception occurs when attempting to read/write data
     * from/to the underlying connection. e.g. If an HTTP request disconnects
     * before it was expected.
     *
     * Defaults to using {@link org.webbitserver.handler.exceptions.SilentExceptionHandler}
     * as this is a common thing to happen on a network, and most systems should not care.
     */
    WebServer connectionExceptionHandler(Thread.UncaughtExceptionHandler handler);

    URI getUri();
    Executor getExecutor();
}
