package com.tiny.okhttp.internal.http;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.Request;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.internal.connection.RealConnection;
import okio.Sink;
import okio.Source;

import java.io.IOException;

public interface ExchangeCodec {

    int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    void writeRequestHeaders(Request request) throws IOException;

    Sink createRequestBody(Request request, long contentLength) throws IOException;

    void finishRequest() throws IOException;

    @Nullable
    Response.Builder readResponseHeaders(boolean expectContinue) throws IOException;

    long reportedContentLength(Response response) throws IOException;

    Source openResponseBodySource(Response response) throws IOException;

    RealConnection connection();
}
