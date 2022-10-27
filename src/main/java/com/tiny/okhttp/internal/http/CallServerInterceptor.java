package com.tiny.okhttp.internal.http;

import com.tiny.okhttp.Interceptor;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.ResponseBody;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ProtocolException;

@Slf4j
public final class CallServerInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        log.info("CallServerInterceptor.intercept");
        /*RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Exchange exchange = realChain.exchange();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();

        exchange.writeRequestHeaders(request);


        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {

            BufferedSink bufferedRequestBody = Okio.buffer(
                    exchange.createRequestBody(request, false));
            request.body().writeTo(bufferedRequestBody);
            bufferedRequestBody.close();

        } else {
            exchange.noRequestBody();
        }

        if (request.body() == null) {
            exchange.finishRequest();
        }

        exchange.responseHeadersStart();

        Response.Builder responseBuilder = exchange.readResponseHeaders(false);

        Response response = responseBuilder
                .request(request)
                .handshake(exchange.connection().handshake())
                .sentRequestAtMillis(sentRequestMillis)
                .receivedResponseAtMillis(System.currentTimeMillis())
                .build();

        exchange.responseHeadersEnd(response);

        response = response.newBuilder()
                .body(exchange.openResponseBody(response))
                .build();

        return response;*/
        return new Response(new ResponseBody() {
        });
    }
}
