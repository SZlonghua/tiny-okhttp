package com.tiny.okhttp;

public class OkHttpClient implements Call.Factory {
    @Override
    public Call newCall(Request request) {
        return RealCall.newRealCall(this, request);
    }
}
