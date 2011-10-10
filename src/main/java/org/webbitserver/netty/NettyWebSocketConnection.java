package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.webbitserver.WebSocketConnection;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class NettyWebSocketConnection implements WebSocketConnection {

    private final Executor executor;
    private final NettyHttpRequest nettyHttpRequest;
    private final ChannelHandlerContext ctx;
    private Version version;

    public NettyWebSocketConnection(Executor executor, NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx) {
        this.executor = executor;
        this.nettyHttpRequest = nettyHttpRequest;
        this.ctx = ctx;
    }

    @Override
    public NettyHttpRequest httpRequest() {
        return nettyHttpRequest;
    }

    @Override
    public NettyWebSocketConnection send(String message) {
        return send(new HybiFrame(Opcodes.OPCODE_TEXT, true, 0, ChannelBuffers.wrappedBuffer(message.getBytes(Charset.forName("UTF-8")))));
    }

    @Override
    public NettyWebSocketConnection send(byte[] message) {
        return send(new HybiFrame(Opcodes.OPCODE_BINARY, true, 0, ChannelBuffers.wrappedBuffer(message)));
    }

    @Override
    public NettyWebSocketConnection ping(String message) {
        return send(new HybiFrame(Opcodes.OPCODE_PING, true, 0, ChannelBuffers.wrappedBuffer(message.getBytes(Charset.forName("UTF-8")))));
    }

    private NettyWebSocketConnection send(HybiFrame frame) {
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

    void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public Version version() {
        return version;
    }
}
