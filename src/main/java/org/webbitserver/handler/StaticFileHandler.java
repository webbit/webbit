package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class StaticFileHandler extends AbstractResourceHandler {

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
    protected StaticFileHandler.IOWorker createIOWorker(HttpRequest request,
                                                        HttpResponse response,
                                                        HttpControl control) {
        return new StaticFileHandler.FileWorker(request, response, control);
    }

    protected class FileWorker extends IOWorker {
        private static final String DIRECTORY_LISTING_FORMAT_STRING =
          "<html><body><ol style='list-style-type: none; padding-left: 0px; margin-left: 0px;'>%s</ol></body></html>";

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
        protected ByteBuffer fileBytes() throws IOException {
            return file.isFile() ? read(file) : null;
        }

        @Override
        protected ByteBuffer welcomeBytes() throws IOException {
            File welcome = new File(file, welcomeFileName);
            return welcome.isFile() ? read(welcome) : null;
        }

        @Override
        protected ByteBuffer directoryListingBytes() throws IOException {
            if (file.isDirectory()) {
              String directoryListing = String.format(
                  DIRECTORY_LISTING_FORMAT_STRING,
                  getFileList());
              byte[] bytes = directoryListing.getBytes();
              ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
              return read(bytes.length, inputStream);
            }
            return null;
        }

        private String getFileList() {
          StringBuilder builder = new StringBuilder();
          for (File file : this.file.listFiles()) {
            builder
                .append("<li><a href=\"")
                .append(file.getName())
                .append("\">")
                .append(file.getName())
                .append("</a></li>");
          }
          return builder.toString();
        }

        private ByteBuffer read(File file) throws IOException {
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
