package com.tiny.okhttp;

import java.io.IOException;

public enum Protocol {
    HTTP_1_0("http/1.0"),

    HTTP_1_1("http/1.1");

    private final String protocol;

    Protocol(String protocol) {
        this.protocol = protocol;
    }

    public static Protocol get(String protocol) throws IOException {
        // Unroll the loop over values() to save an allocation.
        if (protocol.equals(HTTP_1_0.protocol)) return HTTP_1_0;
        if (protocol.equals(HTTP_1_1.protocol)) return HTTP_1_1;
        throw new IOException("Unexpected protocol: " + protocol);
    }
}
