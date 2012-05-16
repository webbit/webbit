package org.webbitserver.handler;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface DirectoryListingFormatter {
  ByteBuffer formatFileListAsHtml(File[] files) throws IOException;
}
