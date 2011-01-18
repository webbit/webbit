package webbit.async.filesystem;

import webbit.async.Result;

import java.nio.charset.Charset;

public interface FileSystem {

    void stat(String path, Result<FileStat> result);
    void readString(String path, Charset charset, Result<String> result);

}
