package org.webbitserver.rest;

import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.webbitserver.rest.Rest.params;

public class UriTemplateHandlerTest {
    @Test
    public void extractsValuesFromUriPatterns() throws Exception {
        UriTemplateHandler uth = new UriTemplateHandler("/foo/{name}/bar/{id}", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                assertEquals("hello", params(request).get("name"));
                assertEquals("96", params(request).get("id"));
            }
        });
        HttpRequest req = new StubHttpRequest("/foo/hello/bar/96");
        uth.handleHttpRequest(req, null, null);
    }

    @Test
    public void callsNextWhenNoMatch() throws Exception {
        UriTemplateHandler uth = new UriTemplateHandler("/foo/{name}/bar/{id}", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                fail("Shouldn't get here");
            }
        });
        HttpRequest req = new StubHttpRequest("/fooh/ello/bar/96");
        HttpResponse res = new StubHttpResponse();
        HttpControl control = new StubHttpControl(req, res);
        uth.handleHttpRequest(req, res, control);
        assertEquals(404, res.status());
    }
}
