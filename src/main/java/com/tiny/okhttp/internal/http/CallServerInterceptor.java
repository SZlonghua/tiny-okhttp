package com.tiny.okhttp.internal.http;

import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.ResponseBody;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public final class CallServerInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        log.info("CallServerInterceptor.intercept");
        return new Response(new ResponseBody() {

        });
    }
}
