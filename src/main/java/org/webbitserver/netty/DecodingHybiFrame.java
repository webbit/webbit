package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.helpers.UTF8Output;

import java.util.ArrayList;
import java.util.List;

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
        if (opcode == Opcodes.OPCODE_TEXT) {
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

    public void dispatch(WebSocketHandler handler, NettyWebSocketConnection connection) throws Throwable {
        switch (opcode) {
            case Opcodes.OPCODE_TEXT:
                handler.onMessage(connection, utf8Output.getStringAndRecycle());
                return;
            case Opcodes.OPCODE_BINARY:
                handler.onMessage(connection, messageBytes());
                return;
            case Opcodes.OPCODE_PONG:
                handler.onPong(connection, utf8Output.getStringAndRecycle());
                return;
            default:
                throw new IllegalStateException("Unexpected opcode:" + opcode);
        }
    }
}
