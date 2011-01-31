package webbit.stub;

import webbit.HttpControl;
import webbit.HttpRequest;
import webbit.HttpResponse;
import webbit.WebSocketHandler;

public class StubHttpControl implements HttpControl {

    private HttpRequest request;
    private HttpResponse response;

    public StubHttpControl() {
    }

    public StubHttpControl(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
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
    public void upgradeToWebSocketConnection(WebSocketHandler handler) {
        throw new UnsupportedOperationException(); // TODO
    }
}
