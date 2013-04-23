package org.webbitserver;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;


import java.net.URLConnection;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.httpGet;

public class ChunkedResponseTest {
    private WebServer webServer = createWebServer(12345);

    @After
    public void die() throws InterruptedException, ExecutionException {
        webServer.stop().get();
    }

     @Test
    public void streamingViaChunks() throws Exception {
        webServer.add("/chunked", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, final HttpResponse res, HttpControl control) {
                control.execute(new Runnable() { 
                    public void run() {
                        res.chunked();
                        try{Thread.sleep(10);}catch (Exception ex){}
                        res.write("chunk1");
                        try{Thread.sleep(10);}catch (Exception ex){}
                        res.write("chunk2");
                        try{Thread.sleep(10);}catch (Exception ex){}
                        res.end();
                    }});
            }
        }).start().get();
        

        URLConnection conn = httpGet(webServer, "/chunked");

        assertTrue("should contain chunks", stringify(conn.getInputStream()).equals("chunk1chunk2"));
        assertTrue("should contain Transfer-Encoding header", conn.getHeaderFields().get("Transfer-Encoding") != null);
        assertTrue("should have chunked value in Transfer encoding header",conn.getHeaderFields().get("Transfer-Encoding").get(0).equals("chunked"));
    }

    private static String stringify(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
   
}