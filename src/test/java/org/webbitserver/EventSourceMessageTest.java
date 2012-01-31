package org.webbitserver;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventSourceMessageTest {
    @Test
    public void skipsColonIfValueEmpty() throws Exception {
        assertEquals("id\n\n", new EventSourceMessage().id("").build());
    }

    @Test
    public void encodesSingleLine() throws Exception {
        assertEquals("data: Hello\n\n", new EventSourceMessage().data("Hello").build());
    }

    @Test
    public void encodesSeveralLines() throws Exception {
        assertEquals("data: Hello\ndata: World\n\n", new EventSourceMessage().data("Hello\nWorld").build());
    }

    @Test
    public void encodesId() throws Exception {
        assertEquals("data: Hello\nid: 33\n\n", new EventSourceMessage().data("Hello").id("33").build());
    }

    @Test
    public void encodesComment() throws Exception {
        assertEquals("data: hello\n: hi there\n\n",
            new EventSourceMessage().data("hello").comment("hi there").build());
    }

    @Test
    public void encodesIdAsLong() throws Exception {
        assertEquals("id: 40\n\n", new EventSourceMessage().id(40L).build());
    }

    @Test
    public void encodesIdAsString() throws Exception {
        assertEquals("id: somehash\n\n", new EventSourceMessage().id("somehash").build());
    }

    @Test
    public void encodesEvent() throws Exception {
        assertEquals("event: house party!\n\n",
            new EventSourceMessage().event("house party!").build());
    }

    @Test
    public void encodesRetry() throws Exception {
        assertEquals("retry: 3\n\n", new EventSourceMessage().retry(3L).build());
    }

    @Test
    public void emptyBuild() throws Exception {
        assertEquals("\n", new EventSourceMessage().build());
    }

    @Test
    public void buildsMessageWithData() throws Exception {
        assertEquals("data: testing\n\n", new EventSourceMessage("testing").build());
    }

    @Test
    public void buildsMessageWithEmptyData() throws Exception {
        assertEquals("\n\n", new EventSourceMessage("").build());
    }

    @Test
    public void buildsMessageWithNullId() throws Exception {
        assertEquals("id\n\n", new EventSourceMessage().id(null).build());
    }
}
