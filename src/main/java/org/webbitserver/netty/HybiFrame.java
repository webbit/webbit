package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.webbitserver.WebSocketHandler;

import java.nio.charset.Charset;

public class HybiFrame {

    public HybiFrame(int opcode, boolean fin, int rsv, ChannelBuffer data) {
        this.opcode = opcode;
        this.fin = fin;
        this.rsv = rsv;
        this.data = data;
    }

    private final int opcode;
    private final boolean fin;
    private final int rsv;
    private final ChannelBuffer data;

    public ChannelBuffer encode(Channel channel) {
        int b0 = 0;
        if (fin) {
            b0 |= (1 << 7);
        }
        b0 |= (rsv % 8) << 4;
        b0 |= opcode % 128;

        ChannelBuffer buffer;
        int length = data.readableBytes();
        if (length <= 125) {
            buffer = createBuffer(channel, data, 2);
            buffer.writeByte(b0);
            buffer.writeByte(length);
        } else if (length <= 0xFFFF) {
            buffer = createBuffer(channel, data, 4);
            buffer.writeByte(b0);
            buffer.writeByte(126);
            buffer.writeByte((length >>> 8) & 0xFF);
            buffer.writeByte((length) & 0xFF);
        } else {
            buffer = createBuffer(channel, data, 10);
            buffer.writeByte(b0);
            buffer.writeByte(127);
            buffer.writeLong(length);
        }

        buffer.writeBytes(data, data.readerIndex(), data.readableBytes());
        return buffer;
    }

    private ChannelBuffer createBuffer(Channel channel, ChannelBuffer data, int headerLength) {
        return channel.getConfig().getBufferFactory().getBuffer(data.order(), data.readableBytes() + headerLength);
    }

    public void dispatch(WebSocketHandler handler, NettyWebSocketConnection connection) throws Throwable {
        switch(opcode) {
            case Opcodes.OPCODE_TEXT:
                handler.onMessage(connection, data.toString(Charset.forName("UTF-8")));
                return;
            case Opcodes.OPCODE_BINARY:
                handler.onMessage(connection, data.array());
                return;
            case Opcodes.OPCODE_PONG:
                handler.onPong(connection, data.toString(Charset.forName("UTF-8")));
                return;
            default:
                throw new IllegalStateException("Unexpected opcode:" + opcode);
        }
    }
}
