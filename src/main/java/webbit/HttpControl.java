package webbit;

public interface HttpControl {

    void nextHandler();

    void nextHandler(HttpRequest request, HttpResponse response);

    void upgradeToWebSocketConnection(WebSocketHandler handler);

}
