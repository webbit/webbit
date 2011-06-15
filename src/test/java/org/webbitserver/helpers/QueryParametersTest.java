package org.webbitserver.helpers;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class QueryParametersTest {
    @Test
    public void parses_one_parameter() throws Exception {
        assertEquals("bar", new QueryParameters("foo=bar").first("foo"));
    }

    @Test
    public void parses_two_parameters() throws Exception {
        assertEquals(asList("b", "e"), new QueryParameters("a=b&c=d&a=e").all("a"));
    }

    @Test
    public void parses_empty_parameter() throws Exception {
        assertEquals(null, new QueryParameters("a=").first("a"));
    }

    @Test
    public void parses_mix_of_present_and_empty_parameters() throws Exception {
        assertEquals(asList("b", null, "e"), new QueryParameters("a=b&a=&a=e").all("a"));
    }
}
