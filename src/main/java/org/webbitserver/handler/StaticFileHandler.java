package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.helpers.ClassloaderResourceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class StaticFileHandler extends AbstractResourceHandler {

    private final File dir;

    public StaticFileHandler(File dir, Executor ioThread, TemplateEngine templateEngine) {
        super(ioThread, templateEngine);
        this.dir = dir;
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

    @Override
    protected FileWorker createIOWorker(HttpRequest request,
                                                        HttpResponse response,
                                                        HttpControl control) {
        return new FileWorker(request, response, control);
    }

    protected class FileWorker extends IOWorker {

        private File file;

        protected FileWorker(HttpRequest request, HttpResponse response, HttpControl control) {
            super(request.uri(), request, response, control);
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
            return file.isFile() ? read(file) : null;
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
