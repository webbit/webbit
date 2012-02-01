package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.helpers.UTF8Exception;
import org.webbitserver.helpers.UTF8Output;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class DecodingHybiFrame {

    private final int opcode;
    private final UTF8Output utf8Output;

    private List<ChannelBuffer> fragments = new ArrayList<ChannelBuffer>();
    private int length;

    public DecodingHybiFrame(int opcode, UTF8Output utf8Output, ChannelBuffer fragment) throws UTF8Exception {
        this.opcode = opcode;
        this.utf8Output = utf8Output;
        append(fragment);
    }

    public void append(ChannelBuffer fragment) throws UTF8Exception {
        length += fragment.readableBytes();
        if (opcode == Opcodes.OPCODE_TEXT) {
            utf8Output.write(fragment.array());
        } else {
            fragments.add(fragment);
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

    public void dispatchMessage(final WebSocketHandler handler, final NettyWebSocketConnection connection, final Executor executor, final Thread.UncaughtExceptionHandler exceptionHandler) throws UTF8Exception {

        switch (opcode) {
            case Opcodes.OPCODE_TEXT: {
                final String messageValue = utf8Output.getStringAndRecycle();
                executor.execute(new CatchingRunnable(exceptionHandler) {
                    @Override
                    protected void go() throws Throwable {
                        handler.onMessage(connection, messageValue);
                    }
                });
                return;
            }
            case Opcodes.OPCODE_BINARY: {
                final byte[] bytes = messageBytes();
                executor.execute(new CatchingRunnable(exceptionHandler) {
                    @Override
                    public void go() throws Throwable {
                        handler.onMessage(connection, bytes);
                    }
                });
                return;
            }
            case Opcodes.OPCODE_PING: {
                final byte[] bytes = messageBytes();
                executor.execute(new CatchingRunnable(exceptionHandler) {
                    @Override
                    protected void go() throws Throwable {
                        handler.onPing(connection, bytes);
                    }
                });
                return;
            }
            case Opcodes.OPCODE_PONG: {
                final byte[] bytes = messageBytes();
                executor.execute(new CatchingRunnable(exceptionHandler) {
                    @Override
                    protected void go() throws Throwable {
                        handler.onPong(connection, bytes);
                    }
                });
                return;
            }
            default:
                throw new IllegalStateException("Unexpected opcode:" + opcode);
        }
    }
}
