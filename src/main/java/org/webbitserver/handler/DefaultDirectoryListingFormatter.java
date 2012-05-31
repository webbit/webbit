package org.webbitserver.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.webbitserver.helpers.XssCharacterEscaper;

public class DefaultDirectoryListingFormatter implements DirectoryListingFormatter {
    private static final String DIRECTORY_LISTING_FORMAT_STRING =
        "<html><body><ol style='list-style-type: none; padding-left: 0px; margin-left: 0px;'>%s</ol></body></html>";

    public ByteBuffer formatFileListAsHtml(File[] files) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (File file : files) {
          String fileName = XssCharacterEscaper.escape(file.getName());
          builder
              .append("<li><a href=\"")
              .append(fileName)
              .append("\">")
              .append(fileName)
              .append("</a></li>");
        }
        String formattedString = String.format(getDirectoryListingFormatString(), builder.toString());
        byte[] formattedBytes = formattedString.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(formattedBytes);
        return AbstractResourceHandler.consumeInputStreamToByteBuffer(formattedBytes.length, inputStream);
    }

    protected String getDirectoryListingFormatString() {
      return DIRECTORY_LISTING_FORMAT_STRING;
    }
}
