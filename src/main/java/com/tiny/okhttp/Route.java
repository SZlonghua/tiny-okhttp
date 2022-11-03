package com.tiny.okhttp;

import java.net.InetSocketAddress;
import java.net.Proxy;

public final class Route {

    final Address address;
    final Proxy proxy;
    final InetSocketAddress inetSocketAddress;

    public Route(Address address, Proxy proxy, InetSocketAddress inetSocketAddress) {
        if (address == null) {
            throw new NullPointerException("address == null");
        }
        if (proxy == null) {
            throw new NullPointerException("proxy == null");
        }
        if (inetSocketAddress == null) {
            throw new NullPointerException("inetSocketAddress == null");
        }
        this.address = address;
        this.proxy = proxy;
        this.inetSocketAddress = inetSocketAddress;
    }

    public Address address() {
        return address;
    }

    public Proxy proxy() {
        return proxy;
    }

    public InetSocketAddress socketAddress() {
        return inetSocketAddress;
    }
}
