package org.webbitserver.netty;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebSocket;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.handler.exceptions.PrintStackTraceExceptionHandler;
import samples.echo.EchoWsServer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.webbitserver.helpers.Hex.toHex;

public abstract class WebSocketClientVerification {
    private EchoWsServer server;
    private URI wsUri;

    @Before
    public void start() throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        server = new EchoWsServer(createServer());
        server.start();
        URI uri = server.uri();
        wsUri = new URI(uri.toASCIIString().replaceFirst("http", "ws"));
    }

    protected abstract WebServer createServer() throws IOException;

    protected abstract void configure(WebSocket ws);

    @After
    public void die() throws IOException, InterruptedException, ExecutionException {
        server.stop();
    }

    @Test
    public void server_echoes_1_byte_string_message_immediately() throws InterruptedException {
        assertEchoed(stringMessage(1));
    }

    @Test
    public void server_echoes_1_byte_binary_message_immediately() throws InterruptedException {
        assertEchoed(binaryMessage(10), 0, 1);
    }

    @Test
    public void server_echoes_10_byte_binary_message_immediately() throws InterruptedException {
        assertEchoed(binaryMessage(10), 0, 10);
    }

    @Test
    public void server_echoes_125_byte_string_message_immediately() throws InterruptedException {
        assertEchoed(stringMessage(125));
    }

    @Test
    public void server_echoes_125_byte_binary_message_immediately() throws InterruptedException {
        assertEchoed(binaryMessage(125), 0, 125);
    }

    @Test
    public void server_echoes_126_byte_string_message_immediately() throws InterruptedException {
        assertEchoed(stringMessage(126));
    }

    @Test
    public void server_echoes_126_byte_binary_message_immediately() throws InterruptedException {
        assertEchoed(binaryMessage(126), 0, 126);
    }

    @Test
    public void server_echoes_127_byte_string_message_immediately() throws InterruptedException {
        assertEchoed(stringMessage(127));
    }

    @Test
    public void server_echoes_127_byte_binary_message_immediately() throws InterruptedException {
        assertEchoed(binaryMessage(127), 0, 127);
    }

    @Test
    public void server_echoes_128_byte_string_message_immediately() throws InterruptedException {
        assertEchoed(stringMessage(128));
    }

    @Test
    public void server_echoes_128_byte_binary_message_immediately() throws InterruptedException {
        assertEchoed(binaryMessage(128), 0, 128);
    }

    @Test
    public void server_echoes_byte_binary_message_with_offset_and_length_immediately() throws InterruptedException {
        assertEchoed(binaryMessage(10), 4, 3);
    }

    // This always fails. We should un-Ignore this when #65 is fixed.
    @Ignore
    @Test
    public void server_echoes_0_byte_string_message_immediately() throws InterruptedException {
        assertEchoed(stringMessage(0));
    }

    private void assertEchoed(final String message) throws InterruptedException {
        final CountDownLatch countDown = new CountDownLatch(2);
        final List<String> received = Collections.synchronizedList(new ArrayList<String>());

        WebSocket ws = new WebSocketClient(wsUri, new BaseWebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Exception {
                connection.send(message);
                countDown.countDown();
            }

            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
                received.add(msg);
                countDown.countDown();
            }
        }, Executors.newSingleThreadExecutor());
        ws.connectionExceptionHandler(new PrintStackTraceExceptionHandler());
        configure(ws);
        ws.start();

        assertTrue("Message wasn't echoed", countDown.await(300, TimeUnit.MILLISECONDS));
        assertEquals(message, received.get(0));
    }

    private void assertEchoed(final byte[] message, final int offset, final int length) throws InterruptedException {
        final byte[] expected = new byte[length];
        System.arraycopy(message, offset, expected, 0, length);

        final byte[] copy = new byte[message.length];
        System.arraycopy(message, 0, copy, 0, message.length);

        final CountDownLatch countDown = new CountDownLatch(2);
        final List<byte[]> received = Collections.synchronizedList(new ArrayList<byte[]>());

        WebSocket ws = new WebSocketClient(wsUri, new BaseWebSocketHandler() {
            @Override
            public void onOpen(WebSocketConnection connection) throws Exception {
                connection.send(message, offset, length);
                countDown.countDown();
            }

            @Override
            public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
                received.add(msg);
                countDown.countDown();
            }
        }, Executors.newSingleThreadExecutor());
        ws.connectionExceptionHandler(new PrintStackTraceExceptionHandler());
        configure(ws);
        ws.start();

        assertTrue("Message wasn't echoed", countDown.await(300, TimeUnit.MILLISECONDS));
        assertEquals(toHex(expected), toHex(received.get(0)));
        assertEquals(toHex(message), toHex(copy));
    }

    private String stringMessage(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    private byte[] binaryMessage(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }
}
