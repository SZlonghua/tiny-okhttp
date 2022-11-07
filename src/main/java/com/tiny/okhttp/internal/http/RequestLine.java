package com.tiny.okhttp.internal.http;

import com.tiny.okhttp.HttpUrl;
import com.tiny.okhttp.Request;

import java.net.Proxy;

public final class RequestLine {
    public static String get(Request request, Proxy.Type proxyType) {
        StringBuilder result = new StringBuilder();
        result.append(request.method());
        result.append(' ');
        result.append(requestPath(request.url()));
        result.append(" HTTP/1.1");
        return result.toString();
    }

    public static String requestPath(HttpUrl url) {
        return url.path();
    }
}
