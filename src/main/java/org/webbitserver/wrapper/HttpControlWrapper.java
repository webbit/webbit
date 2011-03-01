package org.webbitserver.wrapper;

import org.webbitserver.*;

import java.util.concurrent.Executor;

public class HttpControlWrapper implements HttpControl {

    private HttpControl control;

    public HttpControlWrapper(HttpControl control) {
        this.control = control;
    }

    public HttpControl underlyingControl() {
        return control;
    }

    public HttpControlWrapper underlyingControl(HttpControl control) {
        this.control = control;
        return this;
    }

    public HttpControl originalControl() {
        if (control instanceof HttpControlWrapper) {
            HttpControlWrapper wrapper = (HttpControlWrapper) control;
            return wrapper.originalControl();
        } else {
            return control;
        }
    }

    @Override
    public void nextHandler() {
        control.nextHandler();
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response) {
        control.nextHandler(request, response);
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response, HttpControl control) {
        control.nextHandler(request, response, control);
    }

    @Override
    public CometConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
        return control.upgradeToWebSocketConnection(handler);
    }

    @Override
    public CometConnection createWebSocketConnection() {
        return control.createWebSocketConnection();
    }

    @Override
    public CometConnection upgradeToEventSourceConnection(CometHandler handler) {
        return control.upgradeToEventSourceConnection(handler);
    }

    @Override
    public Executor handlerExecutor() {
        return control.handlerExecutor();
    }

    @Override
    public void execute(Runnable command) {
        control.execute(command);
    }

}
