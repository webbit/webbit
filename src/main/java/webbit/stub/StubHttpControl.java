package webbit.stub;

import webbit.HttpControl;
import webbit.HttpRequest;
import webbit.HttpResponse;
import webbit.WebSocketHandler;

public class StubHttpControl implements HttpControl {

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response) {
        response.status(404).end();
    }

    @Override
    public void upgradeToWebSocketConnection(WebSocketHandler handler) {
        throw new UnsupportedOperationException(); // TODO
    }
}
