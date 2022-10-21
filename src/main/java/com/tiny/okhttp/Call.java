package com.tiny.okhttp;

import java.io.IOException;

public interface Call {

    Response execute() throws IOException;

    interface Factory {
        Call newCall(Request request);
    }
}
