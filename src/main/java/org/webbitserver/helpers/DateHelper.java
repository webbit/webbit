package org.webbitserver.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {
    private static final ThreadLocal<DateFormat> RFC_1123 = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;
        }
    };

    private static final ThreadLocal<DateFormat> RFC_850 = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
					SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss zzz", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;
        }
    };

    public static String rfc1123Format(Date date) {
        return RFC_1123.get().format(date);
    }

    public static String rfc850Format(Date date) {
        return RFC_850.get().format(date);
    }
}
