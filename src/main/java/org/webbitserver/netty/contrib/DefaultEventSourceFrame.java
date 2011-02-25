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
        return copiedBuffer("data:" + message + "\n\n", CharsetUtil.UTF_8);
    }
}
