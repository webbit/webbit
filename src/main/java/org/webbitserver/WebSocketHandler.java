package org.webbitserver;

public interface WebSocketHandler extends CometHandler {
    void onMessage(CometConnection connection, String msg) throws Exception;
}
