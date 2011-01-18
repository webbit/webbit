package webbit.async.filesystem;

public class FileStat {

    private final boolean exists;
    private final boolean isDirectory;
    private final long lastModified;
    private final long length;

    public FileStat(boolean exists, boolean isDirectory, long lastModified, long length) {
        this.exists = exists;
        this.isDirectory = isDirectory;
        this.lastModified = lastModified;
        this.length = length;
    }

    public boolean exists() {
        return exists;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long lastModified() {
        return lastModified;
    }

    public long length() {
        return length;
    }

    @Override
    public String toString() {
        return "FileStat{" +
                "exists=" + exists +
                ", isDirectory=" + isDirectory +
                ", lastModified=" + lastModified +
                ", length=" + length +
                '}';
    }
}
