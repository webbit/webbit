package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.helpers.Base64;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

public class Hybi implements WebSocketVersion {
    public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
    public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

    private static final Charset ASCII = Charset.forName("ASCII");
    private static final String ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final int MIN_HYBI_VERSION = 8;
    private static final MessageDigest SHA1;

    static {
        try {
            SHA1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("SHA-1 not supported on this platform");
        }
    }

    private final HttpRequest req;
    private final HttpResponse res;

    public Hybi(HttpRequest req, HttpResponse res) {
        this.req = req;
        this.res = res;
    }

    @Override
    public boolean matches() {
        return getHybiVersion() != null;
    }

    @Override
    public void prepareHandshakeResponse(NettyWebSocketConnection webSocketConnection) {
        webSocketConnection.setHybiWebSocketVersion(getHybiVersion());

        if (getHybiVersion() < MIN_HYBI_VERSION) {
            res.setStatus(HttpResponseStatus.UPGRADE_REQUIRED);
            res.setHeader(SEC_WEBSOCKET_VERSION, String.valueOf(MIN_HYBI_VERSION));
            return;
        }

        String key = req.getHeader(SEC_WEBSOCKET_KEY);
        if (key == null) {
            res.setStatus(HttpResponseStatus.BAD_REQUEST);
            return;
        }

        String accept = Base64.encode(sha1(key + ACCEPT_GUID));

        res.setStatus(new HttpResponseStatus(101, "Switching Protocols"));
        res.addHeader(UPGRADE, WEBSOCKET.toLowerCase());
        res.addHeader(CONNECTION, UPGRADE);
        res.addHeader(SEC_WEBSOCKET_ACCEPT, accept);

        String webSocketProtocol = req.getHeader(SEC_WEBSOCKET_PROTOCOL);
        if (webSocketProtocol != null) {
            res.addHeader(SEC_WEBSOCKET_PROTOCOL, webSocketProtocol);
        }
    }

    @Override
    public ChannelHandler createDecoder() {
        return HybiWebSocketFrameDecoder.serverSide();
    }

    @Override
    public ChannelHandler createEncoder() {
        return new HybiWebSocketFrameEncoder();
    }

    private Integer getHybiVersion() {
        return req.containsHeader(SEC_WEBSOCKET_VERSION) ? Integer.parseInt(req.getHeader(SEC_WEBSOCKET_VERSION).trim()) : null;
    }

    private byte[] sha1(String s) {
        return SHA1.digest(s.getBytes(ASCII));
    }
}
