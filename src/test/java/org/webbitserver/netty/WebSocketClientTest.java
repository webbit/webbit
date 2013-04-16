package org.webbitserver.netty;

import org.junit.Test;
import org.webbitserver.WebServer;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.webbitserver.WebServers.createWebServer;

public class WebSocketClientTest {

    private Throwable uncaughtException;

    @Test
    public void shouldAllowMultipleWebSocketClientsPerProcess() throws Exception {
        final WebServer webServer = createWebServer(44444).add("/", new WebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Throwable {
            }

            @Override
            public void onClose(WebSocketConnection connection) throws Throwable {
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
            }

            @Override
            public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
            }

            @Override
            public void onPing(WebSocketConnection connection, byte[] msg) throws Throwable {
            }

            @Override
            public void onPong(WebSocketConnection connection, byte[] msg) throws Throwable {
            }
        });
        webServer.start().get();

        final int iterations = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(iterations);

        URI uri = new URI("ws://localhost:44444/");

        for (int i = 0; i < iterations; i++) {
            final WebSocketClient client = new WebSocketClient(uri, new WebSocketHandler() {
                @Override
                public void onOpen(WebSocketConnection connection) throws Throwable {
                    connection.close();
                }

                @Override
                public void onClose(WebSocketConnection connection) throws Throwable {
                    countDownLatch.countDown();
                }

                @Override
                public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
                }

                @Override
                public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
                }

                @Override
                public void onPing(WebSocketConnection connection, byte[] msg) throws Throwable {
                }

                @Override
                public void onPong(WebSocketConnection connection, byte[] msg) throws Throwable {
                }
            });
            client.uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    throwable.printStackTrace();
                }
            });

            client.connectionExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    throwable.printStackTrace();
                    uncaughtException = throwable;
                }
            });
            client.start().get();
        }

        countDownLatch.await(5, SECONDS);
        assertNull(uncaughtException);
    }
}
