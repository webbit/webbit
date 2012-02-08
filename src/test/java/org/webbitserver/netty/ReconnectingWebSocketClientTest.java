package org.webbitserver.netty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocket;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import samples.echo.EchoWsServer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReconnectingWebSocketClientTest {
    private EchoWsServer server;
    private URI wsUri;

    @Before
    public void start() throws IOException, URISyntaxException, InterruptedException {
        server = new EchoWsServer(new NettyWebServer(59509));
        URI uri = server.uri();
        wsUri = new URI(uri.toASCIIString().replaceFirst("http", "ws"));
    }

    @After
    public void die() throws ExecutionException, InterruptedException {
        server.stop();
    }

    @Test
    public void client_reconnects_when_told_to_do_so() throws InterruptedException, IOException, ExecutionException, TimeoutException {
        final CountDownLatch closed = new CountDownLatch(1);
        final CountDownLatch connected = new CountDownLatch(1);
        WebSocket ws = new WebSocketClient(wsUri, new BaseWebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Exception {
                connected.countDown();
            }

            @Override
            public void onClose(WebSocketConnection connection) throws Exception {
                closed.countDown();
            }
        });
        ws.reconnectEvery(10);

        ws.start().get(100, TimeUnit.MILLISECONDS);
        assertTrue("Should have closed", closed.await(50, TimeUnit.MILLISECONDS));

        server.start();
        assertTrue("Should have reconnected", connected.await(200, TimeUnit.MILLISECONDS));
    }

    @Test
    public void client_does_not_reconnect_when_not_told_to_do_so() throws InterruptedException, IOException, ExecutionException, TimeoutException {
        final CountDownLatch closed = new CountDownLatch(1);
        final CountDownLatch connected = new CountDownLatch(1);
        WebSocket ws = new WebSocketClient(wsUri, new BaseWebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Exception {
                connected.countDown();
            }

            @Override
            public void onClose(WebSocketConnection connection) throws Exception {
                closed.countDown();
            }
        });

        ws.start().get(100, TimeUnit.MILLISECONDS);
        assertTrue("Should have closed", closed.await(50, TimeUnit.MILLISECONDS));

        server.start();
        assertFalse("Shouldn't have reconnected", connected.await(200, TimeUnit.MILLISECONDS));
    }
}
