package org.webbitserver.handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;

public class EmbeddedResourceHandlerTest {
    private WebServer webServer = createWebServer(59504);
    private AbstractResourceHandler handler;
    private Executor immediateExecutor;

    @Before
    public void createHandler() {
        immediateExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
        handler = new EmbeddedResourceHandler("web", immediateExecutor, getClass());
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
    public void listsDirectory() throws Exception {
        handler.enableDirectoryListing(true).welcomeFile("doesnotexist");

        StubHttpResponse response = handle(request("/"));
        assertEquals(200, response.status());
        assertThat(response.contentsString(), containsString("index.html"));
        assertThat(response.contentsString(), containsString("jquery-1.5.2.js"));
        assertThat(response.contentsString(), not(containsString("EmbeddedResourceHandlerTest")));
    }

    @Test
    public void listsSubDirectory() throws Exception {
      handler.enableDirectoryListing(true).welcomeFile("doesnotexist");

      StubHttpResponse response = handle(request("/"));
      assertEquals(200, response.status());
      // &#x2F; is a /
      assertThat(response.contentsString(), containsString("href=\"subdir&#x2F;\""));
      assertThat(response.contentsString(), not(containsString("subfile.txt")));

      response = handle(request("/subdir/"));
      assertEquals(200, response.status());
      assertThat(response.contentsString(), containsString("subfile.txt"));
      assertThat(response.contentsString(), not(containsString("index.html")));
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
    public void canUseTemplateEngine() throws IOException, InterruptedException, ExecutionException {
        handler = new EmbeddedResourceHandler("web", immediateExecutor, getClass(), new TemplateEngine() {
            @Override
            public byte[] process(byte[] template, String templatePath, Object templateContext) {
                String templateSource = new String(template, Charset.forName("UTF-8"));
                String context = templateContext.toString();
                try {
                    return (templateSource+context).getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException();
                }
            }
        });
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                request.data(TemplateEngine.TEMPLATE_CONTEXT, "THE CONTEXT");
                control.nextHandler();
            }
        });
        webServer.add(handler).start().get();
        assertEquals("Hello worldTHE CONTEXT", contents(httpGet(webServer, "/index.html")));
    }

    @Test
    public void shouldWorkWithBiggerFilesUsingEmbedded() throws IOException, InterruptedException, ExecutionException {
        webServer.add(handler).start().get();
        String jquery = contents(httpGet(webServer, "/jquery-1.5.2.js"));
        if (!jquery.trim().endsWith("})(window);")) {
            fail("Ended with:[" + jquery.substring(jquery.length() - 200, jquery.length()) + "]");
        }
    }

    @Test
    public void shouldWorkWithBiggerFilesUsingFileHandler() throws IOException, InterruptedException, ExecutionException {
        handler = new StaticFileHandler("src/test/resources/web");
        webServer.add(handler).start().get();

        String jquery = contents(httpGet(webServer, "/jquery-1.5.2.js"));
        if (!jquery.trim().endsWith("})(window);")) {
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
