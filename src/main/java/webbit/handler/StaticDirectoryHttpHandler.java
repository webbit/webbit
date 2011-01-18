package webbit.handler;

import webbit.HttpHandler;
import webbit.HttpRequest;
import webbit.HttpResponse;
import webbit.async.Result;
import webbit.async.filesystem.AsyncFileSystem;
import webbit.async.filesystem.FileStat;
import webbit.async.filesystem.FileSystem;
import webbit.async.filesystem.JavaFileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class StaticDirectoryHttpHandler implements HttpHandler {

    public static final Map<String, String> DEFAULT_MIME_TYPES;

    static {
        // This is not an exhaustive list, just the most common types. Call registerMimeType() to add more.
        Map<String, String> mimeTypes = new HashMap<String, String>();
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("csv", "text/csv");
        mimeTypes.put("htm", "text/html");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("xml", "text/xml");
        mimeTypes.put("js", "text/javascript"); // Technically it should be application/javascript (RFC 4329), but IE8 struggles with that
        mimeTypes.put("xhtml", "application/xhtml+xml");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("zip", "application/zip");
        mimeTypes.put("tar", "application/x-tar");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("tiff", "image/tiff");
        mimeTypes.put("tif", "image/tiff");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("svg", "image/svg+xml");
        mimeTypes.put("ico", "image/vnd.microsoft.icon");
        DEFAULT_MIME_TYPES = Collections.unmodifiableMap(mimeTypes);
    }

    private static final String DEFAULT_WELCOME_FILE = "index.html";

    private final FileSystem fileSystem;
    private final Map<String, String> mimeTypes;
    private String welcomeFile;

    public StaticDirectoryHttpHandler(FileSystem fileSystem) {
        this.mimeTypes = new HashMap<String,String>(DEFAULT_MIME_TYPES);
        this.welcomeFile = DEFAULT_WELCOME_FILE;
        this.fileSystem = fileSystem;
    }

    public StaticDirectoryHttpHandler addMimeType(String extension, String mimeType) {
        mimeTypes.put(extension, mimeType);
        return this;
    }

    public StaticDirectoryHttpHandler welcomeFile(String welcomeFile) {
        this.welcomeFile = welcomeFile;
        return this;
    }

    public StaticDirectoryHttpHandler(Executor userThreadExecutor, Executor ioThreadExecutor, File dir) throws IOException {
        this(new AsyncFileSystem(userThreadExecutor, ioThreadExecutor, new JavaFileSystem(dir)));
    }

    public StaticDirectoryHttpHandler(Executor userThreadExecutor, File dir) throws IOException {
        this(new AsyncFileSystem(userThreadExecutor, newFixedThreadPool(4), new JavaFileSystem(dir)));
    }

    @Override
    public void handleHttpRequest(HttpRequest request, final HttpResponse response) throws Exception {
        serveFile(response, request.uri());
    }

    private void serveFile(final HttpResponse response, final String path) {
        // TODO: Handle binary files
        // TODO: Cache
        // TODO: Ignore query params
        fileSystem.readText(path, response.charset(), new Result<String>() {
            @Override
            public void complete(String contents) {
                response.header("Content-Type", guessMimeType(path, response))
                        .header("Content-Length", contents.length())
                        .content(contents)
                        .end();
            }

            @Override
            public void error(final Exception error) {
                if (error instanceof FileNotFoundException) {
                    response.status(404).end();
                } else {
                    fileSystem.stat(path, new Result<FileStat>() {
                        @Override
                        public void complete(FileStat result) {
                            if (result.isDirectory() && result.exists()) {
                                serveFile(response, path + "/" + welcomeFile);   
                            } else {
                                response.error(error);
                            }
                        }

                        @Override
                        public void error(Exception ignored) {
                            response.error(error);
                        }
                    });
                }
            }
        });
    }

    private String guessMimeType(String path, HttpResponse response) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1) {
            return null;
        }
        String extension = path.substring(lastDot + 1).toLowerCase();
        String mimeType = mimeTypes.get(extension);
        if (mimeType == null) {
            return null;
        }
        if (mimeType.startsWith("text/") && response.charset() != null) {
            mimeType += "; charset=" + response.charset().name();
        }
        return mimeType;
    }
}
