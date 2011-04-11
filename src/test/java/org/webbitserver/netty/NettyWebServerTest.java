package org.webbitserver.netty;

import java.util.Set;
import java.util.concurrent.Executors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NettyWebServerTest {
    @Test
    public void stopsServerCleanlyNotLeavingResourcesHanging() throws Exception {
        int threadCountStart = getCurrentThreadCount();
        NettyWebServer server = new NettyWebServer(Executors.newSingleThreadScheduledExecutor(), 8080).start();
        assertEquals(threadCountStart+1, getCurrentThreadCount());
        server.stop();
        assertEquals(threadCountStart, getCurrentThreadCount());
    }

    private int getCurrentThreadCount() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        return threadArray.length;
    }

}