package com.tiny.okhttp;

import java.io.IOException;

public interface Call {

    Response execute() throws IOException;

    void enqueue(Callback responseCallback);

    interface Factory {
        Call newCall(Request request);
    }
}
