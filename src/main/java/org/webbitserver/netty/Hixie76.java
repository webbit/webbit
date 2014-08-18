package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;

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
    private static final MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not supported on this platform");
        }
    }


    private final HttpRequest req;
    private final HttpResponse res;

    public Hixie76(HttpRequest req, HttpResponse res) {
        this.req = req;
        this.res = res;
    }

    @Override
    public boolean matches() {
        return req.headers().contains(SEC_WEBSOCKET_KEY1) && req.headers().contains(SEC_WEBSOCKET_KEY2);
    }

    @Override
    public void prepareHandshakeResponse(NettyWebSocketConnection webSocketConnection) {
        webSocketConnection.setVersion("HIXIE-76");

        res.setStatus(new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.headers().add(UPGRADE, WEBSOCKET);
        res.headers().add(CONNECTION, UPGRADE);
        res.headers().add(SEC_WEBSOCKET_ORIGIN, req.headers().get(ORIGIN));
        res.headers().add(SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
        String protocol = req.headers().get(SEC_WEBSOCKET_PROTOCOL);
        if (protocol != null) {
            res.headers().add(SEC_WEBSOCKET_PROTOCOL, protocol);
        }

        // Calculate the answer of the challenge.
        String key1 = req.headers().get(SEC_WEBSOCKET_KEY1);
        String key2 = req.headers().get(SEC_WEBSOCKET_KEY2);
        int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
        int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
        long c = req.getContent().readLong();
        ChannelBuffer input = ChannelBuffers.buffer(16);
        input.writeInt(a);
        input.writeInt(b);
        input.writeLong(c);
        ChannelBuffer output = ChannelBuffers.wrappedBuffer(MD5.digest(input.array()));
        res.setContent(output);
    }

    @Override
    public ChannelHandler createDecoder() {
        return new WebSocketFrameDecoder();
    }

    @Override
    public ChannelHandler createEncoder() {
        return new WebSocketFrameEncoder();
    }

    private String getWebSocketLocation(HttpRequest req) {
      return  getWebSocketProtocol(req) + req.headers().get(HttpHeaders.Names.HOST) + req.getUri();
    }
  
	  private String getWebSocketProtocol(HttpRequest req) {
		  if(req.headers().get(HttpHeaders.Names.ORIGIN).matches("(?s)https://.*")) { return "wss://"; } else { return "ws://"; }
	  }
}
