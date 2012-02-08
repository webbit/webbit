package org.webbitserver.handler;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;

public class CookieTest {
    private WebServer webServer = createWebServer(59504);

    @After
    public void die() throws InterruptedException, ExecutionException {
        webServer.stop().get();
    }

    @Test
    public void setsOneOutboundCookie() throws IOException, InterruptedException, ExecutionException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.cookie(new HttpCookie("a", "b")).end();
            }
        }).start().get();
        URLConnection urlConnection = httpGet(webServer, "/");
        List<HttpCookie> cookies = cookies(urlConnection);
        assertEquals(1, cookies.size());
        assertEquals("a", cookies.get(0).getName());
        assertEquals("b", cookies.get(0).getValue());
    }

    @Test
    public void setsTwoOutboundCookies() throws IOException, InterruptedException, ExecutionException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.cookie(new HttpCookie("a", "b")).cookie(new HttpCookie("c", "d")).end();
            }
        }).start().get();
        URLConnection urlConnection = httpGet(webServer, "/");
        List<HttpCookie> cookies = cookies(urlConnection);
        assertEquals(2, cookies.size());
        assertEquals("a", cookies.get(0).getName());
        assertEquals("b", cookies.get(0).getValue());
        assertEquals("c", cookies.get(1).getName());
        assertEquals("d", cookies.get(1).getValue());
    }

    @Test
    public void parsesOneInboundCookie() throws IOException, InterruptedException, ExecutionException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                String body = "Your cookie value: " + request.cookieValue("someName");
                response.header("Content-Length", body.length())
                        .content(body)
                        .end();
            }
        }).start().get();
        URLConnection urlConnection = httpGet(webServer, "/");
        urlConnection.addRequestProperty("Cookie", new HttpCookie("someName", "someValue").toString());
        assertEquals("Your cookie value: someValue", contents(urlConnection));
    }

    @Test
    public void parsesThreeInboundCookiesInTwoHeaders() throws IOException, InterruptedException, ExecutionException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                String body = "Your cookies:";
                List<HttpCookie> cookies = sort(request.cookies());
                for (HttpCookie cookie : cookies) {
                    body += " " + cookie.getName() + "=" + cookie.getValue();
                }
                response.header("Content-Length", body.length())
                        .content(body)
                        .end();
            }
        }).start().get();
        URLConnection urlConnection = httpGet(webServer, "/");
        urlConnection.addRequestProperty("Cookie", new HttpCookie("a", "b").toString());
        urlConnection.addRequestProperty("Cookie", new HttpCookie("c", "\"d").toString() + "; " + new HttpCookie("e", "f").toString());
        assertEquals("Your cookies: a=b c=\"d e=f", contents(urlConnection));
    }

    @Test
    public void behavesWellWhenThereAreNoInboundCookies() throws IOException, ExecutionException, InterruptedException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                String body = "Cookie count:" + request.cookies().size();
                response.header("Content-Length", body.length())
                        .content(body)
                        .end();
            }
        }).start().get();
        URLConnection urlConnection = httpGet(webServer, "/");
        assertEquals("Cookie count:0", contents(urlConnection));
    }

    // You wouldn't have thought it was that convoluted, but it is.
    private List<HttpCookie> cookies(URLConnection urlConnection) {
        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> header : headerFields.entrySet()) {
            if ("Set-Cookie".equals(header.getKey())) {
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
