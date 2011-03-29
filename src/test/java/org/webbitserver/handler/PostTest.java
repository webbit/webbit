package org.webbitserver.handler;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpPost;

public class PostTest {

    private WebServer webServer = createWebServer(59504);

    @After
    public void die() throws IOException, InterruptedException {
        webServer.stop().join();
    }

    @Test
    public void exposesBodyInRequest() throws IOException, InterruptedException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content("Body = {" + request.body() + "}").end();
            }
        }).start();
        String result = contents(httpPost(webServer, "/", "hello\n world"));
        assertEquals("Body = {hello\n world}", result);
    }

    @Test
    public void exposesPostBodyAsParameters() throws IOException, InterruptedException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content("a=" + request.postParam("a") + ", b=" + request.postParam("b")).end();
            }
        }).start();
        String result = contents(httpPost(webServer, "/", "b=foo&a=hello%20world&c=d"));
        assertEquals("a=hello world, b=foo", result);
    }


}
