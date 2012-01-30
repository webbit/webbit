package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.WebbitException;
import org.webbitserver.helpers.UTF8Output;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class DecodingHybiFrame {

    private final UTF8Output utf8Output = new UTF8Output();
    private final int opcode;

    private final List<ChannelBuffer> byteMessageFragments = new ArrayList<ChannelBuffer>();
    private final StringBuilder stringMessageFragments = new StringBuilder();
    private int length;

    public DecodingHybiFrame(int opcode, ChannelBuffer fragment) {
        this.opcode = opcode;
        append(fragment);
    }

    public void append(ChannelBuffer fragment) {
        length += fragment.readableBytes();
        if (opcode == Opcodes.OPCODE_TEXT || opcode == Opcodes.OPCODE_PONG) {
            utf8Output.write(fragment.array());
            stringMessageFragments.append(utf8Output.getStringAndRecycle());
        } else {
            byteMessageFragments.add(fragment);
        }
    }

    public void dispatchMessage(final WebSocketHandler handler, final NettyWebSocketConnection connection, final Executor executor, final Thread.UncaughtExceptionHandler exceptionHandler) {
        Thread.UncaughtExceptionHandler exceptionHandlerWithWebbitContext = exceptionHandlerWithConnectionForContext(connection, exceptionHandler);

        switch (opcode) {
            case Opcodes.OPCODE_TEXT: {
                executor.execute(new CatchingRunnable(exceptionHandlerWithWebbitContext) {
                    @Override
                    protected void go() throws Throwable {
                        handler.onMessage(connection, stringMessageFragments.toString());
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

    private byte[] messageBytes() {
        byte[] result = new byte[length];
        int offset = 0;
        for (ChannelBuffer fragment : byteMessageFragments) {
            byte[] array = fragment.array();
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
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
