package org.webbitserver.handler;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpPost;

public class StaleConnectionTest {

    private WebServer webServer = createWebServer(59504);

    @After
    public void die() throws IOException, InterruptedException {
        webServer.stop().join();
    }

    @Test
    public void closesConnectionAfterTimeoutIfClientKeepsConnectioOpen() throws IOException, InterruptedException {
        webServer
                .staleConnectionTimeout(100)
                .add(new HttpHandler() {
                    @Override
                    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
                            throws Exception
                    {
                        response.content("Body = {" + request.body() + "}");
                        response.header("Content-Length",
                                        (String) null); // This makes the client hang until the server closes the connection.
                        response.end();
                    }
                }).start();
        String result = contents(httpPost(webServer, "/", "hello\n world"));
        assertEquals("Body = {hello\n world}", result);
    }
}
