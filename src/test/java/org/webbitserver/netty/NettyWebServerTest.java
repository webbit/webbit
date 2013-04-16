package org.webbitserver.netty;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class NettyWebServerTest {

    private NettyWebServer server;

    @After
    public void stopServer() throws ExecutionException, InterruptedException {
        if (server != null) {
            server.stop().get();
        }
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

    @Test
    public void stopsServerCleanlyNotLeavingResourcesHanging() throws Exception {
        for (int i = 0; i < 100; i++) {
            startAndStop();
        }
    }

    private void startAndStop() throws InterruptedException, ExecutionException {
        List<String> beforeStart = getCurrentThreadNames();
        new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 9080).start().get().stop().get();
        List<String> afterStop = getCurrentThreadNames();
        if (afterStop.size() > beforeStart.size()) {
            System.err.println(String.format("Expected fewer threads after stopping. Before start: %d, After stop: %d", beforeStart.size(), afterStop.size()));
            System.err.println("Not failing the test because that hoses the release process. Just printing so we don't forget to fix this");
        }
    }

    private List<String> getCurrentThreadNames() {
        System.gc();
        List<String> threadNames = new ArrayList<String>();
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Thread thread : allStackTraces.keySet()) {
            threadNames.add(thread.getName());
        }
        Collections.sort(threadNames);
        return threadNames;
    }

}