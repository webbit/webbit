package webbit.async.filesystem;

import webbit.async.Result;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;

public class AsyncFileSystem implements FileSystem {

    private final Executor userThreadExecutor;
    private final Executor ioThreadExecutor;
    private final FileSystem fileSystem;

    public AsyncFileSystem(Executor userThreadExecutor, Executor ioThreadExecutor, FileSystem fileSystem) {
        this.userThreadExecutor = userThreadExecutor;
        this.ioThreadExecutor = ioThreadExecutor;
        this.fileSystem = fileSystem;
    }

    private void onIoThread(Runnable command) {
        ioThreadExecutor.execute(command);
    }

    private void onUserThread(Runnable command) {
        userThreadExecutor.execute(command);
    }

    private <T> Result<T> onUserThread(final Throwable originalStack, final Result<T> callback) {
        return new Result<T>() {
            @Override
            public void complete(final T item) {
                onUserThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.complete(item);
                    }
                });
            }

            @Override
            public void error(final Exception error) {
                Throwable cause = lastCause(error);
                cause.initCause(originalStack);
                onUserThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.error(error);
                    }
                });
            }
        };
    }

    private Throwable lastCause(Throwable throwable) {
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    @Override
    public void stat(final String path, final Result<FileStat> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.stat(path, onUserThread(originalStack, result));
            }
        });
    }

    @Override
    public void readString(final String path, final Charset charset, final Result<String> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.readString(path, charset, onUserThread(originalStack, result));
            }
        });
    }
    
}
