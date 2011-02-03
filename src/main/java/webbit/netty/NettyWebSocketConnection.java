package webbit.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import webbit.WebSocketConnection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class NettyWebSocketConnection implements WebSocketConnection {

    private final Executor executor;
    private final NettyHttpRequest nettyHttpRequest;
    private final ChannelHandlerContext ctx;

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
    public WebSocketConnection send(String message) {
        ctx.getChannel().write(new DefaultWebSocketFrame(message));
        return this;
    }

    @Override
    public WebSocketConnection close() {
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

}
