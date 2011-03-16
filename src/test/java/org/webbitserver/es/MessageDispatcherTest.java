package org.webbitserver.es;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MessageDispatcherTest {
    public EventSourceHandler h;
    public MessageDispatcher md;

    @Before
    public void setup() {
        h = mock(EventSourceHandler.class);
        md = new MessageDispatcher(h);
    }

    @Test
    public void dispatchesSingleLineMessage() throws Exception {
        md.line("data: hello");
        md.line("");

        verify(h).onMessage(eq(new MessageEvent("hello", null)));
    }

    @Test
    public void doesntFireMultipleTimesIfSeveralEmptyLines() throws Exception {
        md.line("data: hello");
        md.line("");
        md.line("");

        verify(h).onMessage(eq(new MessageEvent("hello", null)));
        verifyNoMoreInteractions(h);
    }

    @Test
    public void dispatchesSingleLineMessageWithId() throws Exception {
        md.line("data: hello");
        md.line("id: 1");
        md.line("");

        verify(h).onMessage(eq(new MessageEvent("hello", "1")));
    }
}
