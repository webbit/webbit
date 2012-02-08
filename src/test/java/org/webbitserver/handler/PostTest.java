package org.webbitserver.handler;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpPost;

public class PostTest {

    private WebServer webServer = createWebServer(59504);

    @After
    public void die() throws InterruptedException, ExecutionException {
        webServer.stop().get();
    }

    @Test
    public void exposesBodyInRequest() throws IOException, InterruptedException, ExecutionException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content("Body = {" + request.body() + "}").end();
            }
        }).start().get();
        String result = contents(httpPost(webServer, "/", "hello\n world"));
        assertEquals("Body = {hello\n world}", result);
    }

    @Test
    public void exposesPostBodyAsParameters() throws IOException, InterruptedException, ExecutionException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content("a=" + request.postParam("a") + ", b=" + request.postParam("b")).end();
            }
        }).start().get();
        String result = contents(httpPost(webServer, "/", "b=foo&a=hello%20world&c=d"));
        assertEquals("a=hello world, b=foo", result);
    }


    @Test
    public void exposesPostParamKeys() throws IOException, InterruptedException, ExecutionException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                ArrayList<String> keysList = new ArrayList<String>(request.postParamKeys());
                Collections.sort(keysList);

                response.content("keys=" + keysList.toString()).end();
            }
        }).start().get();
        String result = contents(httpPost(webServer, "/", "b=foo&a=hello%20world&c=d&b=duplicate"));
        assertEquals("keys=[a, b, c]", result);
    }

    @Test
    public void exposesPostBodyAsBytes() throws IOException, ExecutionException, InterruptedException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content(Arrays.toString(request.bodyAsBytes())).end();
            }
        }).start().get();
        byte[] byteArray = new byte[] {87, 79, 87, 46, 46, 46};
        String result = contents(httpPost(webServer, "/", new String(byteArray)));
        assertEquals(Arrays.toString(byteArray), result);
    }
}
