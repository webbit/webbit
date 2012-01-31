package org.webbitserver;

import org.webbitserver.netty.WebSocketClient;

import java.io.InputStream;

public interface WebSocket {
    WebSocket start();

    WebSocket close();

    WebSocket reconnectEvery(long millis);

    WebSocket setupSsl(InputStream keyStore, String storePass);

    /**
     * What to do when an exception gets thrown in a handler.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler}.
     * It is suggested that apps supply their own implementation (e.g. to log somewhere).
     */
    WebSocketClient uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler);

    /**
     * What to do when an exception occurs when attempting to read/write data
     * from/to the underlying connection. e.g. If an HTTP request disconnects
     * before it was expected.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.SilentExceptionHandler}
     * as this is a common thing to happen on a network, and most systems should not care.
     */
    WebSocketClient connectionExceptionHandler(Thread.UncaughtExceptionHandler handler);
}
