package org.webbitserver.netty;

import org.webbitserver.WebServer;
import org.webbitserver.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

public class WssClientTest extends WebSocketClientVerification {
    @Override
    protected WebServer createServer() throws IOException {
        NettyWebServer webServer = new NettyWebServer(Executors.newSingleThreadExecutor(), new InetSocketAddress(9988), URI.create("https://localhost:9988"));
        webServer.setupSsl(getClass().getResourceAsStream("/ssl/keystore"), "webbit");
        return webServer;
    }

    @Override
    protected void configure(WebSocket ws) {
        ws.setupSsl(getClass().getResourceAsStream("/ssl/keystore"), "webbit");
    }
}
