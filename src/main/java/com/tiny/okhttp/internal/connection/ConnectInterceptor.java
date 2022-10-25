package com.tiny.okhttp.internal.connection;

import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.OkHttpClient;
import com.tiny.okhttp.Response;
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
        System.out.println("dddddd");
        return chain.proceed(chain.request());
    }
}
