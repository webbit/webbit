package webbit.async.filesystem;

import webbit.async.Result;

import java.io.*;
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
    public void readText(String path, Charset charset, Result<String> result) {
        try {
            File file = resolve(path);
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
            if (file.isDirectory()) {
                throw new IOException("Cannot readText() on directory: " + file.getAbsolutePath());
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

    @Override
    public void writeText(String path, Charset charset, String text, Result<Void> result) {
        write(path, charset, text, result, false);
    }

    @Override
    public void appendText(String path, Charset charset, String text, Result<Void> result) {
        write(path, charset, text, result, true);
    }

    private void write(String path, Charset charset, String text, Result<Void> result, boolean append) {
        try {
            File file = resolve(path);
            if (file.isDirectory()) {
                throw new IOException("Cannot write to directory: " + file.getAbsolutePath());
            }
            FileOutputStream outputStream = new FileOutputStream(file, append);
            try {
                outputStream.write(text.getBytes(charset));
                result.complete(null);
            } finally {
                outputStream.close();
            }
        } catch (IOException e) {
            result.error(e);
        }
    }

    @Override
    public void move(String oldPath, String newPath, Result<Boolean> result) {
        try {
            File oldFile = resolve(oldPath);
            File newFile = resolve(newPath);
            result.complete(oldFile.renameTo(newFile));
        } catch (IOException e) {
            result.error(e);
        }
    }

    @Override
    public void mkdir(String path, boolean makeParents, Result<Boolean> result) {
        try {
            File file = resolve(path);
            result.complete(makeParents ? file.mkdirs() : file.mkdir());
        } catch (IOException e) {
            result.error(e);
        }
    }

    @Override
    public void delete(String path, boolean recursive, Result<Boolean> result) {
        try {
            File file = resolve(path);
            result.complete(recursive ? recursiveDelete(file) : file.delete());
        } catch (IOException e) {
            result.error(e);
        }
    }

    public static boolean recursiveDelete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                recursiveDelete(child);
            }
        }
        return file.delete();
    }
}
