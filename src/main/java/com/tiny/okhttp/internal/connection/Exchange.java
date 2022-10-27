package com.tiny.okhttp.internal.connection;

import com.tiny.okhttp.Call;
import com.tiny.okhttp.internal.http.ExchangeCodec;

public final class Exchange {

    final Transmitter transmitter;
    final Call call;
    final ExchangeFinder finder;
    final ExchangeCodec codec;

    public Exchange(Transmitter transmitter, Call call,
                    ExchangeFinder finder, ExchangeCodec codec) {
        this.transmitter = transmitter;
        this.call = call;
        this.finder = finder;
        this.codec = codec;
    }
}
