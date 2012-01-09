package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.util.CharsetUtil;
import org.webbitserver.WebSocketConnection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class NettyWebSocketConnection implements WebSocketConnection {

    private final Executor executor;
    private final NettyHttpRequest nettyHttpRequest;
    private final ChannelHandlerContext ctx;
    private final byte[] outboundMaskingKey;
    private String version;
    private boolean hybi;

    public NettyWebSocketConnection(Executor executor, NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx, byte[] outboundMaskingKey) {
        this.executor = executor;
        this.nettyHttpRequest = nettyHttpRequest;
        this.ctx = ctx;
        this.outboundMaskingKey = outboundMaskingKey;
    }

    @Override
    public NettyHttpRequest httpRequest() {
        return nettyHttpRequest;
    }

    @Override
    public NettyWebSocketConnection send(String message) {
        if (hybi) {
            return write(new EncodingHybiFrame(Opcodes.OPCODE_TEXT, true, 0, outboundMaskingKey, ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8)));
        } else {
            return write(new DefaultWebSocketFrame(message));
        }
    }

    @Override
    public NettyWebSocketConnection send(byte[] message) {
        return write(new EncodingHybiFrame(Opcodes.OPCODE_BINARY, true, 0, outboundMaskingKey, ChannelBuffers.wrappedBuffer(message)));
    }

    @Override
    public NettyWebSocketConnection ping(String message) {
        return write(new EncodingHybiFrame(Opcodes.OPCODE_PING, true, 0, outboundMaskingKey, ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8)));
    }

    private NettyWebSocketConnection write(Object frame) {
        ctx.getChannel().write(frame);
        return this;
    }

    @Override
    public NettyWebSocketConnection close() {
        ctx.getChannel().close();
        return this;
    }

    @Override
    public Map<String, Object> data() {
        return nettyHttpRequest.data();
    }

    @Override
    public Object data(String key) {
        return data().get(key);
    }

    @Override
    public NettyWebSocketConnection data(String key, Object value) {
        data().put(key, value);
        return this;
    }

    @Override
    public Set<String> dataKeys() {
        return data().keySet();
    }

    @Override
    public Executor handlerExecutor() {
        return executor;
    }

    @Override
    public void execute(Runnable command) {
        handlerExecutor().execute(command);
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

    Channel getChannel() {
        return ctx.getChannel();
    }
}
