package org.webbitserver.handler;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.CometConnection;
import org.webbitserver.CometHandler;
import org.webbitserver.WebServer;
import org.webbitserver.es.EventSource;

import java.io.IOException;
import java.net.URI;

import static java.lang.Thread.sleep;
import static org.webbitserver.WebServers.createWebServer;

public class EventSourceClientTest {
    private WebServer webServer = createWebServer(59504);

    @After
    public void die() throws IOException, InterruptedException {
        webServer.stop().join();
    }

    @Test
    public void canReadFromEventSource() throws Exception {
        webServer
                .add("/es", new CometHandler() {
                    @Override
                    public void onOpen(CometConnection connection) throws Exception {
                        connection.send("ONE");
                        connection.send("TWO");
                        connection.send("THREE");
                    }

                    @Override
                    public void onClose(CometConnection connection) throws Exception {
                    }

                    @Override
                    public void onMessage(CometConnection connection, String msg) throws Exception {
                    }
                })
                .start();

        Thread.sleep(500);

        EventSource ws = new EventSource(URI.create("http://localhost:59504/es")) {
            @Override
            public void onConnect() {
                System.out.println("connect");
            }

            @Override
            public void onDisconnect() {
                throw new RuntimeException("TODO");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("frame.getTextData() = " + message);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        };
        ws.connect();

        sleep(1000);
    }
}
