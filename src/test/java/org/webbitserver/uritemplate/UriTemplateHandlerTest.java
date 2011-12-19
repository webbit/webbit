package org.webbitserver.uritemplate;

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

public class UriTemplateHandlerTest {
    @Test
    public void extracts_values_from_uri_patterns() throws Exception {
        UriTemplateHandler uth = new UriTemplateHandler("/foo/{name}/bar/{id}", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                assertEquals("hello", request.data("name"));
                assertEquals("96", request.data("id"));
            }
        });
        HttpRequest req = new StubHttpRequest("/foo/hello/bar/96");
        uth.handleHttpRequest(req, null, null);
    }

    @Test
    public void calls_next_when_no_match() throws Exception {
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
