package webbit.async.filesystem;

import webbit.async.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class JavaFileSystem implements FileSystem {

    private final File root;

    public JavaFileSystem(File root) throws IOException {
        this.root = root.getCanonicalFile();
    }

    private File resolve(String path) throws IOException {
        // TODO: Validate it lies under root.
        return new File(root, path).getCanonicalFile();
    }

    @Override
    public void stat(String path, Result<FileStat> result) {
        try {
            File file = resolve(path);
            result.complete(new FileStat(file.exists(), file.isDirectory(), file.lastModified(), file.length()));
        } catch (IOException e) {
            result.error(e);
        }
    }

    @Override
    public void readString(String path, Charset charset, Result<String> result) {
        try {
            File file = resolve(path);
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
            if (file.isDirectory()) {
                throw new IOException("Cannot readString() on directory");
            }
            FileInputStream inputStream = new FileInputStream(file);
            try {
                FileChannel channel = inputStream.getChannel();
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                result.complete(charset.decode(buffer).toString());
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            result.error(e);
        }
    }
}
