package com.tiny.okhttp.internal.connection;

import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.OkHttpClient;
import com.tiny.okhttp.Request;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.internal.http.RealInterceptorChain;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class ConnectInterceptor implements Interceptor {

    public final OkHttpClient client;

    public ConnectInterceptor(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        log.info("ConnectInterceptor.intercept");
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        Transmitter transmitter = realChain.transmitter();

        //it's in the RetryAndFollowUpInterceptor in okhttp source
        transmitter.prepareToConnect(request);

        // We need the network to satisfy this request. Possibly for validating a conditional GET.
        boolean doExtensiveHealthChecks = !request.method().equals("GET");
        Exchange exchange = transmitter.newExchange(chain, doExtensiveHealthChecks);

        return realChain.proceed(request, transmitter, exchange);
    }
}
