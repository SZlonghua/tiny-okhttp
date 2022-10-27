package com.tiny.okhttp;

import com.tiny.okhttp.internal.connection.RealConnectionPool;

import java.util.concurrent.TimeUnit;

public final class ConnectionPool {

    final RealConnectionPool delegate;

    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.delegate = new RealConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit);
    }

    public RealConnectionPool getDelegate() {
        return delegate;
    }
}
