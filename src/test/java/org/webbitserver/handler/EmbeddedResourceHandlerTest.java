package org.webbitserver.handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.HttpHandler;
import org.webbitserver.WebServer;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;

public class EmbeddedResourceHandlerTest {
    private WebServer webServer = createWebServer(59504);
    private HttpHandler handler;

    @Before
    public void createHandler() {
        Executor immediateExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
        handler = new EmbeddedResourceHandler("web", immediateExecutor);
    }

    @After
    public void stop() throws InterruptedException, ExecutionException {
        webServer.stop().get();
    }

    @Test
    public void should404ForMissingFiles() throws Exception {
        assertReturnedWithStatus(200, handle(request("/index.html")));
        assertReturnedWithStatus(200, handle(request("/index.html?x=y")));
        assertReturnedWithStatus(404, handle(request("/notfound.html")));
        assertReturnedWithStatus(404, handle(request("/foo/bar")));
    }

    @Test
    public void shouldFindWelcomeFile() throws Exception {
        assertReturnedWithStatus(200, handle(request("/")));
    }

    @Test
    public void shouldWorkInRealServer() throws IOException, InterruptedException, ExecutionException {
        webServer.add(handler).start().get();
        assertEquals("Hello world", contents(httpGet(webServer, "/index.html")));
        assertEquals("Hello world", contents(httpGet(webServer, "/index.html?x=y")));
    }

    @Test
    public void shouldWorkWithBiggerFilesUsingEmbedded() throws IOException, InterruptedException, ExecutionException {
        webServer.add(handler).start().get();
        String jquery = contents(httpGet(webServer, "/jquery-1.5.2.js"));
        if (!jquery.endsWith("})(window);\n")) {
            fail("Ended with:[" + jquery.substring(jquery.length() - 200, jquery.length()) + "]");
        }
    }

    @Test
    public void shouldWorkWithBiggerFilesUsingFileHandler() throws IOException, InterruptedException, ExecutionException {
        handler = new StaticFileHandler("src/test/resources/web");
        webServer.add(handler).start().get();

        String jquery = contents(httpGet(webServer, "/jquery-1.5.2.js"));
        if (!jquery.endsWith("})(window);\n")) {
            fail("Ended with:[" + jquery.substring(jquery.length() - 200, jquery.length()) + "]");
        }
    }

    @Test
    public void shouldFindWelcomeFileInRealServer() throws IOException, InterruptedException, ExecutionException {
        webServer.add(handler).start().get();
        assertEquals("Hello world", contents(httpGet(webServer, "/")));
    }

    // --- Test helpers

    /**
     * Create stubbed request.
     */
    private StubHttpRequest request(String uri) {
        return new StubHttpRequest(uri);
    }

    /**
     * Send stub request to handler, and return a stubbed response for inspection.
     */
    private StubHttpResponse handle(StubHttpRequest request) throws Exception {
        StubHttpResponse response = new StubHttpResponse();
        handler.handleHttpRequest(request, response, new StubHttpControl(request, response));
        return response;
    }

    private void assertReturnedWithStatus(int expectedStatus, StubHttpResponse response) {
        assertEquals(expectedStatus, response.status());
        assertTrue(response.ended());
        assertNull(response.error());
    }

}
