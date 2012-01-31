package org.webbitserver.helpers;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * A rather simplistic parser of "Cookie:" headers.
 */
public class InboundCookieParser {
    public static List<HttpCookie> parse(List<String> headerValues) {
        List<HttpCookie> result = new ArrayList<HttpCookie>();
        for (String headerValue : headerValues) {
            String[] nvPairs = headerValue.split(";");
            for (String nvPair : nvPairs) {
                String[] nameAndValue = nvPair.split("=");
                if (nameAndValue[1].startsWith("\"")) {
                    nameAndValue[1] = nameAndValue[1].substring(1);
                }
                if (nameAndValue[1].endsWith("\"")) {
                    nameAndValue[1] = nameAndValue[1].substring(0, nameAndValue[1].length() - 1);
                }
                result.add(new HttpCookie(nameAndValue[0], nameAndValue[1]));
            }
        }
        return result;
    }

}
