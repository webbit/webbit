package webbit.handler;

import webbit.HttpHandler;
import webbit.HttpRequest;
import webbit.HttpResponse;
import webbit.async.Result;
import webbit.async.filesystem.AsyncFileSystem;
import webbit.async.filesystem.FileSystem;
import webbit.async.filesystem.JavaFileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class StaticDirectoryHttpHandler implements HttpHandler {

    private final FileSystem fileSystem;

    public StaticDirectoryHttpHandler(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public StaticDirectoryHttpHandler(Executor userThreadExecutor, Executor ioThreadExecutor, File dir) throws IOException {
        this(new AsyncFileSystem(userThreadExecutor, ioThreadExecutor, new JavaFileSystem(dir)));
    }

    public StaticDirectoryHttpHandler(Executor userThreadExecutor, File dir) throws IOException {
        this(new AsyncFileSystem(userThreadExecutor, newFixedThreadPool(4), new JavaFileSystem(dir)));
    }

    @Override
    public void handleHttpRequest(HttpRequest request, final HttpResponse response) throws Exception {
        // TODO: Read file from URL
        // TODO: Handle binary files
        // TODO: Server appropriate mime-type
        // TODO: Cache
        // TODO: Ignore certain files
        fileSystem.readString("index.html", response.charset(), new Result<String>() {
            @Override
            public void complete(String contents) {
                response.header("Content-Type", "text/html; charset=" + response.charset().name())
                        .header("Content-Length", contents.length())
                        .content(contents)
                        .end();
            }

            @Override
            public void error(Exception error) {
                if (error instanceof FileNotFoundException) {
                    response.status(404).end();
                } else {
                    response.error(error);
                }
            }
        });
    }
}
