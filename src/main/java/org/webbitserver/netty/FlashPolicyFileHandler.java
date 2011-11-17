package org.webbitserver.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.CharsetUtil;

public class FlashPolicyFileHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Channel ch = e.getChannel();
		ChannelBuffer response = getPolicyFileContents(ch);
		ChannelFuture future = ch.write(response);
		future.addListener(ChannelFutureListener.CLOSE);
		ctx.getPipeline().remove(this);
	}

	private ChannelBuffer getPolicyFileContents(Channel ch) throws Exception {

		InetSocketAddress address = (InetSocketAddress) ch.getLocalAddress();
		int port = address.getPort();

		return ChannelBuffers.copiedBuffer(
			"<?xml version=\"1.0\"?>\r\n"
			+ "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\r\n"
			+ "<cross-domain-policy>\r\n"
			+ "  <site-control permitted-cross-domain-policies=\"master-only\"/>\r\n"
			+ "  <allow-access-from domain=\"*\" to-ports=\"" + port + "\" />\r\n"
			+ "</cross-domain-policy>\r\n",
			CharsetUtil.US_ASCII);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

}
