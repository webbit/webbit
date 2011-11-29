package org.webbitserver;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * Marker for any exceptions in the Webbit stack.
 *
 * This is used to ensure the exceptions we report to {@link Thread.UncaughtExceptionHandler}s are well
 * documented and make it obvious that an error occurred in Webbit. This is particularly useful for projects
 * that make heavy use of Netty in other libraries, since most of our exceptions come out of the Netty stack,
 * and don't include Webbit code in their stack traces.
 */
public class WebbitException extends RuntimeException {
    private static final long serialVersionUID = 8212455395690231426L;

    public WebbitException() {
    }

    public WebbitException(String message) {
        super(message);
    }

    public WebbitException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebbitException(Throwable cause) {
        super(cause);
    }

    public static WebbitException fromExceptionEvent(ExceptionEvent e) {
        return new WebbitException(e.getCause().getMessage() + " on " + e.getChannel().toString(), e.getCause());
    }

    public static WebbitException fromException(Throwable t, Channel channel) {
        return new WebbitException(t.getMessage() + " on " + channel.toString(), t);
    }
}
