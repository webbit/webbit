package org.webbitserver.helpers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps a ThreadFactory and assigns a name to each newly created thread, that includes two
 * counters: one to indicate which ThreadFactory instance this is, and another for the thread
 * created by the factory.
 */
public class NamingThreadFactory implements ThreadFactory {
    private static final AtomicInteger factoryCount = new AtomicInteger();
    private final AtomicInteger threadCount = new AtomicInteger();

    private final ThreadFactory factory;
    private final String prefix;

    public NamingThreadFactory(ThreadFactory factory, String prefix) {
        this.factory = factory;
        this.prefix = prefix;
        factoryCount.incrementAndGet();
    }

    public NamingThreadFactory(String prefix) {
        this(Executors.defaultThreadFactory(), prefix);
    }

    @Override
    public Thread newThread(Runnable r) {
        threadCount.incrementAndGet();
        Thread thread = factory.newThread(r);
        thread.setName(threadName());
        return thread;
    }

    /**
     * Override this method to customize thread name.
     */
    protected String threadName() {
        return String.format("%s-%d-%d-thread", prefix, factoryCount.intValue(), threadCount.intValue());
    }
}