package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;
import org.webbitserver.helpers.UTF8Output;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class DecodingHybiFrame {

    private final int opcode;
    private final UTF8Output utf8Output;

    private List<ChannelBuffer> fragments = new ArrayList<ChannelBuffer>();
    private int length;

    public DecodingHybiFrame(int opcode, UTF8Output utf8Output, ChannelBuffer fragment) {
        this.opcode = opcode;
        this.utf8Output = utf8Output;
        append(fragment);
    }

    public void append(ChannelBuffer fragment) {
        fragments.add(fragment);
        length += fragment.readableBytes();
        if (opcode == Opcodes.OPCODE_TEXT || opcode == Opcodes.OPCODE_PONG) {
            utf8Output.write(fragment.array());
        }
    }

    private byte[] messageBytes() {
        byte[] result = new byte[length];
        int offset = 0;
        for (ChannelBuffer fragment : fragments) {
            byte[] array = fragment.array();
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public void dispatchMessage(final WebSocketHandler handler, final NettyWebSocketConnection connection, final Executor executor, final Thread.UncaughtExceptionHandler exceptionHandler) {
        Thread.UncaughtExceptionHandler exceptionHandlerWithWebbitContext = exceptionHandlerWithConnectionForContext(connection, exceptionHandler);

        switch (opcode) {
            case Opcodes.OPCODE_TEXT: {
                final String messageValue = utf8Output.getStringAndRecycle();
                executor.execute(new CatchingRunnable(exceptionHandlerWithWebbitContext) {
                    @Override
                    protected void go() throws Throwable {
                        handler.onMessage(connection, messageValue);
                    }
                });
                return;
            }
            case Opcodes.OPCODE_BINARY: {
                final byte[] bytes = messageBytes();
                executor.execute(new CatchingRunnable(exceptionHandlerWithWebbitContext) {
                    @Override
                    public void go() throws Throwable {
                        handler.onMessage(connection, bytes);
                    }
                });
                return;
            }
            case Opcodes.OPCODE_PONG: {
                final String pongValue = utf8Output.getStringAndRecycle();
                executor.execute(new CatchingRunnable(exceptionHandlerWithWebbitContext) {
                    @Override
                    protected void go() throws Throwable {
                        handler.onPong(connection, pongValue);
                    }
                });
                return;
            }
            default:
                throw new IllegalStateException("Unexpected opcode:" + opcode);
        }
    }

    // Uncaught exception handler including the connection for context.
    private static Thread.UncaughtExceptionHandler exceptionHandlerWithConnectionForContext(final NettyWebSocketConnection connection, final Thread.UncaughtExceptionHandler exceptionHandler) {
        return new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                exceptionHandler.uncaughtException(t, WebbitException.fromException(e, connection.getChannel()));
            }
        };
    }

}
