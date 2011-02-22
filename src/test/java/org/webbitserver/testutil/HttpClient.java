package org.webbitserver.testutil;

import org.webbitserver.WebServer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class HttpClient {

    public static URLConnection httpGet(WebServer webServer, String path) throws IOException {
        URL url = new URL(webServer.getUri().toURL(), path);
        return url.openConnection();
    }

    public static String contents(URLConnection urlConnection) throws IOException {
        byte[] buffer = new byte[urlConnection.getContentLength()];
        urlConnection.getInputStream().read(buffer);
        return new String(buffer);
    }


}
