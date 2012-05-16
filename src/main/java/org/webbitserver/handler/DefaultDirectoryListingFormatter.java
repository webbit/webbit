package org.webbitserver.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

class DefaultDirectoryListingFormatter implements DirectoryListingFormatter {
    private static final String DIRECTORY_LISTING_FORMAT_STRING =
        "<html><body><ol style='list-style-type: none; padding-left: 0px; margin-left: 0px;'>%s</ol></body></html>";

    public ByteBuffer formatFileListAsHtml(File[] files) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (File file : files) {
          String fileName = FileNameEscaper.escape(file.getName());
          builder
              .append("<li><a href=\"")
              .append(fileName)
              .append("\">")
              .append(fileName)
              .append("</a></li>");
        }
        String formattedString = String.format(DIRECTORY_LISTING_FORMAT_STRING, builder.toString());
        byte[] formattedBytes = formattedString.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(formattedBytes);
        return AbstractResourceHandler.consumeInputStreamToByteBuffer(formattedBytes.length, inputStream);
    }

    private static class FileNameEscaper {
        public static String escape(String input) {
            StringBuilder builder = new StringBuilder(input.length());
            for (int i = 0; i < input.length(); ++i) {
                char original = input.charAt(i);
                switch (original) {
                    case '&':
                        builder.append("&amp;");
                        break;
                    case '<':
                        builder.append("&lt;");
                        break;
                    case '>':
                        builder.append("&gt;");
                        break;
                    case '"':
                        builder.append("&quot;");
                        break;
                    case '\'':
                        builder.append("&#x27;");
                        break;
                    case '/':
                        builder.append("&#x2F;");
                        break;
                    default:
                        builder.append(original);
                        break;
                }
            }
            return builder.toString();
        }
    }
}
