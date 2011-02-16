package org.webbitserver.handler.exceptions;

import java.io.PrintStream;

/**
 * Exception handler that dumps the stack trace.
 *
 * @see org.webbitserver.WebServer#connectionExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
 * @see org.webbitserver.WebServer#uncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
 */
public class PrintStackTraceExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final PrintStream out;

    public PrintStackTraceExceptionHandler() {
        this(System.err);
    }

    public PrintStackTraceExceptionHandler(PrintStream out) {
        this.out = out;
    }

    @Override
    public void uncaughtException(Thread t, Throwable exception) {
        exception.printStackTrace(out);
    }
}
