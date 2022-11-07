package com.tiny.okhttp;

import com.sun.istack.internal.Nullable;

import java.net.Socket;

public interface Connection {
    Route route();

    Socket socket();

    @Nullable
    Handshake handshake();
}
