package org.webbitserver.netty;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NettyWebServerTest {

    private NettyWebServer server;

    @After
    public void stopServer() throws ExecutionException, InterruptedException {
        server.stop().get();
    }

    @Ignore // See https://github.com/webbit/webbit/issues/41
    @Test
    public void stopsServerCleanlyNotLeavingResourcesHanging() throws Exception {
        int threadCountStart = getCurrentThreadCount();
        server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080).start().get();
        server.stop().get();
        sleep(200);
        assertEquals(threadCountStart, getCurrentThreadCount());
    }

    @Test
    public void stopsServerCleanlyAlsoWhenClientsAreConnected() throws Exception {
        final CountDownLatch stopper = new CountDownLatch(1);
        server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080).start().get();
        server.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                server.stop().get();
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
    public void restartServerDoesNotThrowException() throws Exception {
        server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080);
        server.start().get();
        server.stop().get();
        server.start().get();
        server.stop().get();
    }

    @Test
    public void startServerAndTestIsRunning() throws Exception {
        server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080);
        server.start().get();
        assertTrue("Server should be running", server.isRunning());

        server.stop().get();
        assertTrue("Server should not be running", !server.isRunning());
    }

    private int getCurrentThreadCount() {
        return Thread.getAllStackTraces().keySet().size();
    }

}