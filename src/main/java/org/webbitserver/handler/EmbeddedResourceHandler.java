package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        return new ResourceWorker(request.uri(), response, control);
    }

    protected class ResourceWorker extends IOWorker {
        private URL resource;
        private InputStream content;
        private File file;

        protected ResourceWorker(String path, HttpResponse response, HttpControl control) {
            super(path, response, control);
        }

        @Override
        protected boolean exists() throws IOException {
            file = new File(root, path);
            resource = getResource(file);
            return resource != null;
        }

        @Override
        protected byte[] fileBytes() throws IOException {
            content = resource.openStream();
            if (content == null || (content instanceof ByteArrayInputStream)) {
                // It seems that directory listings are reported as BAOS, while files are not. Seems fragile, but works...
                return null;
            } else {
                return read(content);
            }
        }

        @Override
        protected byte[] welcomeBytes() throws IOException {
            URL resourceURL = getResource(new File(file, welcomeFileName));
            return resourceURL == null ? null : read(resourceURL.openStream());
        }

        private byte[] read(InputStream content) throws IOException {
            try {
                return read(content.available(), content);
            } catch(NullPointerException happensWhenReadingDirectoryPAthInJar) {
                return null;
            }
        }

        private URL getResource(File file) throws IOException {
            String path = file.getPath();
            return getClass().getClassLoader().getResource(path);
        }
    }
}
