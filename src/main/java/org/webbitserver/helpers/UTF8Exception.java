package org.webbitserver.helpers;

import java.io.UnsupportedEncodingException;

public class UTF8Exception extends UnsupportedEncodingException {
    private static final long serialVersionUID = 3599440257713569059L;

    public UTF8Exception(String reason) {
        super(reason);
    }
}
