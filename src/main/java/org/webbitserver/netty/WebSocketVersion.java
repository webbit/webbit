package org.webbitserver.netty;

public interface WebSocketVersion {
    void prepareHandshakeResponse(NettyWebSocketConnection webSocketConnection);
    boolean matches();
}
