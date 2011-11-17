package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.util.CharsetUtil;

public class FlashPolicyFileDecoder extends FrameDecoder {

	private static final ChannelBuffer FLASH_POLICY_REQUEST = ChannelBuffers
			.copiedBuffer("<policy-file-request/>\0", CharsetUtil.US_ASCII);

	public FlashPolicyFileDecoder() {
		super(true);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {

		// Will use the first 23 bytes to detect the policy file request.
		if (buffer.readableBytes() >= 23) {
			ChannelPipeline p = ctx.getPipeline();
			ChannelBuffer firstMessage = buffer.readBytes(23);

			if (FLASH_POLICY_REQUEST.equals(firstMessage)) {
				p.addAfter("flashpolicydecoder", "flashpolicyhandler",
						new FlashPolicyFileHandler());
			}

			p.remove(this);

			if (buffer.readable()) {
				return new Object[] { firstMessage, buffer.readBytes(buffer.readableBytes()) };
			} else {
				return firstMessage;
			}

		}

		// Forward the current buffer as is to handlers.
		return buffer.readBytes(buffer.readableBytes());

	}

}
