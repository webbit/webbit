package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

// Maybe http://www.uofr.net/~greg/java/get-resource-listing.html
public class EmbeddedResourceHandler extends AbstractResourceHandler {
    // We're using File because it's good at dealing with path concatenation and slashes. Not actually opening the file.
    private final File root;

    public EmbeddedResourceHandler(String root, Executor ioThread) {
        super(ioThread);
        this.root = new File(root);
    }

    public EmbeddedResourceHandler(String root) {
        this(root, newFixedThreadPool(4));
    }

    @Override
    protected IOWorker createIOWorker(HttpRequest request, HttpResponse response, HttpControl control) {
        return new ResourceWorker(request, response, control);
    }

    protected class ResourceWorker extends IOWorker {
        private InputStream resource;
        private InputStream content;
        private File file;

        protected ResourceWorker(HttpRequest request, HttpResponse response, HttpControl control) {
            super(request.uri(), request, response, control);
        }

        @Override
        protected boolean exists() throws IOException {
            file = new File(root, path);
            resource = getResource(file);
            return resource != null;
        }

        @Override
        protected ByteBuffer fileBytes() throws IOException {
            content = resource;
            if (content == null || (content instanceof ByteArrayInputStream)) {
                // It seems that directory listings are reported as BAOS, while files are not. Seems fragile, but works...
                return null;
            } else {
                return read(content);
            }
        }

        @Override
        protected ByteBuffer welcomeBytes() throws IOException {
            InputStream resourceStream = getResource(new File(file, welcomeFileName));
            return resourceStream == null ? null : read(resourceStream);
        }

        private ByteBuffer read(InputStream content) throws IOException {
            try {
                return read(content.available(), content);
            } catch (NullPointerException happensWhenReadingDirectoryPathInJar) {
                return null;
            }
        }

        private InputStream getResource(File file) throws IOException {
            String resourcePath = file.getPath();
            if ('/' != File.separatorChar) {
                resourcePath = resourcePath.replace(File.separatorChar, '/');
            }
            return getClass().getClassLoader().getResourceAsStream(resourcePath);
        }
    }
}
