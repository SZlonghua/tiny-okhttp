package com.tiny.okhttp.internal.http;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.Call;
import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.Request;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.internal.connection.Exchange;
import com.tiny.okhttp.internal.connection.Transmitter;

import java.io.IOException;
import java.util.List;

public final class RealInterceptorChain implements Interceptor.Chain {

    private final List<Interceptor> interceptors;
    private final Transmitter transmitter;
    private final @Nullable Exchange exchange;
    private final int index;
    private final Request request;
    private final Call call;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    private int calls;

    public RealInterceptorChain(List<Interceptor> interceptors, Transmitter transmitter,
                                @Nullable Exchange exchange, int index, Request request, Call call,
                                int connectTimeout, int readTimeout, int writeTimeout) {
        this.interceptors = interceptors;
        this.transmitter = transmitter;
        this.exchange = exchange;
        this.index = index;
        this.request = request;
        this.call = call;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
    }


    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) throws IOException {
        return proceed(request, transmitter, exchange);
    }

    @Override
    public int connectTimeoutMillis() {
        return connectTimeout;
    }

    @Override
    public int readTimeoutMillis() {
        return readTimeout;
    }

    @Override
    public int writeTimeoutMillis() {
        return writeTimeout;
    }

    public Response proceed(Request request, Transmitter transmitter, @Nullable Exchange exchange)
            throws IOException {
        if (index >= interceptors.size()) throw new AssertionError();

        calls++;

        // Call the next interceptor in the chain.
        RealInterceptorChain next = new RealInterceptorChain(interceptors, transmitter, exchange,
                index + 1, request, call, connectTimeout, readTimeout, writeTimeout);
        Interceptor interceptor = interceptors.get(index);
        Response response = interceptor.intercept(next);

        // Confirm that the intercepted response isn't null.
        if (response == null) {
            throw new NullPointerException("interceptor " + interceptor + " returned null");
        }

        if (response.body() == null) {
            throw new IllegalStateException(
                    "interceptor " + interceptor + " returned a response with no body");
        }

        return response;
    }

    public Transmitter transmitter() {
        return transmitter;
    }
}
