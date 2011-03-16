package org.webbitserver.handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.CometConnection;
import org.webbitserver.CometHandler;
import org.webbitserver.WebServer;
import org.webbitserver.es.EventSource;
import org.webbitserver.es.EventSourceHandler;
import org.webbitserver.es.MessageEvent;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.webbitserver.WebServers.createWebServer;

public class EventSourceClientTest {
    private WebServer webServer;
    private EventSource es;

    @Before
    public void createServer() {
        webServer = createWebServer(59504);
    }

    @After
    public void die() throws IOException, InterruptedException {
        es.disconnect().await();
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

    private void assertSentAndReceived(final List<String> messages) throws IOException, InterruptedException {
        webServer
                .add("/es/.*", new CometHandler() {
                    @Override
                    public void onOpen(CometConnection connection) throws Exception {
                        // For some reason we have to sleep a little here before starting to send messages.
                        // Removing this sleep (intermittently) causes tests to fail. The first test never fails.
                        // Not sure where this race condition occurs - it could be in webbit itself...
                        sleep(10);
                        for (String message : messages) {
                            URI uri = URI.create(connection.httpRequest().uri());
                            connection.send(message + " " + uri.getPath().split("/")[2]);
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

        final CountDownLatch latch = new CountDownLatch(messages.size());
        es = new EventSource(URI.create("http://localhost:59504/es/hello"), new EventSourceHandler() {
            int n = 0;

            @Override
            public void onConnect() {
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onMessage(MessageEvent event) {
                assertEquals(messages.get(n++) + " hello", event.data);
                assertEquals("http://localhost:59504/es/hello", event.origin);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        });
        es.connect().await();
        assertTrue("Didn't get all messages", latch.await(1000, TimeUnit.MILLISECONDS));
    }
}
