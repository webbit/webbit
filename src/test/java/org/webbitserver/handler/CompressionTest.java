package org.webbitserver.handler;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.decompressContents;
import static org.webbitserver.testutil.HttpClient.httpGetAcceptCompressed;
import static org.webbitserver.testutil.HttpClient.httpPostCompressed;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;

public class CompressionTest {

    private final WebServer webServer = createWebServer(59504);

    private final String content = "Very short string for which there is no real point in compressing, but we're going to do it anyway.";

    @After
    public void die() throws InterruptedException, ExecutionException {
        webServer.stop().get();
    }

    @Test
    public void compressedPostIsUncompressedProperly() throws IOException, ExecutionException, InterruptedException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
                    throws Exception {
                response.content(request.body()).end();
            }
        }).start().get();
        String result = contents(httpPostCompressed(webServer, "/", content));
        assertEquals(content, result);
    }

    @Test
    public void compressedResponseIsSentProperly() throws IOException, ExecutionException, InterruptedException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
                    throws Exception {
                response.content(content).end();
            }
        }).start().get();
        HttpURLConnection urlConnection = (HttpURLConnection) httpGetAcceptCompressed(webServer, "/");
        String result = decompressContents(urlConnection);
        assertEquals(content, result);
        assertEquals("gzip", urlConnection.getContentEncoding());
    }

}
