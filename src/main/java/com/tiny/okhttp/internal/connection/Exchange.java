package com.tiny.okhttp.internal.connection;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.Call;
import com.tiny.okhttp.Request;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.ResponseBody;
import com.tiny.okhttp.internal.http.ExchangeCodec;
import com.tiny.okhttp.internal.http.RealResponseBody;
import okio.*;

import java.io.IOException;
import java.net.ProtocolException;

public final class Exchange {

    final Transmitter transmitter;
    final Call call;
    final ExchangeFinder finder;
    final ExchangeCodec codec;

    public Exchange(Transmitter transmitter, Call call,
                    ExchangeFinder finder, ExchangeCodec codec) {
        this.transmitter = transmitter;
        this.call = call;
        this.finder = finder;
        this.codec = codec;
    }

    public void writeRequestHeaders(Request request) throws IOException {
        try {
            codec.writeRequestHeaders(request);
        } catch (IOException e) {
            throw e;
        }
    }

    public Sink createRequestBody(Request request) throws IOException {
        long contentLength = request.body().contentLength();
        Sink rawRequestBody = codec.createRequestBody(request, contentLength);
        return new RequestBodySink(rawRequestBody, contentLength);
    }

    public void finishRequest() throws IOException {
        try {
            codec.finishRequest();
        } catch (IOException e) {
            throw e;
        }
    }

    public void responseHeadersStart() {

    }

    public void responseHeadersEnd(Response response) {

    }

    public ResponseBody openResponseBody(Response response) throws IOException {
        try {
            String contentType = response.header("Content-Type");
            long contentLength = codec.reportedContentLength(response);
            Source rawSource = codec.openResponseBodySource(response);
            ResponseBodySource source = new ResponseBodySource(rawSource, contentLength);
            return new RealResponseBody(contentType, contentLength, Okio.buffer(source));
        } catch (IOException e) {
            throw e;
        }
    }

    public @Nullable Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        try {
            Response.Builder result = codec.readResponseHeaders(expectContinue);
            if (result != null) {
                result.initExchange(this);
            }
            return result;
        } catch (IOException e) {
            throw e;
        }
    }

    @Nullable
    IOException bodyComplete(
            long bytesRead, boolean responseDone, boolean requestDone, @Nullable IOException e) {
        return transmitter.exchangeMessageDone(this, requestDone, responseDone, e);
    }

    public RealConnection connection() {
        return codec.connection();
    }

    public void noRequestBody() {
        transmitter.exchangeMessageDone(this, true, false, null);
    }




    private final class RequestBodySink extends ForwardingSink {
        private boolean completed;
        /** The exact number of bytes to be written, or -1L if that is unknown. */
        private long contentLength;
        private long bytesReceived;
        private boolean closed;

        RequestBodySink(Sink delegate, long contentLength) {
            super(delegate);
            this.contentLength = contentLength;
        }

        @Override public void write(Buffer source, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            if (contentLength != -1L && bytesReceived + byteCount > contentLength) {
                throw new ProtocolException("expected " + contentLength
                        + " bytes but received " + (bytesReceived + byteCount));
            }
            try {
                super.write(source, byteCount);
                this.bytesReceived += byteCount;
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Override public void flush() throws IOException {
            try {
                super.flush();
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Override public void close() throws IOException {
            if (closed) return;
            closed = true;
            if (contentLength != -1L && bytesReceived != contentLength) {
                throw new ProtocolException("unexpected end of stream");
            }
            try {
                super.close();
                complete(null);
            } catch (IOException e) {
                throw complete(e);
            }
        }

        private @Nullable IOException complete(@Nullable IOException e) {
            if (completed) return e;
            completed = true;
            return bodyComplete(bytesReceived, false, true, e);
        }
    }

    final class ResponseBodySource extends ForwardingSource {
        private final long contentLength;
        private long bytesReceived;
        private boolean completed;
        private boolean closed;

        ResponseBodySource(Source delegate, long contentLength) {
            super(delegate);
            this.contentLength = contentLength;

            if (contentLength == 0L) {
                complete(null);
            }
        }

        @Override public long read(Buffer sink, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            try {
                long read = delegate().read(sink, byteCount);
                if (read == -1L) {
                    complete(null);
                    return -1L;
                }

                long newBytesReceived = bytesReceived + read;
                if (contentLength != -1L && newBytesReceived > contentLength) {
                    throw new ProtocolException("expected " + contentLength
                            + " bytes but received " + newBytesReceived);
                }

                bytesReceived = newBytesReceived;
                if (newBytesReceived == contentLength) {
                    complete(null);
                }

                return read;
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Override public void close() throws IOException {
            if (closed) return;
            closed = true;
            try {
                super.close();
                complete(null);
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Nullable IOException complete(@Nullable IOException e) {
            if (completed) return e;
            completed = true;
            return bodyComplete(bytesReceived, true, false, e);
        }
    }
}
