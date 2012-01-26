package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executor;

public class WebSocketConnectionHandler extends SimpleChannelUpstreamHandler {
    private final WebSocketHandler webSocketHandler;
    private final Executor executor;
    private final NettyWebSocketConnection webSocketConnection;
    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler ioExceptionHandler;

    public WebSocketConnectionHandler(NettyWebSocketConnection webSocketConnection, Thread.UncaughtExceptionHandler exceptionHandler, Thread.UncaughtExceptionHandler ioExceptionHandler, WebSocketHandler webSocketHandler, Executor executor) {
        this.webSocketConnection = webSocketConnection;
        this.exceptionHandler = exceptionHandler;
        this.ioExceptionHandler = ioExceptionHandler;
        this.webSocketHandler = webSocketHandler;
        this.executor = executor;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object message = e.getMessage();
        if (message instanceof DecodingHybiFrame) {
            DecodingHybiFrame frame = (DecodingHybiFrame) message;
            frame.dispatchMessage(webSocketHandler, webSocketConnection, executor, exceptionHandler);
        } else {
            // Hixie 75/76
            final WebSocketFrame frame = (WebSocketFrame) message;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        webSocketHandler.onMessage(webSocketConnection, frame.getTextData());
                    } catch (Throwable throwable) {
                        exceptionHandler.uncaughtException(Thread.currentThread(), WebbitException.fromException(throwable, e.getChannel()));
                    }
                }
            });
        }
    }

    @Override
    public void channelUnbound(ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        final Thread thread = Thread.currentThread();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocketHandler.onClose(webSocketConnection);
                } catch (Exception e1) {
                    exceptionHandler.uncaughtException(thread, WebbitException.fromException(e1, e.getChannel()));
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ClosedChannelException) {
            e.getChannel().close();
        } else {
            final Thread thread = Thread.currentThread();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ioExceptionHandler.uncaughtException(thread, WebbitException.fromExceptionEvent(e));
                }
            });
        }
    }
}
