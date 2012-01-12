package org.webbitserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Executor;

/**
 * <p>Configures an event based webserver.</p>
 * <p/>
 * <p>To create an instance, use {@link WebServers#createWebServer(int)}.</p>
 * <p/>
 * <p>As with many of the interfaces in webbitserver, setter style methods return a
 * reference to this, to allow for simple initialization using method chaining.</p>
 * <p/>
 * <h2>Hello World Example</h2>
 * <pre>
 * class HelloWorldHandler implements HttpHandler {
 *   void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
 *     response.header("Content-Type", "text/html")
 *             .content("Hello World")
 *             .end();
 *   }
 * }
 * WebServer webServer = WebServers.createWebServer(8080)
 *                                 .add(new HelloWorldHandler())
 *                                 .start();
 * print("Point your browser to " + webServer.getUri());
 * </pre>
 * <p/>
 * <h2>Serving Static Files</h2>
 * <pre>
 * WebServer webServer = WebServers.createWebServer(8080)
 *                                 .add(new StaticFileHandler("./wwwdata"))
 *                                 .start();
 * </pre>
 *
 * @author Joe Walnes
 * @see WebServers
 * @see HttpHandler
 * @see WebSocketConnection
 * @see EventSourceConnection
 */
public interface WebServer {

    /**
     * Add an HttpHandler. When a request comes in the first HttpHandler will be invoked.
     * The HttpHandler should either handle the request, or pass the request onto the
     * next HttpHandler (using {@link HttpControl#nextHandler()}). This is repeated
     * until a HttpHandler returns a response. If there are no remaining handlers, the
     * webserver shall return 404 NOT FOUND to the browser.
     * <p/>
     * HttpHandlers are attempted in the order in which they are added to the WebServer.
     *
     * @see HttpHandler
     */
    WebServer add(HttpHandler handler);

    /**
     * Add an HttpHandler that will only respond to a certain path (e.g "/some/page").
     * <p/>
     * This is shortcut for {@code add(newPathMatchHandler(path, handler))}.
     *
     * @see HttpHandler
     * @see #add(HttpHandler)
     * @see org.webbitserver.handler.PathMatchHandler
     */
    WebServer add(String path, HttpHandler handler);

    /**
     * Add a WebSocketHandler for dealing with WebSockets.
     * <p/>
     * This is shortcut for {@code add(new PathMatchHandler(path, newHttpToWebSocketHandler(handler)))}.
     *
     * @see WebSocketHandler
     * @see HttpHandler
     * @see #add(HttpHandler)
     * @see org.webbitserver.handler.HttpToWebSocketHandler
     * @see org.webbitserver.handler.PathMatchHandler
     */
    WebServer add(String path, WebSocketHandler handler);

    /**
     * Add a WebSocketHandler for dealing with WebSockets.
     * <p/>
     * This is shortcut for {@code add(new PathMatchHandler(path, newHttpToEventSourceHandler(handler)))}.
     *
     * @see HttpHandler
     * @see #add(HttpHandler)
     * @see org.webbitserver.handler.HttpToEventSourceHandler
     * @see org.webbitserver.handler.PathMatchHandler
     */
    WebServer add(String path, EventSourceHandler handler);

    /**
     * Start web server in background.
     */
    WebServer start() throws IOException;

    /**
     * Stop web server background thread. This returns immediately, but the
     * webserver may still be shutting down. To wait until it's fully stopped,
     * use {@link #join()}.
     */
    WebServer stop() throws IOException;

    /**
     * Call after {@link #stop()} to wait until webserver has stopped its background
     * threads and closed all socket connections. This method blocks.
     */
    WebServer join() throws InterruptedException;

    /**
     * What to do when an exception gets thrown in a handler.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler}.
     * It is suggested that apps supply their own implementation (e.g. to log somewhere).
     */
    WebServer uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler);

    /**
     * What to do when an exception occurs when attempting to read/write data
     * from/to the underlying connection. e.g. If an HTTP request disconnects
     * before it was expected.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.SilentExceptionHandler}
     * as this is a common thing to happen on a network, and most systems should not care.
     */
    WebServer connectionExceptionHandler(Thread.UncaughtExceptionHandler handler);

    /**
     * Get base URI that webserver is serving on.
     */
    URI getUri();
    
    /**
     * Get base port that webserver is serving on.
     */
    int getPort();

    /**
     * Get main work executor that all handlers will execute on.
     */
    Executor getExecutor();

    /**
     * Number of milliseconds before a stale HTTP keep-alive connection is closed by the server. A HTTP connection
     * is considered stale if it remains open without sending more data within the timeout window.
     */
    WebServer staleConnectionTimeout(long millis);

    /**
     * Setup SSL/TLS handler
     * <p/>
     * This is shortcut for {@code setupSsl(keyStore, pass, pass)}.
     *
     * @param keyStore Keystore InputStream
     * @param pass Store and key password
     * @return current WebServer instance
     * @throws WebbitException A problem loading the keystore
     * @see #setupSsl(String, String, String)
     */
    WebServer setupSsl(InputStream keyStore, String pass) throws WebbitException;

    /**
     * Setup SSL/TLS handler
     *
     * @param keyStore Keystore InputStream
     * @param storePass Store password
     * @param keyPass Key password
     * @return current WebServer instance
     * @throws WebbitException A problem loading the keystore
     * @see #setupSsl(String, String, String)
     */
    WebServer setupSsl(InputStream keyStore, String storePass, String keyPass) throws WebbitException;
}
