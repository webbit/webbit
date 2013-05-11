package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.helpers.ClassloaderResourceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class StaticFileHandler extends AbstractResourceHandler {

    private final File dir;

    private final long maxAge;

    public StaticFileHandler(File dir, Executor ioThread, TemplateEngine templateEngine) {
        super(ioThread, templateEngine);
        this.dir = dir;
        this.maxAge = 0;
    }

    public StaticFileHandler(File dir, Executor ioThread) {
        this(dir, ioThread, new StaticFile());
    }

    public StaticFileHandler(String dir, Executor ioThread, TemplateEngine templateEngine) {
        this(new File(dir), ioThread, templateEngine);
    }

    public StaticFileHandler(String dir, Executor ioThread) {
        this(dir, ioThread, new StaticFile());
    }

    public StaticFileHandler(File dir, TemplateEngine templateEngine) {
        this(dir, newFixedThreadPool(4), templateEngine);
    }

    public StaticFileHandler(File dir) {
        this(dir, new StaticFile());
    }

    public StaticFileHandler(String dir, TemplateEngine templateEngine) {
        this(new File(dir), templateEngine);
    }

    public StaticFileHandler(String dir) {
        this(new File(dir));
    }

    //cache control-aware

    public StaticFileHandler(File dir, Executor ioThread, TemplateEngine templateEngine, long maxAge) {
        super(ioThread, templateEngine);
        this.dir = dir;
        this.maxAge = maxAge;
    }

    public StaticFileHandler(File dir, Executor ioThread, long maxAge) {
        this(dir, ioThread, new StaticFile(), maxAge);
    }

    public StaticFileHandler(String dir, long maxAge) {
        this(new File(dir), maxAge);
    }

    public StaticFileHandler(File dir, long maxAge) {
        this(dir, newFixedThreadPool(4), new StaticFile(), maxAge);
    }

    @Override
    protected FileWorker createIOWorker(HttpRequest request,
                                        HttpResponse response,
                                        HttpControl control) {
        return new FileWorker(request, response, control, maxAge);
    }

    protected class FileWorker extends IOWorker {

        private File file;

        private final HttpResponse response;

        private final HttpRequest request;

        private final long maxAge;

        private String mimeType(String uri) {
            String ext = uri.lastIndexOf(".") != -1 ? uri.substring(uri.lastIndexOf(".")) : null;
            String currentMimeType = mimeTypes.get(ext);
            if (currentMimeType == null) currentMimeType = "text/plain";
            return currentMimeType;
        }
        //based on: http://m2tec.be/blog/2010/02/03/java-md5-hex-0093
        private  String MD5(String md5) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                byte[] array = md.digest(md5.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < array.length; ++i) {
                    sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }

        private String toHeader(Date date) {
            SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return httpDateFormat.format(date);
        }

        private Date fromHeader(String date) {
            SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                return httpDateFormat.parse(date);
            } catch (Exception ex) {
                return new Date();
            }
        }

        protected FileWorker(HttpRequest request, HttpResponse response, HttpControl control, long maxAge) {
            super(request.uri(), request, response, control);
            this.maxAge = maxAge;
            this.response = response;
            this.request = request;
        }

        @Override
        protected boolean exists() throws IOException {
            file = resolveFile(path);
            return file != null && file.exists();
        }

        @Override
        protected boolean isDirectory() throws IOException {
            return file.isDirectory();
        }

        @Override
        protected byte[] fileBytes() throws IOException {
            byte[] raw = file.isFile() ? read(file) : null;
            //add cache control headers if needed
            if (raw != null) {
                Date lastModified = new Date(file.lastModified());
                String hashtext = MD5(Long.toString(lastModified.getTime()));
                if (hashtext != null) response.header("ETag", "\"" + hashtext + "\"");

                response.header("Last-Modified", toHeader(lastModified));
                //is there an incoming If-Modified-Since?
                if (request.header("If-Modified-Since") != null) {
                    if (fromHeader(request.header("If-Modified-Since")).getTime() >= lastModified.getTime() ) {
                        response.status(304);
                    }
                }
                //is setting cache control necessary?
                if (maxAge != 0) {
                    response.header("Expires", toHeader( new Date(new Date().getTime() + maxAge * 1000)));
                    response.header("Cache-Control", "max-age=" + maxAge+", public");
                }
            }
            return raw;
        }

        @Override
        protected byte[] welcomeBytes() throws IOException {
            File welcome = new File(file, welcomeFileName);
            return welcome.isFile() ? read(welcome) : null;
        }

        @Override
        protected byte[] directoryListingBytes() throws IOException {
            if (!isDirectory()) {
                return null;
            }
            Iterable<FileEntry> files = ClassloaderResourceHelper.fileEntriesFor(file.listFiles());
            return directoryListingFormatter.formatFileListAsHtml(files);
        }

        private byte[] read(File file) throws IOException {
            return read((int) file.length(), new FileInputStream(file));
        }

        protected File resolveFile(String path) throws IOException {
            // Find file, relative to root
            File result = new File(dir, path).getCanonicalFile();

            // For security, check file really does exist under root.
            String fullPath = result.getPath();
            if (!fullPath.startsWith(dir.getCanonicalPath() + File.separator) && !fullPath.equals(dir.getCanonicalPath())) {
                // Prevent paths like http://foo/../../etc/passwd
                return null;
            }
            return result;
        }
    }
}
