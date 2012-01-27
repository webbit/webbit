package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.WebbitException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY1;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY2;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

public class Hixie76 implements WebSocketVersion {
    private final HttpRequest req;
    private final HttpResponse res;

    public Hixie76(HttpRequest req, HttpResponse res) {
        this.req = req;
        this.res = res;
    }

    @Override
    public void prepareHandshakeResponse(NettyWebSocketConnection webSocketConnection) {
        webSocketConnection.setVersion("HIXIE-76");

        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.addHeader(UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, UPGRADE);
        res.addHeader(SEC_WEBSOCKET_ORIGIN, req.getHeader(ORIGIN));
        res.addHeader(SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
        String protocol = req.getHeader(SEC_WEBSOCKET_PROTOCOL);
        if (protocol != null) {
            res.addHeader(SEC_WEBSOCKET_PROTOCOL, protocol);
        }

        // Calculate the answer of the challenge.
        String key1 = req.getHeader(SEC_WEBSOCKET_KEY1);
        String key2 = req.getHeader(SEC_WEBSOCKET_KEY2);
        int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
        int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
        long c = req.getContent().readLong();
        ChannelBuffer input = ChannelBuffers.buffer(16);
        input.writeInt(a);
        input.writeInt(b);
        input.writeLong(c);
        try {
            ChannelBuffer output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(input.array()));
            res.setContent(output);
        } catch (NoSuchAlgorithmException e) {
            throw new WebbitException(e);
        }
    }

    @Override
    public boolean matches() {
        return req.containsHeader(SEC_WEBSOCKET_KEY1) && req.containsHeader(SEC_WEBSOCKET_KEY2);
    }

    private String getWebSocketLocation(HttpRequest req) {
        // TODO: It should be wss if it was https
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + req.getUri();
    }
}
