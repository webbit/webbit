package org.webbitserver.netty;

import org.webbitserver.WebServer;
import org.webbitserver.WebSocket;

import java.io.IOException;

public class WsClientTest extends WebSocketClientVerification {
    @Override
    protected WebServer createServer() throws IOException {
        return new NettyWebServer(9988);
    }

    @Override
    protected void configure(WebSocket ws) {
    }

}
