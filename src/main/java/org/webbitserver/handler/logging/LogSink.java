package org.webbitserver.handler.logging;

import org.webbitserver.CometConnection;
import org.webbitserver.HttpRequest;

public interface LogSink {

    void httpStart(HttpRequest request);
    void httpEnd(HttpRequest request);

    void cometConnectionOpen(CometConnection connection);
    void cometConnectionClose(CometConnection connection);
    void webSocketInboundData(CometConnection connection, String data);
    void cometOutboundData(CometConnection connection, String data);

    void error(HttpRequest request, Throwable error);

    void custom(HttpRequest request, String action, String data);

}
