package com.tiny.okhttp;

import javax.net.SocketFactory;

public final class Address {
    public HttpUrl url;
    final SocketFactory socketFactory;

    public Address(HttpUrl url, SocketFactory socketFactory) {
        this.url = url;
        this.socketFactory = socketFactory;
    }

    public SocketFactory socketFactory() {
        return socketFactory;
    }
}
