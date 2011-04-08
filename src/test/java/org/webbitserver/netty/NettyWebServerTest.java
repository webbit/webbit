package org.webbitserver.netty;

import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Kushal Pisavadia
 */
public class NettyWebServerTest {

    /**
     * Tests the stop() method in NettyWebServer. Makes sure that we are stopping
     * the web server cleanly and not leaving any resources hanging.
     *
     * @throws Exception
     * @see NettyWebServer
     */
    @Test
    public void testStop() throws Exception {
        int threadCountStart = 0, threadCountEnd = 0;

        threadCountStart = this.getCurrentThreadCount();

        // Build, start, wait and stop the Netty Web Server
        NettyWebServer instance = new NettyWebServer(8080).start();
        instance.stop();

        threadCountEnd = this.getCurrentThreadCount();

        assertEquals(threadCountStart, threadCountEnd);
    }

    private int getCurrentThreadCount() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        return threadArray.length;
    }

}