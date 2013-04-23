package org.webbitserver.handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.WebServer;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.contents;
import static org.webbitserver.testutil.HttpClient.httpGet;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Locale;
import java.util.Date;

public class StaticFileHandlerTest {

    private File dir;
    private StaticFileHandler handler;

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
    public void shouldSupportUnboundedEndRangeRequests() throws Exception {
        String contents = "the yellow fox jumped over the blue log";
        writeFile("some_file", contents);
        StubHttpRequest request = request("/some_file");
        request.header("Range", "bytes=0-");
        StubHttpResponse response = handle(request);
        assertReturnedWithStatus(206, response);
        assertEquals(String.valueOf(contents.length()), response.header("Content-Length"));
        assertEquals("bytes 0-" + (contents.length() - 1) + "/" + contents.length(), response.header("Content-Range"));
        assertEquals(contents, response.contentsString());
    }

    @Test
    public void shouldSupportUnboundedStartRangeRequests() throws Exception {
        String contents = "the yellow fox jumped over the blue log";
        writeFile("some_file", contents);
        StubHttpRequest request = request("/some_file");
        request.header("Range", "bytes=-8");
        StubHttpResponse response = handle(request);
        assertReturnedWithStatus(206, response);
        assertEquals(String.valueOf(8), response.header("Content-Length"));
        assertEquals("bytes 31-" + (contents.length() - 1) + "/" + contents.length(), response.header("Content-Range"));
        assertEquals("blue log", response.contentsString());
    }

    @Test
    public void shouldSupportBoundedRangeRequests() throws Exception {
        String contents = "the yellow fox jumped over the blue log";
        writeFile("some_file", contents);
        StubHttpRequest request = request("/some_file");
        request.header("Range", "bytes=4-9");
        StubHttpResponse response = handle(request);
        assertReturnedWithStatus(206, response);
        assertEquals("bytes 4-9" + "/" + contents.length(), response.header("Content-Range"));
        assertEquals("yellow", response.contentsString());
        assertEquals(String.valueOf(6), response.header("Content-Length"));
    }

    @Test
    public void shouldReturnInvalidRangeIfBeyondSizeOfContent() throws Exception {
        String contents = "the yellow fox jumped over the blue log";
        writeFile("some_file", contents);
        StubHttpRequest request = request("/some_file");
        request.header("Range", "bytes=1000-5000");
        StubHttpResponse response = handle(request);
        assertReturnedWithStatus(416, response);
        assertEquals("bytes *" + "/" + contents.length(), response.header("Content-Range"));
    }

    @Test
    public void shouldServesWelcomePagesForDirectories() throws Exception {
        assertReturnedWithStatus(404, handle(request("/")));
        assertReturnedWithStatus(404, handle(request("/a")));
        assertReturnedWithStatus(404, handle(request("/a/")));
        assertReturnedWithStatus(404, handle(request("/b")));

        String welcomeFileContents = "hi";

        writeFile("index.html", welcomeFileContents);
        assertReturnedWithStatusAndContainsContent(200, welcomeFileContents, handle(request("/")));
        assertReturnedWithStatus(404, handle(request("/a")));
        assertReturnedWithStatus(404, handle(request("/a/")));
        assertReturnedWithStatus(404, handle(request("/b")));

        mkdir("a");
        mkdir("b");
        assertReturnedWithStatusAndContainsContent(200, welcomeFileContents, handle(request("/")));
        assertReturnedWithStatus(301, handle(request("/a")));
        assertReturnedWithStatus(404, handle(request("/a/")));
        assertReturnedWithStatus(301, handle(request("/b")));

        writeFile("a/index.html", welcomeFileContents);
        assertReturnedWithStatusAndContainsContent(200, welcomeFileContents, handle(request("/")));
        assertReturnedWithStatus(301, handle(request("/a")));
        assertReturnedWithStatusAndContainsContent(200, welcomeFileContents, handle(request("/a/")));
        assertReturnedWithStatus(301, handle(request("/b")));

        writeFile("b/index.html", welcomeFileContents);
        assertReturnedWithStatusAndContainsContent(200, welcomeFileContents, handle(request("/")));
        assertReturnedWithStatus(301, handle(request("/a")));
        assertReturnedWithStatusAndContainsContent(200, welcomeFileContents, handle(request("/a/")));
        assertReturnedWithStatus(301, handle(request("/b")));
    }

    @Test
    public void shouldServeDirectoryListingForDirectories() throws Exception {
        writeFile("a.foo", "");
        handler.enableDirectoryListing(true);
        assertReturnedWithStatusAndContainsContent(200, "a.foo", handle(request("/")));
        
        mkdir("a");
        writeFile("a/a.foo", "");
        assertReturnedWithStatus(301, handle(request("/a")));
        assertReturnedWithStatusAndContainsContent(200, "a.foo", handle(request("/a/")));
    }

