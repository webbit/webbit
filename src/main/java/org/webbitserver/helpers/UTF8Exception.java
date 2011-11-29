package org.webbitserver.helpers;

public class UTF8Exception extends RuntimeException {
    private static final long serialVersionUID = 3599440257713569059L;

    public UTF8Exception(String reason) {
        super(reason);
    }
}
