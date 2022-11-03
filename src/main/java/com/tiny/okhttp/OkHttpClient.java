package com.tiny.okhttp;

import javax.net.SocketFactory;

public class OkHttpClient implements Call.Factory {

    final SocketFactory socketFactory;

    final boolean retryOnConnectionFailure;

    final int connectTimeout;
    final int readTimeout;
    final int writeTimeout;
    final int pingInterval;

    ConnectionPool connectionPool;

    public OkHttpClient() {
        this(new Builder());
    }

    OkHttpClient(Builder builder) {

        this.socketFactory = builder.socketFactory;
        this.retryOnConnectionFailure = builder.retryOnConnectionFailure;

        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.pingInterval = builder.pingInterval;

        this.connectionPool = builder.connectionPool;
    }

    @Override
    public Call newCall(Request request) {
        return RealCall.newRealCall(this, request);
    }

    public SocketFactory socketFactory() {
        return socketFactory;
    }

    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    public int readTimeoutMillis() {
        return readTimeout;
    }

    public int writeTimeoutMillis() {
        return writeTimeout;
    }

    public int pingIntervalMillis() {
        return pingInterval;
    }

    public boolean retryOnConnectionFailure() {
        return retryOnConnectionFailure;
    }

    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    public static final class Builder {

        SocketFactory socketFactory;

        boolean retryOnConnectionFailure;

        int connectTimeout;
        int readTimeout;
        int writeTimeout;
        int pingInterval;

        ConnectionPool connectionPool;

        public Builder() {

            socketFactory = SocketFactory.getDefault();
            retryOnConnectionFailure = true;

            connectTimeout = 10_000;
            readTimeout = 10_000;
            writeTimeout = 10_000;
            pingInterval = 0;

            connectionPool = new ConnectionPool();
        }

    }

}
