package org.webbitserver.stub;

import org.webbitserver.*;

import java.util.concurrent.Executor;

public class StubHttpControl implements HttpControl {

    private HttpRequest request;
    private HttpResponse response;
    private WebSocketConnection webSocketConnection;
    private WebSocketHandler webSocketHandler;
    private EventSourceHandler eventSourceHandler;
    private EventSourceConnection eventSourceConnection;

    public StubHttpControl() {
    }

    public StubHttpControl(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public StubHttpControl(WebSocketConnection connection) {
        this.webSocketConnection = connection;
    }

    public StubHttpControl(HttpRequest request, HttpResponse response, WebSocketConnection connection) {
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
    public WebSocketConnection upgradeToWebSocketConnection(WebSocketHandler handler) {
        this.webSocketHandler = handler;
        return webSocketConnection;
    }

    @Override
    public WebSocketConnection createWebSocketConnection() {
        return webSocketConnection;
    }

    @Override
    public EventSourceConnection upgradeToEventSourceConnection(EventSourceHandler eventSourceHandler) {
        this.eventSourceHandler = eventSourceHandler;
        return eventSourceConnection;
    }

    public WebSocketConnection webSocketConnection() {
        return webSocketConnection;
    }

    public StubHttpControl webSocketConnection(WebSocketConnection webSocketConnection) {
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
