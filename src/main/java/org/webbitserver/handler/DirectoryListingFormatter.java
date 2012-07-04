package org.webbitserver.handler;

import java.io.IOException;

public interface DirectoryListingFormatter {
    /**
     * Formats a list of files for display as a full well-formed HTML page.
     * Must escape any unsafe file names or other data being returned.
     *
     * @param files A list of files about which information (e.g. file names) is to be presented.
     * @return A ByteBuffer containing an entire HTML page to display, presenting information about files.
     * @throws IOException
     */
    byte[] formatFileListAsHtml(Iterable<FileEntry> files) throws IOException;
}
