package org.webbitserver.stub;

import org.webbitserver.*;

import java.util.concurrent.Executor;

public class StubHttpControl implements HttpControl {

    private HttpRequest request;
    private HttpResponse response;
    private CometHandler cometHandler;
    private CometConnection cometConnection;

    public StubHttpControl() {
    }

    public StubHttpControl(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public StubHttpControl(CometConnection connection) {
        this.cometConnection = connection;
    }

    public StubHttpControl(HttpRequest request, HttpResponse response, CometConnection connection) {
        this.request = request;
        this.response = response;
        this.cometConnection = connection;
    }

    public HttpRequest request() {
        return request;
    }

    public HttpResponse response() {
        return response;
    }

    public StubHttpControl request(HttpRequest request) {
        this.request = request;
        return this;
    }

    public StubHttpControl response(HttpResponse response) {
        this.response = response;
        return this;
    }

    @Override
    public void nextHandler() {
        nextHandler(request, response, this);
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response) {
        nextHandler(request, response, this);
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response, HttpControl control) {
        response.status(404).end();
    }

    @Override
    public CometConnection upgradeToWebSocketConnection(CometHandler handler) {
        this.cometHandler = handler;
        return this.cometConnection;
    }

    @Override
    public CometConnection createWebSocketConnection() {
        return this.cometConnection;
    }

    @Override
    public CometConnection upgradeToEventSourceConnection(CometHandler cometHandler) {
        this.cometHandler = cometHandler;
        return cometConnection;
    }

    @Override
    public CometConnection createEventSourceConnection() {
        return this.cometConnection;
    }

    public CometConnection cometConnection() {
        return this.cometConnection;
    }

    public StubHttpControl cometConnection(CometConnection cometConnection) {
        this.cometConnection = cometConnection;
        return this;
    }

    public CometHandler cometHandler() {
        return cometHandler;
    }

    @Override
    public Executor handlerExecutor() {
        return this;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
