package org.webbitserver.netty.contrib;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

public class DefaultEventSourceFrame {
    private final String message;

    public DefaultEventSourceFrame(String message) {
        this.message = message;
    }

    public ChannelBuffer toChannelBuffer() {
        return copiedBuffer(toString(), CharsetUtil.UTF_8);
    }

    @Override
    public String toString() {
        return "data:" + message + "\n\n";
    }
}
