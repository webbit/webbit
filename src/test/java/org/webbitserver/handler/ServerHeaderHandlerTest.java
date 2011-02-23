package org.webbitserver.handler;

import org.junit.Test;
import org.webbitserver.WebServer;

import java.io.IOException;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;

public class ServerHeaderHandlerTest {

    @Test
    public void setsHttpServerHeader() throws IOException, InterruptedException {
        WebServer webServer = createWebServer(59504)
                .add(new ServerHeaderHandler("My Server"))
                .add(new StringHttpHandler("text/plain", "body"))
                .start();
        try {
            URLConnection urlConnection = httpGet(webServer, "/");
            assertEquals("My Server", urlConnection.getHeaderField("Server"));
            assertEquals("body", contents(urlConnection));
        } finally {
            webServer.stop().join();
        }
    }

    @Test
    public void canBeOverriddenByOtherHandlers() throws IOException, InterruptedException {
        WebServer webServer = createWebServer(59504)
                .add(new ServerHeaderHandler("My Server"))
                .add(new ServerHeaderHandler("No actually, this is My Server"))
                .add(new StringHttpHandler("text/plain", "body"))
                .start();
        try {
            URLConnection urlConnection = httpGet(webServer, "/");
            assertEquals("No actually, this is My Server", urlConnection.getHeaderField("Server"));
            assertEquals("body", contents(urlConnection));
        } finally {
            webServer.stop().join();
        }
    }

    @Test
    public void canBeClearedByOtherHandlers() throws IOException, InterruptedException {
        WebServer webServer = createWebServer(59504)
                .add(new ServerHeaderHandler("My Server"))
                .add(new ServerHeaderHandler(null))
                .add(new StringHttpHandler("text/plain", "body"))
                .start();
        try {
            URLConnection urlConnection = httpGet(webServer, "/");
            assertFalse(urlConnection.getHeaderFields().containsKey("Server"));
            assertEquals("body", contents(urlConnection));
        } finally {
            webServer.stop().join();
        }
    }
}
