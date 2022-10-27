package com.tiny.okhttp.internal.connection;

import java.util.concurrent.TimeUnit;

public final class RealConnectionPool {

    private final int maxIdleConnections;
    private final long keepAliveDurationNs;

    public RealConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);

        // Put a floor on the keep alive duration, otherwise cleanup will spin loop.
        if (keepAliveDuration <= 0) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
        }
    }
}
