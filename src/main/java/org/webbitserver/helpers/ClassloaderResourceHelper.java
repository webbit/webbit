package org.webbitserver.helpers;

import org.webbitserver.handler.FileEntry;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassloaderResourceHelper {

    /**
     * Lists all files and directories relative to the classpath entry (jar or folder)
     * containing clazz, directly under subdirectory.
     * <p/>
     * If clazz org.webbitserver.Foo is in folder /some/path/org/webbitserver/Foo.clazz,
     * will list files in /some/path/$subdirectory
     * <br />
     * If clazz org.webbitserver.Foo is in jar /some/path/webbit.jar,
     * will list files in /some/path/webbit.jar:$subdirectory
     *
     * @param clazz        Any class within the classpath entry to be listed.
     * @param subdirectory Subdirectory to list.  Must be an absolute path, relative to the top level of the classpath entry.
     * @return List of files directly in subdirectory, under the classpath entry containing clazz.
     * @throws IOException
     */
    public static Iterable<FileEntry> listFilesRelativeToClass(Class<?> clazz, String subdirectory) throws IOException {
        ArrayList<FileEntry> list = new ArrayList<FileEntry>();
        CodeSource src = clazz.getProtectionDomain().getCodeSource();
        if (src == null) {
            return list;
        }
        URL classpathEntry = src.getLocation();
        try {
            // Check if we're loaded from a folder
            File file = new File(new File(classpathEntry.toURI()), subdirectory);
            if (file.isDirectory()) {
                return fileEntriesFor(file.listFiles());
            }
        } catch (URISyntaxException e) {
            // Should never happen, because we know classpathentry is valid
            throw new RuntimeException(e);
        }

        // We're not in a folder, so we must be in a jar or similar
        subdirectory = subdirectory.replace(File.separatorChar, '/');
        if (!subdirectory.endsWith("/")) {
            subdirectory = subdirectory + "/";
        }

        ZipInputStream jarStream = new ZipInputStream(classpathEntry.openStream());
        ZipEntry zipEntry;
        while ((zipEntry = jarStream.getNextEntry()) != null) {
            if (isChild(subdirectory, zipEntry.getName())) {
                String basename = zipEntry.getName().substring(subdirectory.length());
                int indexOfSlash = basename.indexOf('/');
                if (indexOfSlash < 0 || indexOfSlash == basename.length() - 1) {
                    list.add(new FileEntry(basename));
                }
            }
        }
        return list;
    }

    private static boolean isChild(String parent, String name) {
        return name.startsWith(parent);
    }

    public static Iterable<FileEntry> fileEntriesFor(File[] files) {
        List<FileEntry> fileEntries = new ArrayList<FileEntry>(files.length);
        for (File file : files) {
            String filename = file.getName();
            if (file.isDirectory()) {
                filename += "/";
            }
            fileEntries.add(new FileEntry(filename));
        }
        return fileEntries;
    }
}
