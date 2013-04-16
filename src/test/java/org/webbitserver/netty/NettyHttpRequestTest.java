package org.webbitserver.netty;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Test;
import org.webbitserver.netty.NettyHttpRequest;

import static org.junit.Assert.assertEquals;

public class NettyHttpRequestTest {

    @Test
    public void decodesQueryParams() {
        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "http://example.com/?foo=bar");
        NettyHttpRequest nhr = new NettyHttpRequest(null, httpRequest, null, 0L);
        assertEquals(nhr.queryParam("foo"), "bar");
    }

    @Test
    public void decodesQueryParamsContainingEncodedEquals() {
        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "http://example.com/?foo=a%2Bb%3Dc");
        NettyHttpRequest nhr = new NettyHttpRequest(null, httpRequest, null, 0L);
        assertEquals(nhr.queryParam("foo"), "a+b=c");
    }
}