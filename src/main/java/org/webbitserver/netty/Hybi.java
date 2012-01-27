package org.webbitserver.netty;

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
    private static final String SEC_WEB_SOCKET_VERSION = "Sec-WebSocket-Version";
    private static final String SEC_WEB_SOCKET_ACCEPT = "Sec-WebSocket-Accept";
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";
    private static final Charset ASCII = Charset.forName("ASCII");
    private static final String ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final int MIN_HYBI_VERSION = 8;
    private static final MessageDigest SHA_1;

    static {
        try {
            SHA_1 = MessageDigest.getInstance("SHA1");
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
    public void prepareHandshakeResponse(NettyWebSocketConnection webSocketConnection) {
        webSocketConnection.setHybiWebSocketVersion(getHybiVersion());

        if (getHybiVersion() < MIN_HYBI_VERSION) {
            res.setStatus(HttpResponseStatus.UPGRADE_REQUIRED);
            res.setHeader(SEC_WEB_SOCKET_VERSION, String.valueOf(MIN_HYBI_VERSION));
            return;
        }

        String key = req.getHeader(SEC_WEB_SOCKET_KEY);
        if (key == null) {
            res.setStatus(HttpResponseStatus.BAD_REQUEST);
            return;
        }

        String accept = Base64.encode(sha1(key + ACCEPT_GUID));

        res.setStatus(new HttpResponseStatus(101, "Switching Protocols"));
        res.addHeader(UPGRADE, WEBSOCKET.toLowerCase());
        res.addHeader(CONNECTION, UPGRADE);
        res.addHeader(SEC_WEB_SOCKET_ACCEPT, accept);
    }

    @Override
    public boolean matches() {
        return getHybiVersion() != null;
    }

    private Integer getHybiVersion() {
        return req.containsHeader(SEC_WEB_SOCKET_VERSION) ? Integer.parseInt(req.getHeader(SEC_WEB_SOCKET_VERSION).trim()) : null;
    }

    private byte[] sha1(String s) {
        return SHA_1.digest(s.getBytes(ASCII));
    }
}
