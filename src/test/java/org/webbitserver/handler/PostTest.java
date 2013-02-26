package org.webbitserver.handler;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.netty.NettyWebServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpPost;

public class PostTest {

    private NettyWebServer webServer = new NettyWebServer(59504);

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
        byte[] byteArray = new byte[]{87, 79, 87, 46, 46, 46};
        String result = contents(httpPost(webServer, "/", new String(byteArray)));
        assertEquals(Arrays.toString(byteArray), result);
    }

    @Test
    public void request_body_longer_than_max_content_length_causes_500_and_does_not_invoke_handlers() throws IOException, ExecutionException, InterruptedException {
        webServer.connectionExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
            }
        });
        webServer.uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
            }
        });
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.error(new RuntimeException("Should never get here"));
            }
        }).start().get();
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < 65537; i++) {
            body.append(".");
        }
        URLConnection urlConnection = httpPost(webServer, "/", body.toString());
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        assertEquals(413, httpURLConnection.getResponseCode());
    }

    @Test
    public void max_content_length_can_be_increased() throws IOException, ExecutionException, InterruptedException {
        webServer.maxContentLength(65537);
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content("length:" + request.bodyAsBytes().length).end();
            }
        }).start().get();
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < 65537; i++) {
            body.append(".");
        }
        URLConnection urlConnection = httpPost(webServer, "/", body.toString());
        String result = contents(urlConnection);
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        assertEquals(200, httpURLConnection.getResponseCode());
        assertEquals("length:65537", result);
    }
}