    @Test
    public void redirectAddingSlashPreservesQuery() throws Exception {
        mkdir("a");
        handler.enableDirectoryListing(true);
        assertReturnedLocationHeaderEqualTo("/a/", handle(request("/a")));
        assertReturnedLocationHeaderEqualTo("/a/?", handle(request("/a?")));
        assertReturnedLocationHeaderEqualTo("/a/?foo=bar?baz", handle(request("/a?foo=bar?baz")));
    }

    @Test
    public void escapesFilenames() throws Exception {
        String filename = "'";
        String escapedFilename = "&#x27;";
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            filename += "&<>\"";
            escapedFilename += "&amp;&lt;&gt;&quot;";
        }
        writeFile(filename, "");
        handler.enableDirectoryListing(true);
        String response = handle(request("/")).contentsString();
        assertThat(response, containsString(escapedFilename));
    }

    @Test
    public void allowsCustomDirectoryListingFormatters() throws Exception {
        mkdir("a");
        handler.enableDirectoryListing(true, new DirectoryListingFormatter() {
            @Override
            public byte[] formatFileListAsHtml(Iterable<FileEntry> files) throws IOException {
                return "Monkeys".getBytes("UTF-8");
            }
        });
        assertReturnedWithStatusAndContainsContent(200, "Monkeys", handle(request("/a/")));
    }
    
    @Test
    public void prefersWelcomeFileToDirectoryListing() throws Exception {
        writeFile("a.foo", "");
        writeFile("index.html", "hi");
        handler.enableDirectoryListing(true);
        assertReturnedWithStatusAndContainsContent(200, "hi", handle(request("/")));
    }

    @Test 
    public void shouldHandleCacheHeaders() throws Exception {
        mkdir("a/b");
        writeFile("index_cache.html", "Blah");
        Long  aYearAgo = (new Date()).getTime() - (365 * 24 * 60 * 60 * 1000); 
        Long  aYearFromNow = (new Date()).getTime() + (365 * 24 * 60 * 60 * 1000); 
        assertEquals(true, handle(request("/index_cache.html")).header("Last-Modified") != null);
        assertEquals(true, handle(request("/index_cache.html")).header("ETag") != null);
        assertEquals(true, handle(request("/index_cache.html")).header("Cache-Control") != null);
        assertEquals(true, handle(request("/index_cache.html")).header("Cache-Control").contains("max-age=3600, public"));
        assertEquals(true, handle(request("/index_cache.html")).header("Expires") != null);
        assertEquals(true, handleWithHeader(request("/index_cache.html"), "If-Modified-Since", toDateHeader(new Date(aYearAgo))).status() == 200);
        assertEquals(true, handleWithHeader(request("/index_cache.html"), "If-Modified-Since", toDateHeader(new Date(aYearFromNow))).status() == 304);
    }

    @Test
    public void shouldGuessMimeTypeFromExtension() throws Exception {
        mkdir("a/b");
        writeFile("index.html", "Blah");
        writeFile("file.HTML", "Blah");
        writeFile("file.swf", "Blah");
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

        assertEquals("application/x-shockwave-flash", handle(request("/file.swf")).header("Content-Type"));
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
    public void shouldWorkInRealServer() throws IOException, InterruptedException, ExecutionException {
        writeFile("index.html", "Hello world");
        writeFile("foo.js", "some js");
        mkdir("some/dir");
        writeFile("some/dir/content1.txt", "some txt");

        WebServer webServer = createWebServer(59504)
                .add(handler)
                .start()
                .get();
        try {
            assertEquals("Hello world", contents(httpGet(webServer, "/index.html")));
            assertEquals("some js", contents(httpGet(webServer, "/foo.js?xx=y")));
            assertEquals("some txt", contents(httpGet(webServer, "/some/dir/content1.txt")));
        } finally {
            webServer.stop().get();
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
        handler = new StaticFileHandler(dir, immediateExecutor, 3600);
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
    private StubHttpResponse handleWithHeader(StubHttpRequest request, String headerName, String headerValue) throws Exception {
        StubHttpResponse response = new StubHttpResponse();
        request.header(headerName, headerValue);
        handler.handleHttpRequest(request, response, new StubHttpControl(request, response));
        return response;
    }
    private String toDateHeader(Date date) {
            SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return httpDateFormat.format(date);
    }

    private void assertReturnedWithStatus(int expectedStatus, StubHttpResponse response) {
        assertEquals(expectedStatus, response.status());
        assertTrue(response.ended());
        assertNull(response.error());
    }

    private void assertReturnedWithStatusAndContainsContent(int expectedStatus, String content, StubHttpResponse response) {
        assertReturnedWithStatus(expectedStatus, response);
        assertThat(response.contentsString(), containsString(content));
    }

    private void assertReturnedLocationHeaderEqualTo(String locationHeaderValue, StubHttpResponse response) {
        assertEquals(locationHeaderValue, response.header("Location"));
    }
}
