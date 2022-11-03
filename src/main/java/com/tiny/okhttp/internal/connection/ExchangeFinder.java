package com.tiny.okhttp.internal.connection;

import com.tiny.okhttp.*;
import com.tiny.okhttp.internal.http.ExchangeCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Slf4j
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
        log.info("findHealthyConnection doExtensiveHealthChecks:{}",doExtensiveHealthChecks);
        return findConnection(connectTimeout, readTimeout, writeTimeout,
                pingIntervalMillis, connectionRetryEnabled);
    }

    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout,
                                          int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address.url.host(), address.url.port());
        Route selectedRoute = new Route(address, Proxy.NO_PROXY, inetSocketAddress);

        RealConnection result = new RealConnection(connectionPool, selectedRoute);
        result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis,
                connectionRetryEnabled, call);
        transmitter.acquireConnectionNoEvents(result);
        return result;
    }
}
