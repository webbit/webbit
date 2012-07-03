package org.webbitserver.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class NullEngine implements TemplateEngine {
    @Override
    public ByteBuffer process(int length, InputStream template, String templatePath, Object templateContext) throws IOException {
        return ByteBuffer.wrap(readBytes(length, template));
    }

    public static byte[] readBytes(int length, InputStream in) throws IOException {
        byte[] data = new byte[length];
        try {
            int read = 0;
            while (read < length) {
                int more = in.read(data, read, data.length - read);
                if (more == -1) {
                    break;
                } else {
                    read += more;
                }
            }
        } finally {
            in.close();
        }
        return data;
    }
}
