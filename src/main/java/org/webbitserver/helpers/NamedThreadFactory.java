package org.webbitserver.helpers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A default {@link ThreadFactory} implementation that accepts the name prefix
 * of the created threads as a constructor argument. Otherwise, this factory
 * yields the same semantics as the thread factory returned by
 * {@link Executors#defaultThreadFactory()}.
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private static final String NAME_PATTERN = "%s-%d-thread";

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String threadNamePrefix;
    private final ThreadFactory delegate;

    /**
     * Creates a new {@link NamedThreadFactory} instance
     *
     * @param threadNamePrefix the name prefix assigned to each thread created.
     */
    public NamedThreadFactory(String threadNamePrefix) {
        this.delegate = Executors.defaultThreadFactory();
        this.threadNamePrefix = String.format(NAME_PATTERN, threadNamePrefix, poolNumber.getAndIncrement());
    }

    /**
     * Creates a new {@link Thread}
     *
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    public Thread newThread(Runnable r) {
        Thread t = delegate.newThread(r);
        t.setName(String.format("%s-%d", this.threadNamePrefix, threadNumber.getAndIncrement()));
        return t;
    }
}
