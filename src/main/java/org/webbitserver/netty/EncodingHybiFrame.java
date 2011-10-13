package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.webbitserver.helpers.UTF8Output;

import java.util.ArrayList;

public class EncodingHybiFrame {

    private final int opcode;
    private final boolean fin;
    private final int rsv;
    private final ChannelBuffer data;

    public EncodingHybiFrame(int opcode, boolean fin, int rsv, ChannelBuffer fragment) {
        this.opcode = opcode;
        this.fin = fin;
        this.rsv = rsv;
        this.data = fragment;
    }

    public ChannelBuffer encode() throws TooLongFrameException {
        int b0 = 0;
        if (fin) {
            b0 |= (1 << 7);
        }
        b0 |= (rsv % 8) << 4;
        b0 |= opcode % 128;

        ChannelBuffer header;
        int length = data.readableBytes();

        if (opcode == Opcodes.OPCODE_PING && length > 125) {
            throw new TooLongFrameException("invalid payload for PING (payload length must be <= 125, was " + length);
        }

        if (length <= 125) {
            header = createBuffer(length + 2);
            header.writeByte(b0);
            header.writeByte(length);
        } else if (length <= 0xFFFF) {
            header = createBuffer(length + 4);
            header.writeByte(b0);
            header.writeByte(126);
            header.writeByte((length >>> 8) & 0xFF);
            header.writeByte((length) & 0xFF);
        } else {
            header = createBuffer(length + 10);
            header.writeByte(b0);
            header.writeByte(127);
            header.writeLong(length);
        }

        return ChannelBuffers.wrappedBuffer(header, data);
    }

    private ChannelBuffer createBuffer(int length) {
        return ChannelBuffers.buffer(length);
    }
}
