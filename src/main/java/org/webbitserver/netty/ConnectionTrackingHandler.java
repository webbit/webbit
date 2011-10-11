package org.webbitserver.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Keeps track of all connections and allows them to be closed later.
 */
public class ConnectionTrackingHandler extends SimpleChannelHandler {

    private final ChannelGroup openConnections = new DefaultChannelGroup();
    private final ReadWriteLock closingLock = new ReentrantReadWriteLock();
    private volatile boolean closed = false;

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // This lock is to deal with the race condition when closeAllConnections() is called,
        // but the server is still accepting connections. Any new channels will be automatically closed.
        closingLock.readLock().lock();
        try {
            if (closed) {
                e.getChannel().close();
            } else {
                openConnections.add(e.getChannel());
            }
        } finally {
            closingLock.readLock().unlock();
        }
    }

    public void closeAllConnections() {
        closingLock.writeLock().lock();
        try {
            closed = true;
        } finally {
            closingLock.writeLock().unlock();
        }
        openConnections.close();
    }
}
