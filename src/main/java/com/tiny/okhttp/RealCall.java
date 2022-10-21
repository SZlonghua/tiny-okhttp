package com.tiny.okhttp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RealCall implements Call {

    final OkHttpClient client;

    final Request originalRequest;

    private RealCall(OkHttpClient client, Request originalRequest) {
        this.client = client;
        this.originalRequest = originalRequest;
    }
    @Override
    public Response execute() throws IOException {
        log.info("RealCall execute");
        return new Response(new ResponseBody(){});
    }

    static RealCall newRealCall(OkHttpClient client, Request originalRequest) {
        RealCall call = new RealCall(client, originalRequest);
        return call;
    }
}
