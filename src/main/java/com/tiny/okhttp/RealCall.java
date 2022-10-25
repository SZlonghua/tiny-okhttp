package com.tiny.okhttp;

import com.tiny.okhttp.internal.connection.ConnectInterceptor;
import com.tiny.okhttp.internal.connection.Transmitter;
import com.tiny.okhttp.internal.http.CallServerInterceptor;
import com.tiny.okhttp.internal.http.RealInterceptorChain;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RealCall implements Call {

    final OkHttpClient client;

    private Transmitter transmitter;

    final Request originalRequest;

    private boolean executed;

    private RealCall(OkHttpClient client, Request originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
    }
    @Override
    public Response execute() throws IOException {
        log.info("RealCall execute");
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        return getResponseWithInterceptorChain();
    }

    Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(new ConnectInterceptor(client));
        interceptors.add(new CallServerInterceptor());

        Interceptor.Chain chain = new RealInterceptorChain(interceptors, transmitter, null, 0,
                originalRequest, this, client.connectTimeoutMillis(),
                client.readTimeoutMillis(), client.writeTimeoutMillis());
        return chain.proceed(originalRequest);
    }

    static RealCall newRealCall(OkHttpClient client, Request originalRequest) {
        RealCall call = new RealCall(client, originalRequest);
        call.transmitter = new Transmitter(client, call);
        return call;
    }
}
