package org.webbitserver.netty;

import org.junit.Ignore;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NettyWebServerTest {
    @Test
    public void stopsServerCleanlyNotLeavingResourcesHanging() throws Exception {
        int threadCountStart = getCurrentThreadCount();
        WebServer server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 8080).start();
        assertEquals(threadCountStart+1, getCurrentThreadCount());
        server.stop().join();
        assertEquals(threadCountStart, getCurrentThreadCount());
    }

    // Failing test for https://github.com/joewalnes/webbit/issues/29
    @Test
    @Ignore
    public void stopsServerCleanlyAlsoWhenClientsAreConnected() throws Exception {
        final CountDownLatch stopper = new CountDownLatch(1);
        final WebServer server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 8080).start();
        server.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                System.out.println("We got here");
                server.stop().join();
                System.out.println("But never here");
                stopper.countDown();
            }
        });
        Socket client = new Socket(InetAddress.getLocalHost(), 8080);
        OutputStream http = client.getOutputStream();
        http.write(("" +
                "GET /index.html HTTP/1.1\r\n" +
                "Host: www.example.com\r\n\r\n").getBytes("UTF-8"));
        http.flush();

        assertTrue("Server should have stopped by now", stopper.await(1000, TimeUnit.MILLISECONDS));
    }

    private int getCurrentThreadCount() {
        return Thread.getAllStackTraces().keySet().size();
    }

}