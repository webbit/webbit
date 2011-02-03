package webbit.handler.logging;

import webbit.HttpRequest;
import webbit.WebSocketConnection;

public interface LogSink {

    void httpStart(HttpRequest request);
    void httpEnd(HttpRequest request);

    void webSocketOpen(WebSocketConnection connection);
    void webSocketClose(WebSocketConnection connection);
    void webSocketInboundData(WebSocketConnection connection, String data);
    void webSocketOutboundData(WebSocketConnection connection, String data);

    void error(HttpRequest httpRequest, Throwable error);

}
