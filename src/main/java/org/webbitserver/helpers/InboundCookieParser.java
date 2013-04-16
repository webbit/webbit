package org.webbitserver.helpers;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.Cookie;


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
                HttpCookie cookie =  new HttpCookie(n.getName(),n.getValue());
                cookie.setSecure(n.isSecure());
                cookie.setPath(n.getPath());
                cookie.setDomain(n.getDomain());
                cookie.setMaxAge(Long.valueOf(n.getMaxAge()));
                cookie.setDiscard(n.isDiscard());
                cookie.setVersion(n.getVersion());
                result.add(cookie);
            }
        return result;   
    }
}

