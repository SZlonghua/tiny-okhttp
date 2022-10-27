package com.tiny.okhttp.internal.connection;

import com.tiny.okhttp.Connection;
import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.OkHttpClient;
import com.tiny.okhttp.internal.http.ExchangeCodec;
import com.tiny.okhttp.internal.http1.Http1ExchangeCodec;

import java.net.SocketException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class RealConnection implements Connection {

    ExchangeCodec newCodec(OkHttpClient client, Interceptor.Chain chain) throws SocketException {
        /*socket.setSoTimeout(chain.readTimeoutMillis());
        source.timeout().timeout(chain.readTimeoutMillis(), MILLISECONDS);
        sink.timeout().timeout(chain.writeTimeoutMillis(), MILLISECONDS);
        return new Http1ExchangeCodec(client, this, source, sink);*/
        return new Http1ExchangeCodec();
    }
}
