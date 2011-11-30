package org.webbitserver.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.util.CharsetUtil;

/**
 * Checks the received {@link org.jboss.netty.buffer.ChannelBuffer
 * ChannelBuffer}s for Flash policy file requests.
 * 
 * <p>
 * If this decoder detects a Flash policy file request it adds a
 * {@link FlashPolicyFileHandler} to the
 * {@link org.jboss.netty.channel.ChannelPipeline ChannelPipeline} and removes
 * itself from the pipeline. If a Flash policy file request is not detected in
 * the first 23 bytes of the buffer, the decoder removes itself from the
 * pipeline.
 * <p>
 * 
 * <p>
 * This implementation is based on the
 * "replacing a decoder with another decoder in a pipeline" section of the
 * {@link org.jboss.netty.handler.codec.frame.FrameDecoder FrameDecoder}
 * documentation.
 * </p>
 */
public class FlashPolicyFileDecoder extends FrameDecoder {

	private final int publicPort;

	private static final ChannelBuffer FLASH_POLICY_REQUEST = ChannelBuffers
			.copiedBuffer("<policy-file-request/>\0", CharsetUtil.US_ASCII);

	public FlashPolicyFileDecoder(int publicPort) {
		super(true);
		this.publicPort = publicPort;
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
						new FlashPolicyFileHandler(this.publicPort));
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
