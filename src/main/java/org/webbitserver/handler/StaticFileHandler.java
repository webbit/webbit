package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class StaticFileHandler extends AbstractResourceHandler {
    private static java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    private final File dir;

    public StaticFileHandler(File dir, Executor ioThread) {
        super(ioThread);
        this.dir = dir;
    }

    public StaticFileHandler(String dir, Executor ioThread) {
        this(new File(dir), ioThread);
    }

    public StaticFileHandler(File dir) {
        this(dir, newFixedThreadPool(4));
    }

    public StaticFileHandler(String dir) {
        this(new File(dir));
    }

    @Override
    protected StaticFileHandler.IOWorker createIOWorker(HttpRequest request, HttpResponse response, HttpControl control) {
        return new StaticFileHandler.FileWorker(request, response, control);
    }

    protected class FileWorker extends IOWorker {
        private File file;
        private HttpRequest request;

        private FileWorker(HttpRequest request, HttpResponse response, HttpControl control) {
            super(request.uri(), response, control);
            this.request = request;
        }

        @Override
        protected boolean exists() throws IOException {
            file = resolveFile(path);
            return file != null && file.exists();
        }

        @Override
        protected byte[] fileBytes() throws IOException {
            return file.isFile() ? read(file) : null;
        }

        @Override
        protected byte[] welcomeBytes() throws IOException {
            File welcome = new File(file, welcomeFileName);
            return welcome.isFile() ? read(welcome) : null;
        }
        
        @Override
        protected boolean isModified() throws IOException {
            if (file == null || !file.exists())
                return true;

            // Get the "If-Modified-Since" header from the request, if any
            String modified = request.header("If-Modified-Since");
            if (modified == null)
                return true;
            
            // Parse modified string
            try {
                java.util.Date date = format.parse(modified);
                
                // Time form header is accurate to seconds only, but lastModified to milliseconds
                return (date.getTime() / 1000) < (file.lastModified() / 1000);
                
            } catch (java.text.ParseException e) {
                return false;
            }
        }

        @Override
        protected void serve(final String mimeType, final byte[] contents) {
            // Add "Last-Modified" header to the response
            response.header("Last-Modified", format.format(new java.util.Date(file.lastModified())));
            
            super.serve(mimeType, contents);
        }
        
        private byte[] read(File file) throws IOException {
            return read((int) file.length(), new FileInputStream(file));
        }

        private File resolveFile(String path) throws IOException {
            // Find file, relative to roo
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
