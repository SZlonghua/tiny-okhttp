package com.tiny.okhttp;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface Interceptor {

    Response intercept(Chain chain) throws IOException;

    interface Chain {

        Request request();

        Response proceed(Request request) throws IOException;

        int connectTimeoutMillis();

        int readTimeoutMillis();

        int writeTimeoutMillis();

    }
}
