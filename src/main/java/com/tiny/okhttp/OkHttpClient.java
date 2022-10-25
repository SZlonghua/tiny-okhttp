package com.tiny.okhttp;

public class OkHttpClient implements Call.Factory {

    final int connectTimeout;
    final int readTimeout;
    final int writeTimeout;

    public OkHttpClient() {
        this(new Builder());
    }

    OkHttpClient(Builder builder) {
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
    }

    @Override
    public Call newCall(Request request) {
        return RealCall.newRealCall(this, request);
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

    public static final class Builder {

        int connectTimeout;
        int readTimeout;
        int writeTimeout;

        public Builder() {
            connectTimeout = 10_000;
            readTimeout = 10_000;
            writeTimeout = 10_000;
        }

    }

}
