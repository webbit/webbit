package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocketx.WebSocket00FrameDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder;

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
    public boolean matches() {
        return false;
    }

    @Override
    public void prepareHandshakeResponse(NettyWebSocketConnection webSocketConnection) {
        webSocketConnection.setVersion("HIXIE-75");
        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.headers().add(UPGRADE, WEBSOCKET);
        res.headers().add(CONNECTION, HttpHeaders.Values.UPGRADE);
        String origin = req.headers().get(ORIGIN);
        if (origin != null) {
            res.headers().add(WEBSOCKET_ORIGIN, origin);
        }
        res.headers().add(WEBSOCKET_LOCATION, getWebSocketLocation(req));
        String protocol = req.headers().get(WEBSOCKET_PROTOCOL);
        if (protocol != null) {
            res.headers().add(WEBSOCKET_PROTOCOL, protocol);
        }
    }

    @Override
    public ChannelHandler createDecoder() {
        return new WebSocket00FrameDecoder();
    }

    @Override
    public ChannelHandler createEncoder() {
        return new WebSocket00FrameEncoder();
    }

    private String getWebSocketLocation(HttpRequest req) {
        return  getWebSocketProtocol(req) + req.headers().get(HttpHeaders.Names.HOST) + req.getUri();
    }
    
    private String getWebSocketProtocol(HttpRequest req) {
  	  if(req.headers().get(HttpHeaders.Names.ORIGIN).matches("(?s)https://.*")) { return "wss://"; } else { return "ws://"; }
    }
}
