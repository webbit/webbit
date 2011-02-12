package org.webbitserver.handler.logging;

import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;

public interface LogSink {

    void httpStart(HttpRequest request);
    void httpEnd(HttpRequest request);

    void webSocketOpen(WebSocketConnection connection);
    void webSocketClose(WebSocketConnection connection);
    void webSocketInboundData(WebSocketConnection connection, String data);
    void webSocketOutboundData(WebSocketConnection connection, String data);

    void error(HttpRequest request, Throwable error);

    void custom(HttpRequest request, String action, String data);

}
