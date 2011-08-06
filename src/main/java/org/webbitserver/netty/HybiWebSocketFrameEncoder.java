package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class HybiWebSocketFrameEncoder extends OneToOneEncoder {
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) msg;
            ChannelBuffer data = frame.getBinaryData();
            ChannelBuffer encoded =
                    channel.getConfig().getBufferFactory().getBuffer(
                            data.order(), data.readableBytes() + 6);

            byte opcode = frame.isText() ? OPCODE_TEXT : OPCODE_BINARY;
            encoded.writeByte(0x80 | opcode);

            int length = data.readableBytes();
            if (125 < length && length <= 65535) {
                encoded.writeByte(126);
                encoded.writeShort(length);
            } else if (length > 65535) {
                encoded.writeByte(127);
                encoded.writeInt(length);
            } else {
                encoded.writeByte(length);
            }

            encoded.writeBytes(data, data.readerIndex(), data.readableBytes());
            encoded = encoded.slice(0, encoded.writerIndex());
            return encoded;
        }
        return msg;
    }
}
