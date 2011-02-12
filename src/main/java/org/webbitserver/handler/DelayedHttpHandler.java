package org.webbitserver.handler;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 * Wraps a standard HttpHandler, and will introduce an artificial delay. Useful for testing
 * how things will behave when they are slow.
 */
public class DelayedHttpHandler implements HttpHandler {

    private final Executor executor;
    private Timer timer;
    private final long delayInMillis;
    private final HttpHandler handler;

    public DelayedHttpHandler(Executor executor, long delayInMillis, HttpHandler handler) {
        this.delayInMillis = delayInMillis;
        this.handler = handler;
        timer = new Timer();
        this.executor = executor;
    }

    @Override
    public void handleHttpRequest(final HttpRequest request, final HttpResponse response, final HttpControl control) throws Exception {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handler.handleHttpRequest(request, response, control);
                        } catch (Exception e) {
                            // TODO
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, delayInMillis);
    }
}
