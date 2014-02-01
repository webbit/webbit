package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractResourceHandler implements HttpHandler {
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
        mimeTypes.put("swf", "application/x-shockwave-flash");
        mimeTypes.put("svg", "image/svg+xml");
        mimeTypes.put("ico", "image/vnd.microsoft.icon");
        DEFAULT_MIME_TYPES = Collections.unmodifiableMap(mimeTypes);
    }

    private static final Pattern SINGLE_BYTE_RANGE = Pattern.compile("bytes=(\\d+)?-(\\d+)?");
    public static final Map<String, String> DEFAULT_MIME_TYPES;
    protected static final String DEFAULT_WELCOME_FILE_NAME = "index.html";
    protected final Executor ioThread;
    protected final Map<String, String> mimeTypes;
    protected String welcomeFileName;
    protected DirectoryListingFormatter directoryListingFormatter;
    protected final TemplateEngine templateEngine;

    private boolean isDirectoryListingEnabled = false;

    public AbstractResourceHandler(Executor ioThread, TemplateEngine templateEngine) {
        this.ioThread = ioThread;
        this.templateEngine = templateEngine;
        this.mimeTypes = new HashMap<String, String>(DEFAULT_MIME_TYPES);
        this.welcomeFileName = DEFAULT_WELCOME_FILE_NAME;
    }

    public AbstractResourceHandler(Executor ioThread) {
        this(ioThread, new StaticFile());
    }

    public AbstractResourceHandler addMimeType(String extension, String mimeType) {
        mimeTypes.put(extension, mimeType);
        return this;
    }

    public AbstractResourceHandler welcomeFile(String welcomeFile) {
        this.welcomeFileName = welcomeFile;
        return this;
    }

    public AbstractResourceHandler enableDirectoryListing(boolean isDirectoryListingEnabled) {
        return enableDirectoryListing(isDirectoryListingEnabled, new DefaultDirectoryListingFormatter());
    }

    public AbstractResourceHandler enableDirectoryListing(boolean isDirectoryListingEnabled, DirectoryListingFormatter directoryListingFormatter) {
        this.isDirectoryListingEnabled = isDirectoryListingEnabled;
        this.directoryListingFormatter = directoryListingFormatter;
        return this;
    }

    @Override
    public void handleHttpRequest(final HttpRequest request, final HttpResponse response, final HttpControl control) throws Exception {
        // Switch from web thead to IO thread, so we don't block web server when we access the filesystem.
        ioThread.execute(createIOWorker(request, response, control));
    }

    protected void serve(final String mimeType,
                         final byte[] staticContents,
                         HttpControl control,
                         final HttpResponse response,
                         final HttpRequest request,
                         final String path) {
        // Switch back from IO thread to web thread.
        control.execute(new Runnable() {
            @Override
            public void run() {
                // TODO: Check bytes read match expected encoding of mime-type
                response.header("Content-Type", mimeType);

                byte[] dynamicContents = templateEngine.process(staticContents, path, request.data(TemplateEngine.TEMPLATE_CONTEXT));
                ByteBuffer contents = ByteBuffer.wrap(dynamicContents);

                if (maybeServeRange(request, contents, response)) {
                    return;
                }

                // TODO: Don't read all into memory, instead use zero-copy.
                response.header("Content-Length", contents.remaining())
                        .content(contents)
                        .end();
            }
        });
    }

    private boolean maybeServeRange(HttpRequest request, ByteBuffer contents, HttpResponse response) {
        String range = request.header("Range");
        if (null == range) {
            return false;
        }

        Matcher matcher = SINGLE_BYTE_RANGE.matcher(range);
        if (!matcher.matches()) {
            return false;
        }

        String startString = matcher.group(1);
        String endString = matcher.group(2);

        if (null != startString && null != endString) {
            int start = Integer.parseInt(startString);
            int end = Integer.parseInt(endString);
            if (start <= end) {
                serveRange(start,
                        Math.min(contents.remaining() - 1, end),
                        contents,
                        response);
                return true;
            }
        } else if (null != startString) {
            serveRange(Integer.parseInt(startString),
                    contents.remaining() - 1,
                    contents,
                    response);
            return true;
        } else if (null != endString) {
            int end = Integer.parseInt(endString);
            serveRange(contents.remaining() - end,
                    contents.remaining() - 1,
                    contents,
                    response);
            return true;
        }

        return false;
    }

    protected void serveRange(int start, int end, ByteBuffer contents, HttpResponse response) {
        if (start > contents.remaining()) {
            response.status(416).header("Content-Range", "bytes */" + contents.remaining()).end();
            return;
        }


        response.status(206)
                .header("Content-Length", end - start + 1) // since its inclusive
                .header("Content-Range",
                        "bytes " + start + "-" + end + "/" + contents.remaining());

        contents.limit(contents.position() + end + 1)
                .position(contents.position() + start);
        response.content(contents).end();
    }

    protected abstract IOWorker createIOWorker(HttpRequest request,
                                                                 HttpResponse response,
                                                                 HttpControl control);

    /**
     * All IO is performed by this worker on a separate thread, so we never block the HttpHandler.
     */
    protected abstract class IOWorker implements Runnable {

        protected String path;
        private final HttpRequest request;
        protected final HttpResponse response;
        protected final HttpControl control;

        protected IOWorker(String path, HttpRequest request, HttpResponse response, HttpControl control) {
            this.path = path;
            this.request = request;
            this.response = response;
            this.control = control;
        }

        protected void notFound() {
            // Switch back from IO thread to web thread.
            control.execute(new Runnable() {
                @Override
                public void run() {
                    control.nextHandler();
                }
            });
        }

        protected void error(final IOException exception) {
            // Switch back from IO thread to web thread.
            control.execute(new Runnable() {
                @Override
                public void run() {
                    response.error(exception);
                }
            });
        }

        @Override
        public void run() {
            String pathWithQuery = path;
            path = withoutQuery(path);

            // TODO: Cache
            try {
                byte[] content = null;
                if (!exists()) {
                    notFound();
                    return;
                }
                if (isDirectory()) {
                    // Assumes if path has been changed since the original request,
                    // its current value with a trailing slash will still resolve properly
                    if (!path.endsWith("/")) {
                        response.status(301).header("Location", path + "/" + extractQuery(pathWithQuery)).end();
                        return;
                    } else if ((content = welcomeBytes()) != null) {
                        serve(guessMimeType(welcomeFileName), content, control, response, request, path);
                        return;
                    } else if (isDirectoryListingEnabled && (content = directoryListingBytes()) != null) {
                        serve(guessMimeType(".html"), content, control, response, request, path);
                        return;
                    }
                    // TODO: Do something other than 404 if directory listing is disabled
                } else if ((content = fileBytes()) != null) {
                    serve(guessMimeType(path), response.status() == 304 ? new byte[0] : content, control, response, request, path);
                    return;
                } else if ((content = welcomeBytes()) != null) {
                    serve(guessMimeType(welcomeFileName), content, control, response, request, path);
                    return;
                }
                notFound();
            } catch (IOException e) {
                error(e);
            }
        }

        protected abstract boolean exists() throws IOException;

        protected abstract boolean isDirectory() throws IOException;

        protected abstract byte[] fileBytes() throws IOException;

        protected abstract byte[] welcomeBytes() throws IOException;

        protected abstract byte[] directoryListingBytes() throws IOException;

        protected byte[] read(int length, InputStream in) throws IOException {
            byte[] data = new byte[length];
            try {
                int read = 0;
                while (read < length) {
                    int more = in.read(data, read, data.length - read);
                    if (more == -1) {
                        break;
                    } else {
                        read += more;
                    }
                }
            } finally {
                in.close();
            }
            return data;
        }

        // TODO: Don't respond with a mime type that violates the request's Accept header
        private String guessMimeType(String path) {
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

        protected String withoutQuery(String path) {
            int queryStart = path.indexOf('?');
            if (queryStart > -1) {
                path = path.substring(0, queryStart);
            }
            return path;
        }

        protected String extractQuery(String path) {
            int queryStart = path.indexOf('?');
            if (queryStart > -1) {
                return path.substring(queryStart);
            }
            return "";
        }

    }
}
