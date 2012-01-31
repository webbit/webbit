package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;

import static org.webbitserver.netty.HybiWebSocketFrameDecoder.applyMask;

public class EncodingHybiFrame {

    private final int opcode;
    private final boolean fin;
    private final int rsv;
    private byte[] maskingKey;
    private final ChannelBuffer data;

    public EncodingHybiFrame(int opcode, boolean fin, int rsv, byte[] maskingKey, ChannelBuffer fragment) {
        this.opcode = opcode;
        this.fin = fin;
        this.rsv = rsv;
        this.maskingKey = maskingKey;
        this.data = fragment;
    }

    public ChannelBuffer encode() throws TooLongFrameException {
        int b0 = 0;
        if (fin) {
            b0 |= (1 << 7);
        }
        b0 |= (rsv % 8) << 4;
        b0 |= opcode % 128;

        int b1 = maskingKey != null ? 0x80 : 0x00;

        int headerLength = maskingKey != null ? 6 : 2;

        ChannelBuffer header;
        int length = data.readableBytes();

        if (opcode == Opcodes.OPCODE_PING && length > 125) {
            throw new TooLongFrameException("invalid payload for PING (payload length must be <= 125, was " + length);
        }

        if (length <= 125) {
            b1 |= length & 0x7F;
            header = createBuffer(headerLength + length);
            header.writeByte(b0);
            header.writeByte(b1);
        } else if (length <= 0xFFFF) {
            b1 |= 126;
            headerLength += 2;
            header = createBuffer(headerLength + length);
            header.writeByte(b0);
            header.writeByte(b1);
            header.writeByte((length >>> 8) & 0xFF);
            header.writeByte((length) & 0xFF);
        } else {
            b1 |= 127;
            headerLength += 8;
            header = createBuffer(headerLength + length);
            header.writeByte(b0);
            header.writeByte(b1);
            header.writeLong(length);
        }

        if (maskingKey != null) {
            header.writeBytes(maskingKey);
            applyMask(data, maskingKey);
        }

        return ChannelBuffers.wrappedBuffer(header, data);
    }

    private ChannelBuffer createBuffer(int length) {
        return ChannelBuffers.buffer(length);
    }
}
