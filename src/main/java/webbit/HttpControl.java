package webbit;

public interface HttpControl {

    void nextHandler();

    void nextHandler(HttpRequest request, HttpResponse response);

    void nextHandler(HttpRequest request, HttpResponse response, HttpControl control);

    void upgradeToWebSocketConnection(WebSocketHandler handler);

}
