package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.helpers.ClassloaderResourceHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

// Maybe http://www.uofr.net/~greg/java/get-resource-listing.html
public class EmbeddedResourceHandler extends AbstractResourceHandler {
    // We're using File because it's good at dealing with path concatenation and slashes. Not actually opening the file.
    private final File root;
    private Class<?> clazz;

    public EmbeddedResourceHandler(String root, Executor ioThread, Class<?> clazz, TemplateEngine templateEngine) {
        super(ioThread, templateEngine);
        this.root = new File(root);
        this.clazz = clazz;
    }

    public EmbeddedResourceHandler(String root, Executor ioThread, Class<?> clazz) {
        this(root, ioThread, clazz, new StaticFile());
    }

    public EmbeddedResourceHandler(String root, Executor ioThread, TemplateEngine templateEngine) {
        this(root, ioThread, EmbeddedResourceHandler.class, templateEngine);
    }

    public EmbeddedResourceHandler(String root, Executor ioThread) {
        this(root, ioThread, EmbeddedResourceHandler.class);
    }

    public EmbeddedResourceHandler(String root, Class<?> clazz, TemplateEngine templateEngine) {
        this(root, newFixedThreadPool(4), clazz, templateEngine);
    }

    public EmbeddedResourceHandler(String root, Class<?> clazz) {
        this(root, newFixedThreadPool(4), clazz);
    }

    public EmbeddedResourceHandler(String root, TemplateEngine templateEngine) {
        this(root, EmbeddedResourceHandler.class, templateEngine);
    }

    public EmbeddedResourceHandler(String root) {
        this(root, EmbeddedResourceHandler.class);
    }

    @Override
    protected ResourceWorker createIOWorker(HttpRequest request, HttpResponse response, HttpControl control) {
        return new ResourceWorker(request, response, control);
    }

    protected class ResourceWorker extends IOWorker {
        private InputStream resource;
        private File file;
        private final String pathWithoutTrailingSlash;
        private final boolean isDirectory;

        protected ResourceWorker(HttpRequest request, HttpResponse response, HttpControl control) {
            super(request.uri(), request, response, control);
            isDirectory = path.endsWith("/");
            pathWithoutTrailingSlash = withoutQuery(isDirectory ? path.substring(0, path.length() - 1) : path);
        }

        @Override
        protected boolean exists() throws IOException {
            file = new File(root, pathWithoutTrailingSlash);
            resource = getResource(file);
            return resource != null;
        }

        @Override
        protected boolean isDirectory() throws IOException {
            return isDirectory;
        }

        @Override
        protected byte[] fileBytes() throws IOException {
            if (resource == null || isDirectory()) {
                return null;
            } else {
                return read(resource);
            }
        }

        @Override
        protected byte[] welcomeBytes() throws IOException {
            File welcomeFile = new File(file, welcomeFileName);
            InputStream resourceStream = getResource(welcomeFile);
            return resourceStream == null ? null : read(resourceStream);
        }

        @Override
        protected byte[] directoryListingBytes() throws IOException {
            String subdirectory = file.getPath();
            Iterable<FileEntry> files = ClassloaderResourceHelper.listFilesRelativeToClass(clazz, subdirectory);
            return isDirectory() ? directoryListingFormatter.formatFileListAsHtml(files) : null;
        }

        private byte[] read(InputStream content) throws IOException {
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
            return clazz.getClassLoader().getResourceAsStream(resourcePath);
        }
    }
}
