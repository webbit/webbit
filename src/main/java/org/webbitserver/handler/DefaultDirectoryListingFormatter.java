package org.webbitserver.handler;

import org.webbitserver.helpers.XssCharacterEscaper;

import java.io.IOException;

public class DefaultDirectoryListingFormatter implements DirectoryListingFormatter {
    private static final String DIRECTORY_LISTING_FORMAT_STRING =
            "<html><body><ol style='list-style-type: none; padding-left: 0px; margin-left: 0px;'>%s</ol></body></html>";

    public byte[] formatFileListAsHtml(Iterable<FileEntry> files) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (FileEntry file : files) {
            String fileName = XssCharacterEscaper.escape(file.name);
            builder
                    .append("<li><a href=\"")
                    .append(fileName)
                    .append("\">")
                    .append(fileName)
                    .append("</a></li>");
        }
        String formattedString = String.format(getDirectoryListingFormatString(), builder.toString());
        return formattedString.getBytes("UTF-8");
    }

    protected String getDirectoryListingFormatString() {
        return DIRECTORY_LISTING_FORMAT_STRING;
    }
}
