package org.webbitserver.netty;

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

import java.util.concurrent.Executor;

/**
 * Responds with a Flash socket policy file.
 * <p/>
 * <p>
 * This implementation is based on the
 * <a href="https://github.com/waywardmonkeys/netty-flash-crossdomain-policy-server"
 * >waywardmonkeys/netty-flash-crossdomain-policy-server</a> project and the
 * <a href="http://www.adobe.com/devnet/flashplayer/articles/socket_policy_files.html"
 * ><em>Setting up a socket policy file server</em></a> article.
 * </p>
 */

public class FlashPolicyFileHandler extends SimpleChannelUpstreamHandler {

    private final int publicPort;
    private final ConnectionHelper connectionHelper;

    public FlashPolicyFileHandler(Executor executor, Thread.UncaughtExceptionHandler exceptionHandler, Thread.UncaughtExceptionHandler ioExceptionHandler, int publicPort) {
        this.publicPort = publicPort;
        this.connectionHelper = new ConnectionHelper(executor, exceptionHandler, ioExceptionHandler) {
            @Override
            protected void fireOnClose() throws Exception {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Channel ch = e.getChannel();
        ChannelBuffer response = getPolicyFileContents();
        ChannelFuture future = ch.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
        ctx.getPipeline().remove(this);
    }

    private ChannelBuffer getPolicyFileContents() throws Exception {

        return ChannelBuffers.copiedBuffer(
                "<?xml version=\"1.0\"?>\r\n"
                        + "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\r\n"
                        + "<cross-domain-policy>\r\n"
                        + "  <site-control permitted-cross-domain-policies=\"master-only\"/>\r\n"
                        + "  <allow-access-from domain=\"*\" to-ports=\"" + this.publicPort + "\" />\r\n"
                        + "</cross-domain-policy>\r\n",
                CharsetUtil.US_ASCII);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        connectionHelper.fireConnectionException(e);
    }

}
