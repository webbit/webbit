package org.webbitserver.handler;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;

import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.WebServer;

public class AliasHandlerTest {
    private WebServer webServer = createWebServer(59504);

    @After
    public void die() throws InterruptedException, ExecutionException {
        webServer.stop().get();
    }

    @Test
    public void forwardsAliasedPath() throws Exception {
        webServer
                .add("/tomayto", new AliasHandler("/tomato"))
                .add("/tomato", new StringHttpHandler("text/plain", "body"))
                .start()
                .get();
        assertEquals("body", contents(httpGet(webServer, "/tomayto")));
    }
}
