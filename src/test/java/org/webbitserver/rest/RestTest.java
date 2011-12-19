package org.webbitserver.rest;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.rest.Rest.param;
import static org.webbitserver.rest.Rest.params;
import static org.webbitserver.rest.Rest.redirect;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;

public class RestTest {
    private WebServer webServer = createWebServer(59504);
    private Rest rest = new Rest(webServer);

    @After
    public void die() throws IOException, InterruptedException {
        webServer.stop().join();
    }

    @Test
    public void exposesBodyInRequest() throws IOException, InterruptedException {
        rest.GET("/people/{name}/pets/{petName}", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content(String.format("Name: %s\nPet: %s\n", params(request).get("name"), params(request).get("petName"))).end();
            }
        });
        webServer.start();
        String result = contents(httpGet(webServer, "/people/Mickey/pets/Pluto"));
        assertEquals("Name: Mickey\nPet: Pluto\n", result);
    }

    @Test
    public void providesEasyRedirectApi() throws IOException, InterruptedException {
        final String petUri = "/people/{name}/pets/{petName}";

        rest.GET("/people/{name}/animals/{petName}", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                redirect(response, petUri,
                        "name", (String) params(request).get("name"),
                        "petName", (String) params(request).get("petName")
                );
            }
        });

        rest.GET(petUri, new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.content(String.format("Name: %s\nPet: %s\n", param(request, "name"), param(request, "petName"))).end();
            }
        });
        webServer.start();
        String result = contents(httpGet(webServer, "/people/Mickey/animals/Pluto"));
        assertEquals("Name: Mickey\nPet: Pluto\n", result);
    }
}
