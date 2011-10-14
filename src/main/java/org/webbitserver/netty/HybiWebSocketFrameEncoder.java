package org.webbitserver.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class HybiWebSocketFrameEncoder extends OneToOneEncoder {
    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof EncodingHybiFrame) {
            EncodingHybiFrame frame = (EncodingHybiFrame) msg;
            return frame.encode();
        }
        return msg;
    }
}
