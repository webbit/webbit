package org.webbitserver.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Keeps track of all connections and automatically closes the ones that are stale.
 */
public class StaleConnectionTrackingHandler extends SimpleChannelHandler {
    private final Map<Channel, Long> stamps = new HashMap<Channel, Long>();
    private final long timeout;

    public StaleConnectionTrackingHandler(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        stamp(e.getChannel());
        super.channelOpen(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        stamp(e.getChannel());
        super.messageReceived(ctx, e);
    }

    private synchronized void stamp(Channel channel) {
        stamps.put(channel, System.currentTimeMillis());
    }

    public synchronized void closeStaleConnections() {
        Iterator<Map.Entry<Channel, Long>> entries = stamps.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Channel, Long> entry = entries.next();
            if (isStale(entry.getValue())) {
                entry.getKey().close();
                entries.remove();
            }
        }
    }

    private boolean isStale(Long timeStamp) {
        return System.currentTimeMillis() - timeStamp > timeout;
    }

}
