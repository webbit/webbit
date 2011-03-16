package org.webbitserver.netty.contrib;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;

import java.util.regex.Pattern;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

public class DefaultEventSourceFrame {
    private static final Pattern START = Pattern.compile("^", Pattern.MULTILINE);
    private static final String DATA = "data: ";

    private final String message;

    public DefaultEventSourceFrame(String message) {
        this.message = message;
    }

    public ChannelBuffer toChannelBuffer() {
        return copiedBuffer(toString(), CharsetUtil.UTF_8);
    }

    @Override
    public String toString() {
        return START.matcher(message).replaceAll(DATA) + "\n\n";
    }
}
