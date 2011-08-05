// https://github.com/wulczer/txWebSocket/blob/hybi-10/websocket.py
// TODO: Use checkpoints
package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.jboss.netty.handler.codec.replay.VoidEnum;

public class HybiWebSocketFrameDecoder extends ReplayingDecoder<VoidEnum> {
    private static final byte OPCODE_CONT = 0x0;
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    public static final int DEFAULT_MAX_FRAME_SIZE = 16384;
    private Byte fragmentOpcode;
    private Byte opcode;

    private enum STATE {
        HYBI_FRAME_START,
        HYBI_PARSING_LENGTH,
        HYBI_MASKING_KEY,
        HYBI_PARSING_LENGTH_2,
        HYBI_PARSING_LENGTH_3,
        HYBI_PAYLOAD
    }

    private STATE state = STATE.HYBI_FRAME_START;

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, VoidEnum state) throws Exception {
        byte b = buffer.readByte();
        System.out.println("Integer.toBinaryString(b) = " + bits(b) + " " + hex(b));

        byte fin = (byte) (b & 0x80);
        byte reserved = (byte) (b & 0x70);
        byte opcode = (byte) (b & 0x0F);

        System.out.println("OPCODE:" + Byte.toString(opcode));

        if (reserved != 0) {
            System.err.println("Reserved bits set: " + bits(reserved) + " " + hex(reserved));
        }

        if (!isOpcode(opcode)) {
            System.err.println("Invalid opcode");
        }

        if (fin != 0) {
            if (fragmentOpcode == null) {
                fragmentOpcode = opcode;
            } else if (opcode != OPCODE_CONT) {
                System.err.println("Continuation frame with invalid opcode " + opcode);
            }
        } else {
            if (fragmentOpcode != null) {
                if (!isControlOpcode(opcode) && opcode != OPCODE_CONT) {
                    System.err.println("Final frame with invalid opcode " + opcode);
                }
            } else if (opcode == OPCODE_CONT) {
                System.err.println("Final frame with invalid opcode " + opcode);
            }
            this.opcode = opcode;
        }

        this.state = STATE.HYBI_PARSING_LENGTH;

        b = buffer.readByte();
        byte masked = (byte) (b & 0x80);
        if(masked == 0) {
            System.err.println("Unmasked frame received");
        }

        byte length = (byte) (b & 0x7F);
        System.out.println("length = " + length);

        // Read Masking key
        ChannelBuffer maskingKey = buffer.readBytes(4);

        return "HAHA";
    }

    private String bits(byte b) {
        return Integer.toBinaryString(b).substring(24);
    }

    private String hex(byte b) {
        return Integer.toHexString(b);
    }

    private boolean isOpcode(int opcode) {
        return opcode == OPCODE_CONT ||
                opcode == OPCODE_TEXT ||
                opcode == OPCODE_BINARY ||
                opcode == OPCODE_CLOSE ||
                opcode == OPCODE_PING ||
                opcode == OPCODE_PONG;
    }

    private boolean isControlOpcode(int opcode) {
        return opcode == OPCODE_CLOSE ||
                opcode == OPCODE_PING ||
                opcode == OPCODE_PONG;
    }

    private boolean isDataOpcode(int opcode) {
        return opcode == OPCODE_TEXT ||
                opcode == OPCODE_BINARY;
    }
}
