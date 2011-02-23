package org.webbitserver.handler;

import org.junit.Test;
import org.webbitserver.*;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URLConnection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;

public class CookieTest {
    @Test
    public void setsOneOutboundCookie() throws IOException, InterruptedException {
        WebServer webServer = createWebServer(59504)
                .add(new HttpHandler() {
                    @Override
                    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                        response.content("Blah").cookie(new HttpCookie("a", "b")).end();
                    }
                })
                .start();
        try {
            URLConnection urlConnection = httpGet(webServer, "/");
            List<HttpCookie> cookies = HttpCookie.parse(urlConnection.getHeaderField("Set-Cookie"));
            assertEquals(1, cookies.size());
            assertEquals("a", cookies.get(0).getName());
            assertEquals("b", cookies.get(0).getValue());
        } finally {
            webServer.stop().join();
        }
    }

    @Test
    public void parserOneInboundCookie() throws IOException, InterruptedException {
        WebServer webServer = createWebServer(59504)
                .add(new HttpHandler() {
                    @Override
                    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                        String body = "Your cookie name: " + request.cookies().get(0).getName();
                        response.header("Content-Length", body.length())
                                .content(body)
                                .end();
                    }
                })
                .start();
        try {
            URLConnection urlConnection = httpGet(webServer, "/");
            urlConnection.addRequestProperty("Cookie", new HttpCookie("someName", "someValue").toString());
            assertEquals("Your cookie name: someName", contents(urlConnection));
        } finally {
            webServer.stop().join();
        }
    }

}
