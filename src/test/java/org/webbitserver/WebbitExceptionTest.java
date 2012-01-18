package org.webbitserver;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebbitExceptionTest {
    @Test
    public void does_not_puke_on_nulls() {
        WebbitException webbitException = WebbitException.fromException(null, null);
        assertEquals("[null throwable] on [null channel]", webbitException.getMessage());
    }

}
