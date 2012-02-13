package org.webbitserver;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface Endpoint<T> {
    /**
     * Start in background. This returns immediately, but the endpoint
     * may still not be ready to accept incoming requests (or have established a connection - if this is a client).
     * To wait until it's fully started, call {@link java.util.concurrent.Future#get()} on the returned future.
     */
    Future<? extends T> start();

    /**
     * Stop in background. This returns immediately, but the
     * endpoint may still be shutting down. To wait until it's fully stopped,
     * call {@link java.util.concurrent.Future#get()} on the returned future.
     */
    Future<? extends T> stop();

    /**
     * What to do when an exception gets thrown in a handler.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler}.
     * It is suggested that apps supply their own implementation (e.g. to log somewhere).
     */
    T uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler);

    /**
     * What to do when an exception occurs when attempting to read/write data
     * from/to the underlying connection. e.g. If an HTTP request disconnects
     * before it was expected.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.SilentExceptionHandler}
     * as this is a common thing to happen on a network, and most systems should not care.
     */
    T connectionExceptionHandler(Thread.UncaughtExceptionHandler handler);

    /**
     * Get main work executor that all handlers will execute on.
     */
    Executor getExecutor();

    /**
     * Get base URI that endpoint is serving on (or connected to).
     */
    URI getUri();

    /**
     * Setup SSL/TLS handler
     * <p/>
     * This is shortcut for {@code setupSsl(keyStore, pass, pass)}.
     *
     * @param keyStore Keystore InputStream
     * @param pass     Store and key password
     * @return current WebServer instance
     * @throws org.webbitserver.WebbitException
     *          A problem loading the keystore
     * @see #setupSsl(String, String, String)
     */
    T setupSsl(InputStream keyStore, String pass) throws WebbitException;
}
