package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.webbitserver.WebSocketHandler;

import java.util.concurrent.Executor;

public class WebSocketConnectionHandler extends SimpleChannelUpstreamHandler {
    private final Executor executor;
    private final NettyWebSocketConnection webSocketConnection;
    private final WebSocketHandler webSocketHandler;
    private final ConnectionHelper connectionHelper;

    public WebSocketConnectionHandler(
            Executor executor,
            Thread.UncaughtExceptionHandler exceptionHandler,
            Thread.UncaughtExceptionHandler ioExceptionHandler,
            final NettyWebSocketConnection webSocketConnection,
            final WebSocketHandler webSocketHandler
    ) {
        this.executor = executor;
        this.webSocketConnection = webSocketConnection;
        this.webSocketHandler = webSocketHandler;
        this.connectionHelper = new ConnectionHelper(executor, exceptionHandler, ioExceptionHandler) {
            @Override
            protected void fireOnClose() throws Throwable {
                webSocketHandler.onClose(webSocketConnection);
            }
        };
    }

    @Override
    public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        connectionHelper.fireOnClose(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        connectionHelper.fireConnectionException(e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        final Thread.UncaughtExceptionHandler exceptionHandlerWithContext = connectionHelper.webbitExceptionWrappingExceptionHandler(e.getChannel());

        Object message = e.getMessage();
        if (message instanceof DecodingHybiFrame) {
            DecodingHybiFrame frame = (DecodingHybiFrame) message;
            frame.dispatchMessage(webSocketHandler, webSocketConnection, executor, exceptionHandlerWithContext);
        } else {
            // Hixie 75/76
            final WebSocketFrame frame = (WebSocketFrame) message;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        webSocketHandler.onMessage(webSocketConnection, frame.getTextData());
                    } catch (Throwable t) {
                        exceptionHandlerWithContext.uncaughtException(Thread.currentThread(), t);
                    }
                }
            });
        }
    }
}
