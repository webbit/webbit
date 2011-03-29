package org.webbitserver.testutil;

import org.webbitserver.WebServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class HttpClient {

    private static final int BUFFER_SIZE_IF_NO_CONTENT_LENGTH_HEADER = 1024;

    public static URLConnection httpGet(WebServer webServer, String path) throws IOException {
        URL url = new URL(webServer.getUri().toURL(), path);
        return url.openConnection();
    }

    public static URLConnection httpPost(WebServer webServer, String path, String body) throws IOException {
        URL url = new URL(webServer.getUri().toURL(), path);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setDoOutput(true);
        urlConnection.getOutputStream().write(body.getBytes(Charset.forName("UTF8")));
        return urlConnection;
    }

    public static String contents(URLConnection urlConnection) throws IOException {
        int length = urlConnection.getContentLength();
        byte[] buffer = new byte[length == -1 ? BUFFER_SIZE_IF_NO_CONTENT_LENGTH_HEADER : length];
        int realLength = urlConnection.getInputStream().read(buffer);
        if (length != -1 && length != realLength) {
            throw new IOException("Content-Length header (" + length + ") did not match actual length (" + realLength + ")");
        }
        return new String(buffer, 0, realLength);
    }


}
