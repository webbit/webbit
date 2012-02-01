package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.util.CharsetUtil;
import org.webbitserver.WebSocketConnection;

import java.util.concurrent.Executor;

public class NettyWebSocketConnection extends AbstractHttpConnection implements WebSocketConnection {

    private final byte[] outboundMaskingKey;
    private String version;
    private boolean hybi;

    public NettyWebSocketConnection(Executor executor, NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx, byte[] outboundMaskingKey) {
        super(ctx, nettyHttpRequest, executor);
        this.outboundMaskingKey = outboundMaskingKey;
    }

    @Override
    public NettyWebSocketConnection send(String message) {
        if (hybi) {
            writeMessage(new EncodingHybiFrame(Opcodes.OPCODE_TEXT, true, 0, outboundMaskingKey, ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8)));
        } else {
            writeMessage(new DefaultWebSocketFrame(message));
        }
        return this;
    }

    @Override
    public NettyWebSocketConnection send(byte[] message) {
        writeMessage(new EncodingHybiFrame(Opcodes.OPCODE_BINARY, true, 0, outboundMaskingKey, ChannelBuffers.wrappedBuffer(message)));
        return this;
    }

    @Override
    public NettyWebSocketConnection ping(byte[] msg) {
        writeMessage(new EncodingHybiFrame(Opcodes.OPCODE_PING, true, 0, outboundMaskingKey, ChannelBuffers.wrappedBuffer(msg)));
        return this;
    }

    @Override
    public NettyWebSocketConnection pong(byte[] msg) {
        writeMessage(new EncodingHybiFrame(Opcodes.OPCODE_PONG, true, 0, outboundMaskingKey, ChannelBuffers.wrappedBuffer(msg)));
        return this;
    }

    @Override
    public NettyWebSocketConnection close() {
        closeChannel();
        return this;
    }

    @Override
    public NettyWebSocketConnection data(String key, Object value) {
        putData(key, value);
        return this;
    }

    @Override
    public String version() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    public void setHybiWebSocketVersion(int webSocketVersion) {
        setVersion("Sec-WebSocket-Version-" + webSocketVersion);
        hybi = true;
    }


}
