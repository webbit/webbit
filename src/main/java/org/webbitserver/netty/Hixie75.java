package org.webbitserver.netty;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

public class Hixie75 implements WebSocketVersion {
    private final HttpRequest req;
    private final HttpResponse res;

    public Hixie75(HttpRequest req, HttpResponse res) {
        this.req = req;
        this.res = res;
    }

    @Override
    public void prepareHandshakeResponse(NettyWebSocketConnection webSocketConnection) {
        webSocketConnection.setVersion("HIXIE-75");
        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.addHeader(UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, HttpHeaders.Values.UPGRADE);
        String origin = req.getHeader(ORIGIN);
        if (origin != null) {
            res.addHeader(WEBSOCKET_ORIGIN, origin);
        }
        res.addHeader(WEBSOCKET_LOCATION, getWebSocketLocation(req));
        String protocol = req.getHeader(WEBSOCKET_PROTOCOL);
        if (protocol != null) {
            res.addHeader(WEBSOCKET_PROTOCOL, protocol);
        }
    }

    @Override
    public boolean matches() {
        return false;
    }

    private String getWebSocketLocation(HttpRequest req) {
        // TODO: It should be wss if it was https
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + req.getUri();
    }
}
