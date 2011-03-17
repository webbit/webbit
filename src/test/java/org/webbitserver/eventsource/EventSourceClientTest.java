package org.webbitserver.eventsource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.CometConnection;
import org.webbitserver.CometHandler;
import org.webbitserver.WebServer;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.webbitserver.WebServers.createWebServer;

public class EventSourceClientTest {
    private WebServer webServer;
    private EventSource eventSource;

    @Before
    public void createServer() {
        webServer = createWebServer(59504);
    }

    @After
    public void die() throws IOException, InterruptedException {
        eventSource.close().join();
        webServer.stop().join();
    }

    @Test
    public void canSendAndReadTwoSingleLineMessages() throws Exception {
        assertSentAndReceived(asList("a", "b"));
    }

    @Test
    public void canSendAndReadThreeSingleLineMessages() throws Exception {
        assertSentAndReceived(asList("C", "D", "E"));
    }

    @Test
    public void canSendAndReadOneMultiLineMessages() throws Exception {
        assertSentAndReceived(asList("f\ng\nh"));
    }

    @Test
    public void reconnectsIfServerIsDown() throws Exception {
        List<String> messages = asList("a", "b");
        CountDownLatch messageCountdown = new CountDownLatch(messages.size());
        CountDownLatch errorCountdown = new CountDownLatch(0);
        startClient(messages, messageCountdown, errorCountdown, 100);
        startServer(messages);
        assertTrue("Didn't get an error on first failed connection", errorCountdown.await(2000, TimeUnit.MILLISECONDS));
        assertTrue("Didn't get all messages", messageCountdown.await(2000, TimeUnit.MILLISECONDS));
    }

    private void assertSentAndReceived(final List<String> messages) throws IOException, InterruptedException {
        startServer(messages);
        CountDownLatch messageCountdown = new CountDownLatch(messages.size());
        startClient(messages, messageCountdown, new CountDownLatch(0), 5000);
        assertTrue("Didn't get all messages", messageCountdown.await(1000, TimeUnit.MILLISECONDS));
    }

    private void startClient(final List<String> expectedMessages, final CountDownLatch messageCountdown, final CountDownLatch errorCountdown, long reconnectionTimeMillis) throws InterruptedException {
        eventSource = new EventSource(Executors.newSingleThreadExecutor(), reconnectionTimeMillis, URI.create("http://localhost:59504/es/hello?echoThis=yo"), new EventSourceHandler() {
            int n = 0;

            @Override
            public void onConnect() {
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onMessage(String event, MessageEvent message) {
                assertEquals(expectedMessages.get(n++) + " yo", message.data);
                assertEquals("http://localhost:59504/es/hello?echoThis=yo", message.origin);
                messageCountdown.countDown();
            }

            @Override
            public void onError(Throwable t) {
                errorCountdown.countDown();
            }
        });
        eventSource.connect().await();
    }

    private void startServer(final List<String> messagesToSend) throws IOException {
        webServer
                .add("/es/.*", new CometHandler() {
                    @Override
                    public void onOpen(CometConnection connection) throws Exception {
                        for (String message : messagesToSend) {
                            connection.send(message + " " + connection.httpRequest().queryParam("echoThis"));
                        }
                    }

                    @Override
                    public void onClose(CometConnection connection) throws Exception {
                    }

                    @Override
                    public void onMessage(CometConnection connection, String msg) throws Exception {
                    }
                })
                .start();
    }
}
