package org.webbitserver.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
    private static final ThreadLocal<DateFormat> RFC_1123 = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        }
    };

    public static String rfc1123Format(Date date) {
        return RFC_1123.get().format(date);
    }
}
