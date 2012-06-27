package org.webbitserver.helpers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Wraps a ThreadFactory and assigns a name to each newly created thread, that includes two
 * counters: one to indicate which ThreadFactory instance this is, and another for the thread
 * created by the factory.
 */
public class NamingThreadFactory implements ThreadFactory {
    private static volatile int factoryCount;
    private volatile int threadCount;

    private final ThreadFactory factory;
    private final String prefix;

    public NamingThreadFactory(ThreadFactory factory, String prefix) {
        this.factory = factory;
        this.prefix = prefix;
        factoryCount++;
    }

    public NamingThreadFactory(String prefix) {
        this(Executors.defaultThreadFactory(), prefix);
    }

    @Override
    public Thread newThread(Runnable r) {
        this.threadCount++;
        Thread thread = factory.newThread(r);
        thread.setName(threadName());
        return thread;
    }

    /**
     * Override this method to customize thread name.
     */
    protected String threadName() {
        return String.format("%s-%d-%d-thread", prefix, factoryCount, threadCount);
    }
}