package com.tiny.okhttp.internal.connection;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.*;
import com.tiny.okhttp.internal.http.ExchangeCodec;

import java.io.IOException;
import java.lang.ref.Reference;
import java.net.Socket;

import static com.tiny.okhttp.internal.Util.closeQuietly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class Transmitter {

    private final OkHttpClient client;
    private final RealConnectionPool connectionPool;
    private final Call call;

    private Request request;
    private ExchangeFinder exchangeFinder;

    // Guarded by connectionPool.
    public RealConnection connection;
    private @Nullable Exchange exchange;
    private boolean exchangeRequestDone;
    private boolean exchangeResponseDone;

    private boolean noMoreExchanges;

    public Transmitter(OkHttpClient client, Call call) {
        this.client = client;
        this.connectionPool = client.connectionPool().getDelegate();
        this.call = call;
//        this.eventListener = client.eventListenerFactory().create(call);
//        this.timeout.timeout(client.callTimeoutMillis(), MILLISECONDS);
    }

    public void prepareToConnect(Request request) {
        this.request = request;
        this.exchangeFinder = new ExchangeFinder(this, connectionPool, createAddress(request.url()), call);
    }

    private Address createAddress(HttpUrl url) {
        return new Address(url,client.socketFactory());
    }

    Exchange newExchange(Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        synchronized (connectionPool) {
            if (noMoreExchanges) {
                throw new IllegalStateException("released");
            }
            if (exchange != null) {
                throw new IllegalStateException("cannot make a new request because the previous response "
                        + "is still open: please call response.close()");
            }
        }

        // The commented part is in the okhttp source code. I'll use findExchange method instead
        /*ExchangeCodec codec = exchangeFinder.find(client, chain, doExtensiveHealthChecks);
        Exchange result = new Exchange(this, call, exchangeFinder, codec);*/
        Exchange result = exchangeFinder.findExchange(client, chain, doExtensiveHealthChecks, this);

        synchronized (connectionPool) {
            this.exchange = result;
            this.exchangeRequestDone = false;
            this.exchangeResponseDone = false;
            return result;
        }
    }

    void acquireConnectionNoEvents(RealConnection connection) {

        if (this.connection != null) throw new IllegalStateException();
        this.connection = connection;
    }


    @Nullable
    IOException exchangeMessageDone(
            Exchange exchange, boolean requestDone, boolean responseDone, @Nullable IOException e) {
        boolean exchangeDone = false;
        synchronized (connectionPool) {
            if (exchange != this.exchange) {
                return e; // This exchange was detached violently!
            }
            boolean changed = false;
            if (requestDone) {
                if (!exchangeRequestDone) changed = true;
                this.exchangeRequestDone = true;
            }
            if (responseDone) {
                if (!exchangeResponseDone) changed = true;
                this.exchangeResponseDone = true;
            }
            if (exchangeRequestDone && exchangeResponseDone && changed) {
                exchangeDone = true;
                this.exchange.connection().successCount++;
                this.exchange = null;
            }
        }
        if (exchangeDone) {
            e = maybeReleaseConnection(e, false);
        }
        return e;
    }

    private @Nullable IOException maybeReleaseConnection(@Nullable IOException e, boolean force) {
        Socket socket;
        synchronized (connectionPool) {
            if (force && exchange != null) {
                throw new IllegalStateException("cannot release connection while it is in use");
            }
            socket = this.connection != null && exchange == null && (force || noMoreExchanges)
                    ? releaseConnectionNoEvents()
                    : null;
        }
        closeQuietly(socket);
        return e;
    }

    @Nullable Socket releaseConnectionNoEvents() {
        return this.connection.socket();
    }
}
