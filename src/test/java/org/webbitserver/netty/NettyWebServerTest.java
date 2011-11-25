package org.webbitserver.netty;

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

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NettyWebServerTest {
    @Test
    public void stopsServerCleanlyNotLeavingResourcesHanging() throws Exception {
        int threadCountStart = getCurrentThreadCount();
        WebServer server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080).start();
        assertEquals(threadCountStart + 2, getCurrentThreadCount());
        server.stop().join();
        sleep(100);
        assertEquals(threadCountStart, getCurrentThreadCount());
    }

    @Test
    public void stopsServerCleanlyAlsoWhenClientsAreConnected() throws Exception {
        final CountDownLatch stopper = new CountDownLatch(1);
        final WebServer server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080).start();
        server.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                server.stop().join();
                stopper.countDown();
            }
        });
        Socket client = new Socket(InetAddress.getLocalHost(), 9080);
        OutputStream http = client.getOutputStream();
        http.write(("" +
                "GET /index.html HTTP/1.1\r\n" +
                "Host: www.example.com\r\n\r\n").getBytes("UTF-8"));
        http.flush();

        assertTrue("Server should have stopped by now", stopper.await(1000, TimeUnit.MILLISECONDS));
    }
    
    @Test
    public void restartServer() throws Exception {
        WebServer server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080);
        server.start();
        server.stop().join();
        server.start();
        server.stop().join();
    }
    
    @Test
    public void startServerAndTestIsRunning() throws Exception {
        NettyWebServer server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080);
        server.start();
        boolean isRunning = server.isRunning();
        server.stop().join();        
        boolean isStopped = !server.isRunning();
        
        assertTrue("Server should be running", isRunning);
        assertTrue("Server should not be running", isStopped);
    }

    private int getCurrentThreadCount() {
        return Thread.getAllStackTraces().keySet().size();
    }

}