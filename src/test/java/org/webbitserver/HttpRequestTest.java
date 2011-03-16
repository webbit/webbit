package org.webbitserver;

import org.junit.Test;
import org.webbitserver.stub.StubHttpRequest;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class HttpRequestTest {
    @Test
    public void extractsSingleQueryParameter() throws Exception {
        HttpRequest req = new StubHttpRequest("http://host.com:8080/path?fish=cod&fruit=orange");
        assertEquals("cod", req.queryParam("fish"));
    }

    @Test
    public void extractsMultipleQueryParameters() throws Exception {
        HttpRequest req = new StubHttpRequest("http://host.com:8080/path?fish=cod&fruit=orange&fish=smørflyndre");
        assertEquals(asList("cod", "smørflyndre"), req.queryParams("fish"));
    }
}
