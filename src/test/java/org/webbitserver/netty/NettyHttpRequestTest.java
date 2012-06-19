package org.webbitserver.netty;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.junit.Test;
import org.webbitserver.HttpRequest;

import java.net.HttpCookie;

import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.webbitserver.HttpRequest.COOKIE_HEADER;

/**
 * @author dbyrne
 */
public class NettyHttpRequestTest {

    @Test
    public void cookies(){
        HttpRequest httpRequest = new NettyHttpRequest(null, new DefaultHttpRequest(HTTP_1_1, GET, "localhost"), new Object(), 1L);

        HttpCookie a = new HttpCookie("A", "1");
        httpRequest.cookie(a);
        assertEquals("1", httpRequest.cookie("A").getValue());

        HttpCookie b = new HttpCookie("B", "2");
        httpRequest.cookie(b);
        assertEquals("2", httpRequest.cookie("B").getValue());

        assertTrue(httpRequest.header(COOKIE_HEADER).contains("A"));
    }

}
