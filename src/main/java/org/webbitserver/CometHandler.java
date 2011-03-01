package org.webbitserver;

public interface CometHandler {
    void onOpen(CometConnection connection) throws Exception;
    void onClose(CometConnection connection) throws Exception;
}
