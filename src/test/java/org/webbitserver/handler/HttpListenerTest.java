package org.webbitserver.handler;

import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpListener;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;
import static org.webbitserver.WebServers.createWebServer;
import java.util.ArrayList;
import java.util.List;
import static org.webbitserver.testutil.HttpClient.httpGet;
import static org.webbitserver.testutil.HttpClient.contents;
import java.net.URLConnection;

public class HttpListenerTest {

    private WebServer webServer = createWebServer(59501);
    private List<String> ids = new ArrayList<String>();

    @Test
    public void listenerShouldBeInvoked() throws IOException, InterruptedException, ExecutionException {
        webServer
                .add("/foo", new HttpListener() {
                    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
                            throws Exception {
                        ids.add("handleHttpRequest");
                        response.end();
                    }
                    public void onOpen (Integer channelId, HttpRequest request) {
                        ids.add(Integer.toString(channelId));
                    }
                    public void onClose (Integer channelId) {
                        ids.add(Integer.toString(channelId));
                        ids.add("end");
                    }
                }).start().get();
        URLConnection urlConnection = httpGet(webServer, "/foo"); 
        contents(urlConnection);      
        webServer.stop().get();  
        assertTrue(ids.size() == 4);
        assertTrue(ids.get(1).equals("handleHttpRequest"));
        assertTrue(ids.get(0).equals(ids.get(2)));
        assertTrue(ids.get(3).equals("end"));
    }
}
