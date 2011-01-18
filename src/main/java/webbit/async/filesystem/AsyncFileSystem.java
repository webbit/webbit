package webbit.async.filesystem;

import webbit.async.Result;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;

@SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
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
    public void readText(final String path, final Charset charset, final Result<String> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.readText(path, charset, onUserThread(originalStack, result));
            }
        });
    }

    @Override
    public void writeText(final String path, final Charset charset, final String text, final Result<Void> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.writeText(path, charset, text, onUserThread(originalStack, result));
            }
        });
    }

    @Override
    public void appendText(final String path, final Charset charset, final String text, final Result<Void> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.appendText(path, charset, text, onUserThread(originalStack, result));
            }
        });
    }

    @Override
    public void move(final String oldPath, final String newPath, final Result<Boolean> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.move(oldPath, newPath, onUserThread(originalStack, result));
            }
        });
    }

    @Override
    public void mkdir(final String path, final boolean makeParents, final Result<Boolean> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.mkdir(path, makeParents, onUserThread(originalStack, result));
            }
        });
    }

    @Override
    public void delete(final String path, final boolean recursive, final Result<Boolean> result) {
        final Throwable originalStack = new Throwable();
        onIoThread(new Runnable() {
            @Override
            public void run() {
                fileSystem.delete(path, recursive, onUserThread(originalStack, result));
            }
        });
    }
}
