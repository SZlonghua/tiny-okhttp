package com.tiny.okhttp;

import java.io.IOException;

public abstract class ResponseBody {

    public ResponseBody() {
    }

    public final String string() throws IOException {
        return "ResponseBody.string";
    }
}
