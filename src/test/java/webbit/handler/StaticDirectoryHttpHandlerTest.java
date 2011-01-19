package webbit.handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import webbit.HttpHandler;
import webbit.WebServer;
import webbit.netty.NettyWebServer;
import webbit.stub.StubHttpRequest;
import webbit.stub.StubHttpResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StaticDirectoryHttpHandlerTest {

    private File dir;
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
    public void shouldIgnoreQueryParams() throws Exception {
        writeFile("index.html", "Hello world");
        writeFile("foo.js", "Blah");
        mkdir("/a/b");
        writeFile("a/b/good", "hi");
        assertReturnedWithStatus(200, handle(request("/index.html?foo=x")));
        assertReturnedWithStatus(200, handle(request("/?foo=x")));
        assertReturnedWithStatus(200, handle(request("/foo.js?sdfsd")));
        assertReturnedWithStatus(200, handle(request("/a/b/good?xx")));
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
        writeFile("a/b/index.html", "Blah"); // <-- welcome file
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
        assertEquals("text/html; charset=UTF-8", handle(request("/a/b/")).header("Content-Type")); // <-- welcome file
        assertEquals("text/javascript; charset=UTF-8", handle(request("/a/b/foo.js")).header("Content-Type"));
        assertEquals("text/javascript; charset=UTF-8", handle(request("/a/b/foo.css.js")).header("Content-Type"));
        assertEquals(null, handle(request("nosuffix")).header("Content-Type"));
        assertEquals(null, handle(request("foo.unknown")).header("Content-Type"));
        assertEquals(null, handle(request(".x")).header("Content-Type"));
        assertEquals(null, handle(request("x.")).header("Content-Type"));
    }

    @Test
    public void shouldNotAllowAccessToFilesOutsideTheRoot() throws Exception {
        assertReturnedWithStatus(404, handle(request("../../etc/passwd")));
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
            assertEquals("some js", contents(httpGet(webServer, "/foo.js?xx=y")));
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
        dir = new File(System.getProperty("java.io.tmpdir"), getClass().getName() + "-" + Math.random());
        assertTrue(dir.mkdirs());
        Executor immediateExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
        handler = new StaticDirectoryHttpHandler(dir, immediateExecutor, immediateExecutor);
    }

    /**
     * Clean up working dir at end of test.
     */
    @After
    public void cleanUpWorkingDir() throws IOException {
        delete(dir);
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    /**
     * Write text file to FileSystem.
     */
    private void writeFile(String path, String contents) throws IOException {
        FileWriter writer = new FileWriter(new File(dir, path));
        try {
            writer.write(contents);
        } finally {
            writer.close();
        }
    }

    /**
     * Mkdir on disk
     */
    private void mkdir(String path) {
        new File(dir, path).mkdirs();
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
