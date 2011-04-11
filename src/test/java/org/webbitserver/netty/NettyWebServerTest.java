package org.webbitserver.netty;

import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NettyWebServerTest {
    @Test
    public void stopsServerCleanlyNotLeavingResourcesHanging() throws Exception {
        int threadCountStart = getCurrentThreadCount();

        // Build, start, wait and stop the Netty Web Server
        new NettyWebServer(8080).start().stop();

        assertEquals(threadCountStart, getCurrentThreadCount());
    }

    private int getCurrentThreadCount() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        return threadArray.length;
    }

}