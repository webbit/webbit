package org.webbitserver.stub;

import org.webbitserver.*;

import java.util.concurrent.Executor;

public class StubHttpControl implements HttpControl {

    private HttpRequest request;
    private HttpResponse response;
    private CometConnection webSocketConnection;
    private WebSocketHandler webSocketHandler;
    private CometHandler eventSourceHandler;
    private CometConnection cometConnection;

    public StubHttpControl() {
    }

    public StubHttpControl(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public StubHttpControl(CometConnection connection) {
        this.webSocketConnection = connection;
    }

    public StubHttpControl(HttpRequest request, HttpResponse response, CometConnection connection) {
        this.request = request;
        this.response = response;
        this.webSocketConnection = connection;
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
    public CometConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
        this.webSocketHandler = handler;
        return webSocketConnection;
    }

    @Override
    public CometConnection createWebSocketConnection() {
        return webSocketConnection;
    }

    @Override
    public CometConnection upgradeToEventSourceConnection(CometHandler cometHandler) {
        this.eventSourceHandler = cometHandler;
        return cometConnection;
    }

    public CometConnection webSocketConnection() {
        return webSocketConnection;
    }

    public StubHttpControl webSocketConnection(CometConnection webSocketConnection) {
        this.webSocketConnection = webSocketConnection;
        return this;
    }

    public WebSocketHandler webSocketHandler() {
        return webSocketHandler;
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
