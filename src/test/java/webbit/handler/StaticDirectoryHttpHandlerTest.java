package webbit.handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import webbit.HttpHandler;
import webbit.WebServer;
import webbit.async.RResult;
import webbit.async.filesystem.FileSystem;
import webbit.async.filesystem.JavaFileSystem;
import webbit.netty.NettyWebServer;
import webbit.stub.StubHttpRequest;
import webbit.stub.StubHttpResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StaticDirectoryHttpHandlerTest {

    private FileSystem fs;
    private HttpHandler handler;

    @Test
    public void should404ForMissingFiles() throws Exception {
        assertReturnedWithStatus(404, handle(request("/index.html")));
        assertReturnedWithStatus(404, handle(request("/notfound.html")));
        assertReturnedWithStatus(404, handle(request("/foo/bar")));
    }

    @Test
    public void shouldServeExistingFiles() throws Exception {
        writeFile("index.html", "Hello world");
        writeFile("foo.js", "Blah");
        mkdir("/a/b");
        writeFile("a/b/good", "hi");
        assertReturnedWithStatus(200, handle(request("/index.html")));
        assertReturnedWithStatus(200, handle(request("/foo.js")));
        assertReturnedWithStatus(404, handle(request("/notfound.html")));
        assertReturnedWithStatus(200, handle(request("/a/b/good")));
        assertReturnedWithStatus(404, handle(request("/a/b/bad")));
    }

    @Test
    public void shouldServesWelcomePagesForDirectories() throws Exception {
        assertReturnedWithStatus(404, handle(request("/")));
        assertReturnedWithStatus(404, handle(request("/a")));
        assertReturnedWithStatus(404, handle(request("/a/")));
        assertReturnedWithStatus(404, handle(request("/b")));

        writeFile("index.html", "hi");
        assertReturnedWithStatus(200, handle(request("/")));
        assertReturnedWithStatus(404, handle(request("/a")));
        assertReturnedWithStatus(404, handle(request("/a/")));
        assertReturnedWithStatus(404, handle(request("/b")));

        mkdir("a");
        mkdir("b");
        assertReturnedWithStatus(200, handle(request("/")));
        assertReturnedWithStatus(404, handle(request("/a")));
        assertReturnedWithStatus(404, handle(request("/a/")));
        assertReturnedWithStatus(404, handle(request("/b")));

        writeFile("a/index.html", "hi");
        assertReturnedWithStatus(200, handle(request("/")));
        assertReturnedWithStatus(200, handle(request("/a")));
        assertReturnedWithStatus(200, handle(request("/a/")));
        assertReturnedWithStatus(404, handle(request("/b")));

        writeFile("b/index.html", "hi");
        assertReturnedWithStatus(200, handle(request("/")));
        assertReturnedWithStatus(200, handle(request("/a")));
        assertReturnedWithStatus(200, handle(request("/a/")));
        assertReturnedWithStatus(200, handle(request("/b")));
    }
    
    @Test
    public void shouldGuessMimeTypeFromExtension() throws Exception {
        mkdir("a/b");
        writeFile("index.html", "Blah");
        writeFile("file.HTML", "Blah");
        writeFile("file2.hTM", "Blah");
        writeFile("foo.txt", "Blah");
        writeFile("foo.png", "Blah");
        writeFile("a/b/foo.js", "Blah");
        writeFile("a/b/foo.css.js", "Blah");
        writeFile("nosuffix", "Blah");
        writeFile("foo.unknown", "Blah");
        writeFile(".x", "Blah");
        writeFile("x.", "Blah");

        assertEquals("text/html; charset=UTF-8", handle(request("/index.html")).header("Content-Type"));
        assertEquals("text/html; charset=UTF-8", handle(request("/file.HTML")).header("Content-Type"));
        assertEquals("text/html; charset=UTF-8", handle(request("/file2.hTM")).header("Content-Type"));
        assertEquals("text/plain; charset=UTF-8", handle(request("/foo.txt")).header("Content-Type"));
        assertEquals("image/png", handle(request("/foo.png")).header("Content-Type"));
        assertEquals("text/javascript; charset=UTF-8", handle(request("/a/b/foo.js")).header("Content-Type"));
        assertEquals("text/javascript; charset=UTF-8", handle(request("/a/b/foo.css.js")).header("Content-Type"));
        assertEquals(null, handle(request("nosuffix")).header("Content-Type"));
        assertEquals(null, handle(request("foo.unknown")).header("Content-Type"));
        assertEquals(null, handle(request(".x")).header("Content-Type"));
        assertEquals(null, handle(request("x.")).header("Content-Type"));
    }

    /**
     * End to end integration test, that fires up a real web server and uses HTTP to check
     * the responses.
     */
    @Test
    public void shouldWorkInRealServer() throws IOException, InterruptedException {
        writeFile("index.html", "Hello world");
        writeFile("foo.js", "some js");
        mkdir("some/dir");
        writeFile("some/dir/content1.txt", "some txt");

        WebServer webServer = new NettyWebServer(Executors.newSingleThreadExecutor(), 55554, handler);
        webServer.start();
        try {
            assertEquals("Hello world", contents(httpGet(webServer, "/index.html")));
            assertEquals("some js", contents(httpGet(webServer, "/foo.js")));
            assertEquals("some txt", contents(httpGet(webServer, "/some/dir/content1.txt")));
        } finally {
            webServer.stop();
            webServer.join();
        }
    }

    // --- Test helpers

    /**
     * Create temporary directory on disk, to store files in.
     */
    @Before
    public void createWorkingDir() throws IOException {
        File dir = new File(System.getProperty("java.io.tmpdir"), getClass().getName() + "-" + Math.random());
        assertTrue(dir.mkdirs());
        fs = new JavaFileSystem(dir);
        handler = new StaticDirectoryHttpHandler(fs);
    }

    /**
     * Clean up working dir at end of test.
     */
    @After
    public void cleanUpWorkingDir() throws IOException {
        fs.delete(".", true, new RResult<Boolean>());
    }

    /**
     * Write text file to FileSystem.
     */
    private void writeFile(String path, String contents) {
        fs.writeText(path, Charset.forName("UTF-8"), contents, new RResult<Void>());
    }

    /**
     * Mkdir on disk
     */
    private void mkdir(String path) {
        fs.mkdir(path, true, new RResult<Boolean>());
    }

    private URLConnection httpGet(WebServer webServer, String path) throws IOException {
        URL url = new URL(webServer.getUri().toURL(), path);
        return url.openConnection();
    }

    /**
     * Create stubbed request.
     */
    private StubHttpRequest request(String uri) {
        return new StubHttpRequest(uri);
    }

    /**
     * Send stub request to handler, and return a stubbed response for inspection.
     */
    private StubHttpResponse handle(StubHttpRequest httpRequest) throws Exception {
        StubHttpResponse response = new StubHttpResponse();
        handler.handleHttpRequest(httpRequest, response);
        return response;
    }

    private void assertReturnedWithStatus(int expectedStatus, StubHttpResponse response) {
        assertEquals(expectedStatus, response.status());
        assertTrue(response.ended());
        assertNull(response.error());
    }

    private String contents(URLConnection urlConnection) throws IOException {
        byte[] buffer = new byte[urlConnection.getContentLength()];
        urlConnection.getInputStream().read(buffer);
        return new String(buffer);
    }
}
