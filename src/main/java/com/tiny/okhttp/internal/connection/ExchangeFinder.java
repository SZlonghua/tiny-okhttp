package com.tiny.okhttp.internal.connection;

import com.tiny.okhttp.Address;
import com.tiny.okhttp.Call;
import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.OkHttpClient;
import com.tiny.okhttp.internal.http.ExchangeCodec;

import java.io.IOException;

final class ExchangeFinder {
    private final Transmitter transmitter;
    private final RealConnectionPool connectionPool;
    private final Call call;
    private final Address address;

    ExchangeFinder(Transmitter transmitter, RealConnectionPool connectionPool,
                   Address address, Call call) {
        this.transmitter = transmitter;
        this.connectionPool = connectionPool;
        this.address = address;
        this.call = call;
    }

    public ExchangeCodec find(
            OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        int connectTimeout = chain.connectTimeoutMillis();
        int readTimeout = chain.readTimeoutMillis();
        int writeTimeout = chain.writeTimeoutMillis();
        int pingIntervalMillis = client.pingIntervalMillis();
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();

        try {
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout,
                    writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);
            return resultConnection.newCodec(client, chain);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("findHealthyConnection fail");
        }
    }

    public Exchange findExchange(
            OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks,
            Transmitter transmitter) {
        ExchangeCodec codec = find(client, chain, doExtensiveHealthChecks);
        return new Exchange(transmitter, call, this, codec);
    }

    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                                                 int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                                                 boolean doExtensiveHealthChecks) throws IOException {
        return new RealConnection();
    }
}
