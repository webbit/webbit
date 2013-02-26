package org.webbitserver.handler;

import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class PathMatchHandlerTest {
    @Test
    public void matchesRequestWithFullUri() throws Exception {
        HttpHandler handler = mock(HttpHandler.class);
        PathMatchHandler pmh = new PathMatchHandler("/hello", handler);

        HttpRequest req = new StubHttpRequest("http://host.com:8080/hello");
        HttpResponse res = new StubHttpResponse();
        HttpControl ctl = new StubHttpControl();

        pmh.handleHttpRequest(req, res, ctl);
        verify(handler).handleHttpRequest(req, res, ctl);
    }

    @Test
    public void matchesRequestWithPathOnly() throws Exception {
        HttpHandler handler = mock(HttpHandler.class);
        PathMatchHandler pmh = new PathMatchHandler("/hello", handler);

        HttpRequest req = new StubHttpRequest("/hello");
        HttpResponse res = new StubHttpResponse();
        HttpControl ctl = new StubHttpControl();

        pmh.handleHttpRequest(req, res, ctl);
        verify(handler).handleHttpRequest(req, res, ctl);
    }

    @Test
    public void matchesRequestWithRegexpPath() throws Exception {
        HttpHandler handler = mock(HttpHandler.class);
        PathMatchHandler pmh = new PathMatchHandler("/hello/.*", handler);

        HttpRequest req = new StubHttpRequest("/hello/world");
        HttpResponse res = new StubHttpResponse();
        HttpControl ctl = new StubHttpControl();

        pmh.handleHttpRequest(req, res, ctl);
        verify(handler).handleHttpRequest(req, res, ctl);
    }

    @Test
    public void handsOffWhenNoMatch() throws Exception {
        HttpHandler handler = mock(HttpHandler.class);
        PathMatchHandler pmh = new PathMatchHandler("/hello", handler);

        HttpRequest req = new StubHttpRequest("http://hello.com:8080/wtf");
        HttpResponse res = new StubHttpResponse();
        HttpControl ctl = mock(HttpControl.class);

        pmh.handleHttpRequest(req, res, ctl);

        verifyZeroInteractions(handler);
        verify(ctl).nextHandler();
    }

}

