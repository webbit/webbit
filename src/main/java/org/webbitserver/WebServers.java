package org.webbitserver;

import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;

public class WebServers {

    /**
     * Returns a new {@link WebServer} object, which runs on the provided port.
     *
     * @param port
     * @return {@link WebServer} object
     * @see NettyWebServer
     */
    public static WebServer createWebServer(int port) {
        return new NettyWebServer(port);
    }

    /**
     * Returns a new {@link WebServer} object, which runs on the provided port
     * and adds the executor to the List of executor services to be called when
     * the server is running.
     *
     * @param executor Since Webbit is designed to be a single threaded non-blocking server,<br />
     * it is assumed that the user supplied executor will provide only a single thread.
     * @param port
     * @return {@link WebServer} object
     * @see NettyWebServer
     */
    public static WebServer createWebServer(Executor executor, int port) {
        return new NettyWebServer(executor, port);
    }

    /**
     * Returns a new {@link WebServer} object, adding the executor to the list
     * of executor services, running on the stated socket address and accessible
     * from the provided public URI.
     *
     * @param executor Since Webbit is designed to be a single threaded non-blocking server,<br />
     * it is assumed that the user supplied executor will provide only a single thread.
     * @param socketAddress
     * @param publicUri
     * @return {@link WebServer} object
     * @see NettyWebServer
     */
    public static WebServer createWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        return new NettyWebServer(executor, socketAddress, publicUri);
    }

}
