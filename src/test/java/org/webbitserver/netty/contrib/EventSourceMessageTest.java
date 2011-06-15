package org.webbitserver.netty.contrib;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventSourceMessageTest {
    @Test
    public void encodesSingleLine() throws Exception {
        assertEquals("data: Hello\n", new EventSourceMessage().data("Hello").build());
    }

    @Test
    public void encodesSeveralLines() throws Exception {
        assertEquals("data: Hello\ndata: World\n", new EventSourceMessage().data("Hello\nWorld").build());
    }

    @Test
    public void encodesId() throws Exception {
        assertEquals("data: Hello\nid: 33\n", new EventSourceMessage().data("Hello").id("33").build());
    }

    @Test
    public void skipsColonIfValueEmpty() throws Exception {
        assertEquals("id\n", new EventSourceMessage().id("").build());
    }
}
