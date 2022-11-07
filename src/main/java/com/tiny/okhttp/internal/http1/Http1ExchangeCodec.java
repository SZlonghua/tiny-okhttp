package com.tiny.okhttp.internal.http1;

import com.tiny.okhttp.Headers;
import com.tiny.okhttp.OkHttpClient;
import com.tiny.okhttp.Request;
import com.tiny.okhttp.Response;
import com.tiny.okhttp.internal.Util;
import com.tiny.okhttp.internal.connection.RealConnection;
import com.tiny.okhttp.internal.http.ExchangeCodec;
import com.tiny.okhttp.internal.http.HttpHeaders;
import com.tiny.okhttp.internal.http.RequestLine;
import com.tiny.okhttp.internal.http.StatusLine;
import okio.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;

import static com.tiny.okhttp.internal.http.StatusLine.HTTP_CONTINUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class Http1ExchangeCodec implements ExchangeCodec {

    private static final int STATE_IDLE = 0; // Idle connections are ready to write request headers.
    private static final int STATE_OPEN_REQUEST_BODY = 1;
    private static final int STATE_WRITING_REQUEST_BODY = 2;
    private static final int STATE_READ_RESPONSE_HEADERS = 3;
    private static final int STATE_OPEN_RESPONSE_BODY = 4;
    private static final int STATE_READING_RESPONSE_BODY = 5;
    private static final int STATE_CLOSED = 6;
    private static final int HEADER_LIMIT = 256 * 1024;

    private final OkHttpClient client;

    private final RealConnection realConnection;

    private final BufferedSource source;
    private final BufferedSink sink;
    private int state = STATE_IDLE;
    private long headerLimit = HEADER_LIMIT;

    public Http1ExchangeCodec(OkHttpClient client, RealConnection realConnection,
                              BufferedSource source, BufferedSink sink) {
        this.client = client;
        this.realConnection = realConnection;
        this.source = source;
        this.sink = sink;
    }

    @Override
    public void writeRequestHeaders(Request request) throws IOException {
        String requestLine = RequestLine.get(
                request, realConnection.route().proxy().type());
        writeRequest(request.headers(), requestLine);
    }

    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (state != STATE_IDLE) throw new IllegalStateException("state: " + state);
        sink.writeUtf8(requestLine).writeUtf8("\r\n");
        for (int i = 0, size = headers.size(); i < size; i++) {
            sink.writeUtf8(headers.name(i))
                    .writeUtf8(": ")
                    .writeUtf8(headers.value(i))
                    .writeUtf8("\r\n");
        }
        sink.writeUtf8("\r\n");
        state = STATE_OPEN_REQUEST_BODY;
    }

    @Override
    public Sink createRequestBody(Request request, long contentLength) throws IOException {
        if (contentLength != -1L) {
            // Stream a request body of a known length.
            return newKnownLengthSink();
        }

        throw new IllegalStateException(
                "Cannot stream a request body without chunked encoding or a known content length!");
    }

    @Override
    public void finishRequest() throws IOException {
        sink.flush();
    }

    @Override public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        if (state != STATE_OPEN_REQUEST_BODY && state != STATE_READ_RESPONSE_HEADERS) {
            throw new IllegalStateException("state: " + state);
        }

        try {
            StatusLine statusLine = StatusLine.parse(readHeaderLine());

            Response.Builder responseBuilder = new Response.Builder()
                    .protocol(statusLine.protocol)
                    .code(statusLine.code)
                    .message(statusLine.message)
                    .headers(readHeaders());

            if (expectContinue && statusLine.code == HTTP_CONTINUE) {
                return null;
            } else if (statusLine.code == HTTP_CONTINUE) {
                state = STATE_READ_RESPONSE_HEADERS;
                return responseBuilder;
            }

            state = STATE_OPEN_RESPONSE_BODY;
            return responseBuilder;
        } catch (EOFException e) {
            // Provide more context if the server ends the stream before sending a response.
            String address = "unknown";
            throw new IOException("unexpected end of stream on "
                    + address, e);
        }
    }

    private Headers readHeaders() throws IOException {
        Headers.Builder headers = new Headers.Builder();
        // parse the result headers until the first blank line
        for (String line; (line = readHeaderLine()).length() != 0; ) {
            headers.addLenient(line);
        }
        return headers.build();
    }

    private String readHeaderLine() throws IOException {
        String line = source.readUtf8LineStrict(headerLimit);
        headerLimit -= line.length();
        return line;
    }

    @Override
    public long reportedContentLength(Response response) {
        if (!HttpHeaders.hasBody(response)) {
            return 0L;
        }

        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return -1L;
        }

        return HttpHeaders.contentLength(response);
    }


    @Override
    public Source openResponseBodySource(Response response) {
        if (!HttpHeaders.hasBody(response)) {
            return newFixedLengthSource(0);
        }

        long contentLength = HttpHeaders.contentLength(response);
        if (contentLength != -1) {
            return newFixedLengthSource(contentLength);
        }

        return newUnknownLengthSource();
    }

    private Source newFixedLengthSource(long length) {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_READING_RESPONSE_BODY;
        return new FixedLengthSource(length);
    }

    private Source newUnknownLengthSource() {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_READING_RESPONSE_BODY;
        realConnection.noNewExchanges();
        return new UnknownLengthSource();
    }



    private Sink newKnownLengthSink() {
        if (state != STATE_OPEN_REQUEST_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_WRITING_REQUEST_BODY;
        return new KnownLengthSink();
    }


    @Override public RealConnection connection() {
        return realConnection;
    }

    private abstract class AbstractSource implements Source {
        protected final ForwardingTimeout timeout = new ForwardingTimeout(source.timeout());
        protected boolean closed;

        @Override public Timeout timeout() {
            return timeout;
        }

        @Override public long read(Buffer sink, long byteCount) throws IOException {
            try {
                return source.read(sink, byteCount);
            } catch (IOException e) {
                realConnection.noNewExchanges();
                responseBodyComplete();
                throw e;
            }
        }

        /**
         * Closes the cache entry and makes the socket available for reuse. This should be invoked when
         * the end of the body has been reached.
         */
        final void responseBodyComplete() {
            if (state == STATE_CLOSED) return;
            if (state != STATE_READING_RESPONSE_BODY) throw new IllegalStateException("state: " + state);

            detachTimeout(timeout);

            state = STATE_CLOSED;
        }
    }

    private class FixedLengthSource extends AbstractSource {
        private long bytesRemaining;

        FixedLengthSource(long length) {
            bytesRemaining = length;
            if (bytesRemaining == 0) {
                responseBodyComplete();
            }
        }

        @Override public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (bytesRemaining == 0) return -1;

            long read = super.read(sink, Math.min(bytesRemaining, byteCount));
            if (read == -1) {
                realConnection.noNewExchanges(); // The server didn't supply the promised content length.
                ProtocolException e = new ProtocolException("unexpected end of stream");
                responseBodyComplete();
                throw e;
            }

            bytesRemaining -= read;
            if (bytesRemaining == 0) {
                responseBodyComplete();
            }
            return read;
        }

        @Override public void close() throws IOException {
            if (closed) return;

            if (bytesRemaining != 0 && !Util.discard(this, DISCARD_STREAM_TIMEOUT_MILLIS, MILLISECONDS)) {
                realConnection.noNewExchanges(); // Unread bytes remain on the stream.
                responseBodyComplete();
            }

            closed = true;
        }
    }

    private class UnknownLengthSource extends AbstractSource {
        private boolean inputExhausted;

        @Override public long read(Buffer sink, long byteCount)
                throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (inputExhausted) return -1;

            long read = super.read(sink, byteCount);
            if (read == -1) {
                inputExhausted = true;
                responseBodyComplete();
                return -1;
            }
            return read;
        }

        @Override public void close() throws IOException {
            if (closed) return;
            if (!inputExhausted) {
                responseBodyComplete();
            }
            closed = true;
        }
    }


    private final class KnownLengthSink implements Sink {
        private final ForwardingTimeout timeout = new ForwardingTimeout(sink.timeout());
        private boolean closed;

        @Override public Timeout timeout() {
            return timeout;
        }

        @Override public void write(Buffer source, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            checkOffsetAndCount(source.size(), 0, byteCount);
            sink.write(source, byteCount);
        }

        @Override public void flush() throws IOException {
            if (closed) return; // Don't throw; this stream might have been closed on the caller's behalf.
            sink.flush();
        }

        @Override public void close() throws IOException {
            if (closed) return;
            closed = true;
            detachTimeout(timeout);
            state = STATE_READ_RESPONSE_HEADERS;
        }
    }


    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    private void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }
}
