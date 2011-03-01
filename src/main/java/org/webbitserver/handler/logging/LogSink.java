package org.webbitserver.handler.logging;

import org.webbitserver.CometConnection;
import org.webbitserver.HttpRequest;

public interface LogSink {

    void httpStart(HttpRequest request);
    void httpEnd(HttpRequest request);

    void webSocketOpen(CometConnection connection);
    void webSocketClose(CometConnection connection);
    void webSocketInboundData(CometConnection connection, String data);
    void webSocketOutboundData(CometConnection connection, String data);

    void error(HttpRequest request, Throwable error);

    void custom(HttpRequest request, String action, String data);

}
