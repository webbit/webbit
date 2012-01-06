package org.webbitserver.wsclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import samples.echo.EchoWsServer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class WebSocketTest {
    private EchoWsServer server;
    private URI wsUri;

    @Before
    public void start() throws IOException, URISyntaxException, InterruptedException {
        server = new EchoWsServer(59509);
        URI uri = server.start();
        wsUri = new URI(uri.toASCIIString().replaceFirst("http", "ws"));
    }

    @After
    public void die() throws IOException, InterruptedException {
        server.stop();
    }

    @Test
    public void sendsAndReceivesTextMessages() throws InterruptedException {
        final CountDownLatch countDown = new CountDownLatch(2);

        new WebSocket(wsUri, new WebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Exception {
                System.out.println("AHOY");
                connection.send("You are Alexander Yalt?");
                countDown.countDown();
            }

            @Override
            public void onClose(WebSocketConnection connection) throws Exception {
                System.out.println("CLOSE");
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
                System.out.println("msg = " + msg);
                assertEquals("You are Alexander Yalt?", msg);
                countDown.countDown();
            }

            @Override
            public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
            }

            @Override
            public void onPong(WebSocketConnection connection, String msg) throws Throwable {
            }
        }, Executors.newSingleThreadExecutor());
//        boolean completed = countDown.await(1000, TimeUnit.MILLISECONDS);
//        System.out.println("completed = " + completed);
//        System.out.println("countDown = " + countDown.getCount());
//        assertFalse("Didn't count down: " + countDown.getCount(), completed);

        sleep(2000);
    }
}
