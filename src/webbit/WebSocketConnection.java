package webbit;

public interface WebSocketConnection {
    HttpRequest httpRequest();
    WebSocketConnection send(String message);
    WebSocketConnection close();
}
