package com.tiny.okhttp;

import com.tiny.okhttp.internal.NamedRunnable;
import com.tiny.okhttp.internal.connection.ConnectInterceptor;
import com.tiny.okhttp.internal.connection.Transmitter;
import com.tiny.okhttp.internal.http.CallServerInterceptor;
import com.tiny.okhttp.internal.http.RealInterceptorChain;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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
        try {
            client.dispatcher().executed(this);
            return getResponseWithInterceptorChain();
        } finally {
            client.dispatcher().finished(this);
        }
    }

    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
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

    /*Response getResponseWithInterceptorChain() throws IOException {
        throw new RejectedExecutionException("ddddddddddd");
    }*/

    static RealCall newRealCall(OkHttpClient client, Request originalRequest) {
        RealCall call = new RealCall(client, originalRequest);
        call.transmitter = new Transmitter(client, call);
        return call;
    }

    String redactedUrl() {
        return originalRequest.url().toString();
    }

    final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;
        private volatile AtomicInteger callsPerHost = new AtomicInteger(0);

        AsyncCall(Callback responseCallback) {
            super("OkHttp %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        void executeOn(ExecutorService executorService) {
            boolean success = false;
            try {
                executorService.execute(this);
                success = true;
            } catch (RejectedExecutionException e) {
                InterruptedIOException ioException = new InterruptedIOException("executor rejected");
                ioException.initCause(e);
                responseCallback.onFailure(RealCall.this, ioException);
            } finally {
                if (!success) {
                    client.dispatcher().finished(this); // This call is no longer running!
                }
            }
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain();
                signalledCallback = true;
                responseCallback.onResponse(RealCall.this, response);
            } catch (IOException e) {
                if (signalledCallback) {
                    // Do not signal the callback twice!
                } else {
                    responseCallback.onFailure(RealCall.this, e);
                }
            } catch (Throwable t) {
                if (!signalledCallback) {
                    IOException canceledException = new IOException("canceled due to " + t);
                    canceledException.addSuppressed(t);
                    responseCallback.onFailure(RealCall.this, canceledException);
                }
                throw t;
            } finally {
                client.dispatcher().finished(this);
            }
        }

        AtomicInteger callsPerHost() {
            return callsPerHost;
        }

        void reuseCallsPerHostFrom(RealCall.AsyncCall other) {
            this.callsPerHost = other.callsPerHost;
        }

        String host() {
            return originalRequest.url().host();
        }
    }
}
