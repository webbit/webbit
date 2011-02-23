package org.webbitserver.handler;

import org.junit.Test;
import org.webbitserver.*;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URLConnection;
import java.util.*;

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
            List<HttpCookie> cookies = cookies(urlConnection);
            assertEquals(1, cookies.size());
            assertEquals("a", cookies.get(0).getName());
            assertEquals("b", cookies.get(0).getValue());
        } finally {
            webServer.stop().join();
        }
    }

    @Test
    public void setsTwoOutboundCookies() throws IOException, InterruptedException {
        WebServer webServer = createWebServer(59504)
                .add(new HttpHandler() {
                    @Override
                    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                        response.cookie(new HttpCookie("a", "b")).cookie(new HttpCookie("c", "d")).end();
                    }
                })
                .start();
        try {
            URLConnection urlConnection = httpGet(webServer, "/");
            List<HttpCookie> cookies = cookies(urlConnection);
            assertEquals(2, cookies.size());
            assertEquals("a", cookies.get(0).getName());
            assertEquals("b", cookies.get(0).getValue());
            assertEquals("c", cookies.get(1).getName());
            assertEquals("d", cookies.get(1).getValue());
        } finally {
            webServer.stop().join();
        }
    }

    @Test
    public void parsesOneInboundCookie() throws IOException, InterruptedException {
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

    @Test
    public void parsesTwoInboundCookie() throws IOException, InterruptedException {
        WebServer webServer = createWebServer(59504)
                .add(new HttpHandler() {
                    @Override
                    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                        String body = "Your cookies:";
                        List<HttpCookie> cookies = sort(request.cookies());
                        for (HttpCookie cookie : cookies) {
                            body += " " + cookie.getName();
                        }
                        response.header("Content-Length", body.length())
                                .content(body)
                                .end();
                    }
                })
                .start();
        try {
            URLConnection urlConnection = httpGet(webServer, "/");
            urlConnection.addRequestProperty("Cookie", new HttpCookie("someName", "someValue").toString());
            urlConnection.addRequestProperty("Cookie", new HttpCookie("me", "too").toString());
            assertEquals("Your cookies: me someName", contents(urlConnection));
        } finally {
            webServer.stop().join();
        }
    }

    private List<HttpCookie> cookies(URLConnection urlConnection) {
        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        Map<String,List<String>> headerFields = urlConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> header : headerFields.entrySet()) {
            if("Set-Cookie".equals(header.getKey())) {
                List<String> value = header.getValue();
                for (String cookie : value) {
                    cookies.addAll(HttpCookie.parse(cookie));
                }
            }
        }
        return sort(cookies);
    }

    private List<HttpCookie> sort(List<HttpCookie> cookies) {
        Collections.sort(cookies, new Comparator<HttpCookie>() {
            @Override
            public int compare(HttpCookie a, HttpCookie b) {
                return a.getName().compareTo(b.getName());
            }
        });
        return cookies;       
    }
}
