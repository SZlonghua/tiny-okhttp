package com.tiny.okhttp.internal.http;

import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.Request;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.ResponseBody;
import com.tiny.okhttp.internal.connection.Exchange;
import lombok.extern.slf4j.Slf4j;
import okio.BufferedSink;
import okio.Okio;

import java.io.IOException;
import java.net.ProtocolException;

@Slf4j
public final class CallServerInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        log.info("CallServerInterceptor.intercept");
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Exchange exchange = realChain.exchange();
        Request request = realChain.request();

        // source at the BridgeInterceptor add headers
        Request.Builder requestBuilder = request.newBuilder();
        requestBuilder.header("Host", request.url().host()+":"+request.url().port());
        requestBuilder.header("Connection", "Keep-Alive");
        requestBuilder.header("Accept-Encoding", "gzip");
        requestBuilder.header("User-Agent", "okhttp/3.14.9");
        
        request = requestBuilder.build();

        long sentRequestMillis = System.currentTimeMillis();

        exchange.writeRequestHeaders(request);

        if (permitsRequestBody(request.method()) && request.body() != null) {

            BufferedSink bufferedRequestBody = Okio.buffer(
                    exchange.createRequestBody(request));
            request.body().writeTo(bufferedRequestBody);
            bufferedRequestBody.close();

        } else {
            exchange.noRequestBody();
        }

        exchange.finishRequest();

        exchange.responseHeadersStart();

        Response.Builder responseBuilder = exchange.readResponseHeaders(false);

        Response response = responseBuilder
                .request(request)
//                .handshake(exchange.connection().handshake()) // https ssl/tls
                .sentRequestAtMillis(sentRequestMillis)
                .receivedResponseAtMillis(System.currentTimeMillis())
                .build();

        exchange.responseHeadersEnd(response);

        response = response.newBuilder()
                .body(exchange.openResponseBody(response))
                .build();

        return response;
//        return new Response(new ResponseBody() {
//        });
    }

    public static boolean permitsRequestBody(String method) {
        return !(method.equals("GET") || method.equals("HEAD"));
    }
}
