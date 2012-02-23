package org.webbitserver.netty;

import org.junit.Ignore;
import org.junit.Test;
import org.webbitserver.WebSocket;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmWebSocketTest {
    @Test
    @Ignore
    public void can_talk_to_em_websocket_with_ascii() throws ExecutionException, InterruptedException {
        assertEchoed("Hellesøy", "Gøy");
    }

    @Test
    @Ignore
    public void can_talk_to_em_websocket_with_unicode() throws ExecutionException, InterruptedException {
        assertEchoed("Hellesoy", "Goy");
    }

    private void assertEchoed(final String stringMessage, final String bytesMessage) throws InterruptedException, ExecutionException {
        final CountDownLatch latch = new CountDownLatch(3);
        final List<String> received = new ArrayList<String>();
        WebSocket ws = new WebSocketClient(URI.create("ws://0.0.0.0:8080"), new WebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Throwable {
            }

            @Override
            public void onClose(WebSocketConnection connection) throws Throwable {
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
                received.add(msg);

                if(!msg.equals(stringMessage)) {
                    // It's the initial message from echo.rb
                    connection.send(stringMessage);
                } else {
                    // It's our own message coming back
                    connection.send(bytesMessage.getBytes("UTF-8"));
                }
                latch.countDown();
            }

            @Override
            public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
                received.add(new String(msg, "UTF-8"));
                latch.countDown();
            }

            @Override
            public void onPing(WebSocketConnection connection, byte[] msg) throws Throwable {
                connection.pong(msg);
            }

            @Override
            public void onPong(WebSocketConnection connection, byte[] msg) throws Throwable {
            }
        });
        ws.start().get();
        boolean finished = latch.await(500, TimeUnit.MILLISECONDS);
        assertEquals(asList("Hello Client!", stringMessage, bytesMessage), received);
        assertTrue(finished);
    }

}
