package webbit.async.filesystem;

import webbit.async.Result;

import java.nio.charset.Charset;

public interface FileSystem {

    void stat(String path, Result<FileStat> result);

    void readText(String path, Charset charset, Result<String> result);
    void writeText(String path, Charset charset, String text, Result<Void> result);
    void appendText(String path, Charset charset, String text, Result<Void> result);

    void move(String oldPath, String newPath, Result<Boolean> result);
    void delete(String path, boolean recursive, Result<Boolean> result);

    void mkdir(String path, boolean makeParents, Result<Boolean> result);

    // TODO: List files in dir
    // TODO: Binary read/write
    // TODO: Stream read/write
    // TODO: Watch/unwatch
}
