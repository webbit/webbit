package org.webbitserver.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public interface TemplateEngine {
    String TEMPLATE_CONTEXT = "TEMPLATE_CONTEXT";

    ByteBuffer process(int length, InputStream in, String path, Object templateContext) throws IOException;
}
