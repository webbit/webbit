package org.webbitserver.netty.contrib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultEventSourceFrameTest {
    @Test
    public void encodesSingleLine() throws Exception {
        assertEquals("data:Hello\n\n", new DefaultEventSourceFrame("Hello").toString());
    }
}
