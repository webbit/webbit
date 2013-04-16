package org.webbitserver.helpers;

import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InboundCookieParser {
    public static List<HttpCookie> parse(List<String> headerValues) {
        List<HttpCookie> result = new ArrayList<HttpCookie>();
        for (String headerValue : headerValues) {
            result.addAll(toHttpCookie(new CookieDecoder().decode(headerValue)));
        }
        return result;
    }

    private static List<HttpCookie> toHttpCookie(Set<Cookie> nettyCookies) {
        List<HttpCookie> result = new ArrayList<HttpCookie>();
        for (Cookie n : nettyCookies) {
            HttpCookie cookie = new HttpCookie(n.getName(), n.getValue());
            cookie.setSecure(n.isSecure());
            cookie.setPath(n.getPath());
            cookie.setDomain(n.getDomain());
            // Unspecified max-age in Netty is Integer.MIN_VALUE, while it's -1 in java.net.HttpCookie
            long maxAge = n.getMaxAge() == Integer.MIN_VALUE ? -1 : n.getMaxAge();
            cookie.setMaxAge(maxAge);
            cookie.setDiscard(n.isDiscard());
            cookie.setVersion(n.getVersion());
            result.add(cookie);
        }
        return result;
    }
}

