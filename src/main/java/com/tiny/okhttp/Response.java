package com.tiny.okhttp;

import com.sun.istack.internal.Nullable;

import java.io.Closeable;
import java.io.IOException;

public final class Response implements Closeable {

    @Nullable
    ResponseBody body;

    public Response(ResponseBody body) {
        this.body = body;
    }

    public @Nullable
    ResponseBody body() {
        return body;
    }

    @Override
    public void close() throws IOException {

    }
}
