package org.webbitserver.eventsource;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MessageDispatcherTest {
    private static final String ORIGIN = "http://host.com:99/foo";
    public EventSourceHandler h;
    public MessageDispatcher md;

    @Before
    public void setup() {
        h = mock(EventSourceHandler.class);
        md = new MessageDispatcher(h, ORIGIN);
    }

    @Test
    public void dispatchesSingleLineMessage() throws Exception {
        md.line("data: hello");
        md.line("");

        verify(h).onMessage(eq(new MessageEvent("hello", null, ORIGIN)));
    }

    @Test
    public void doesntFireMultipleTimesIfSeveralEmptyLines() throws Exception {
        md.line("data: hello");
        md.line("");
        md.line("");

        verify(h).onMessage(eq(new MessageEvent("hello", null, ORIGIN)));
        verifyNoMoreInteractions(h);
    }

    @Test
    public void dispatchesSingleLineMessageWithId() throws Exception {
        md.line("data: hello");
        md.line("id: 1");
        md.line("");

        verify(h).onMessage(eq(new MessageEvent("hello", "1", ORIGIN)));
    }
}
