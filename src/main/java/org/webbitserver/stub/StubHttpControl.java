package org.webbitserver.stub;

import org.webbitserver.EventSourceConnection;
import org.webbitserver.EventSourceHandler;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import java.util.concurrent.Executor;

public class StubHttpControl implements HttpControl {

    private HttpRequest request;
    private HttpResponse response;
    private WebSocketHandler webSocketHandler;
    private WebSocketConnection webSocketConnection;

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
        return this.webSocketConnection;
    }

    @Override
    public WebSocketConnection webSocketConnection() {
        return this.webSocketConnection;
    }

    @Override
    public EventSourceConnection upgradeToEventSourceConnection(EventSourceHandler handler) {
        throw new UnsupportedOperationException();
//        this.webSocketHandler = handler;
//        return webSocketConnection;
    }

    @Override
    public EventSourceConnection eventSourceConnection() {
        throw new UnsupportedOperationException();
//        return this.webSocketConnection;
    }

    public StubHttpControl webSocketConnection(WebSocketConnection connection) {
        this.webSocketConnection = connection;
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
