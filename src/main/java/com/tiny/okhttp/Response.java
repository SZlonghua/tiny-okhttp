package com.tiny.okhttp;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.internal.connection.Exchange;

import java.io.Closeable;
import java.io.IOException;

public final class Response implements Closeable {

    final Request request;
    final Protocol protocol;
    final int code;
    final String message;
    final @Nullable Handshake handshake;
    final Headers headers;
    final @Nullable ResponseBody body;
//    final @Nullable Response networkResponse;
//    final @Nullable Response cacheResponse;
//    final @Nullable Response priorResponse;
    final long sentRequestAtMillis;
    final long receivedResponseAtMillis;
    final @Nullable Exchange exchange;



    Response(Builder builder) {
        this.request = builder.request;
        this.protocol = builder.protocol;
        this.code = builder.code;
        this.message = builder.message;
        this.handshake = builder.handshake;
        this.headers = builder.headers.build();
        this.body = builder.body;
//        this.networkResponse = builder.networkResponse;
//        this.cacheResponse = builder.cacheResponse;
//        this.priorResponse = builder.priorResponse;
        this.sentRequestAtMillis = builder.sentRequestAtMillis;
        this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
        this.exchange = builder.exchange;
    }

    public Request request() {
        return request;
    }

    /**
     * Returns the HTTP protocol, such as {@link Protocol#HTTP_1_1} or {@link Protocol#HTTP_1_0}.
     */
    public Protocol protocol() {
        return protocol;
    }

    /** Returns the HTTP status code. */
    public int code() {
        return code;
    }

    /**
     * Returns true if the code is in [200..300), which means the request was successfully received,
     * understood, and accepted.
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /** Returns the HTTP status message. */
    public String message() {
        return message;
    }

    /**
     * Returns the TLS handshake of the connection that carried this response, or null if the response
     * was received without TLS.
     */
    public @Nullable Handshake handshake() {
        return handshake;
    }

//    public List<String> headers(String name) {
//        return headers.values(name);
//    }

    public @Nullable String header(String name) {
        return header(name, null);
    }

    public @Nullable String header(String name, @Nullable String defaultValue) {
        String result = headers.get(name);
        return result != null ? result : defaultValue;
    }

    public Headers headers() {
        return headers;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }


    public @Nullable ResponseBody body() {
        return body;
    }

    @Override
    public void close() throws IOException {
        if (body == null) {
            throw new IllegalStateException("response is not eligible for a body and must not be closed");
        }
        body.close();
    }




    public static class Builder {
        @Nullable Request request;
        @Nullable Protocol protocol;
        int code = -1;
        String message;
        @Nullable Handshake handshake;
        Headers.Builder headers;
        @Nullable ResponseBody body;
//        @Nullable Response networkResponse;
//        @Nullable Response cacheResponse;
//        @Nullable Response priorResponse;
        long sentRequestAtMillis;
        long receivedResponseAtMillis;
        @Nullable
        Exchange exchange;

        public Builder() {
            headers = new Headers.Builder();
        }

        Builder(Response response) {
            this.request = response.request;
            this.protocol = response.protocol;
            this.code = response.code;
            this.message = response.message;
            this.handshake = response.handshake;
            this.headers = response.headers.newBuilder();
            this.body = response.body;
//            this.networkResponse = response.networkResponse;
//            this.cacheResponse = response.cacheResponse;
//            this.priorResponse = response.priorResponse;
            this.sentRequestAtMillis = response.sentRequestAtMillis;
            this.receivedResponseAtMillis = response.receivedResponseAtMillis;
            this.exchange = response.exchange;
        }

        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder handshake(@Nullable Handshake handshake) {
            this.handshake = handshake;
            return this;
        }

        /**
         * Sets the header named {@code name} to {@code value}. If this request already has any headers
         * with that name, they are all replaced.
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
         * headers like "Set-Cookie".
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /** Removes all headers named {@code name} on this builder. */
        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /** Removes all headers on this builder and adds {@code headers}. */
        public Builder headers(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        public Builder body(@Nullable ResponseBody body) {
            this.body = body;
            return this;
        }

       /* public Builder networkResponse(@Nullable Response networkResponse) {
            if (networkResponse != null) checkSupportResponse("networkResponse", networkResponse);
            this.networkResponse = networkResponse;
            return this;
        }

        public Builder cacheResponse(@Nullable Response cacheResponse) {
            if (cacheResponse != null) checkSupportResponse("cacheResponse", cacheResponse);
            this.cacheResponse = cacheResponse;
            return this;
        }

        private void checkSupportResponse(String name, Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException(name + ".body != null");
            } else if (response.networkResponse != null) {
                throw new IllegalArgumentException(name + ".networkResponse != null");
            } else if (response.cacheResponse != null) {
                throw new IllegalArgumentException(name + ".cacheResponse != null");
            } else if (response.priorResponse != null) {
                throw new IllegalArgumentException(name + ".priorResponse != null");
            }
        }

        public Builder priorResponse(@Nullable Response priorResponse) {
            if (priorResponse != null) checkPriorResponse(priorResponse);
            this.priorResponse = priorResponse;
            return this;
        }*/

        private void checkPriorResponse(Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException("priorResponse.body != null");
            }
        }

        public Builder sentRequestAtMillis(long sentRequestAtMillis) {
            this.sentRequestAtMillis = sentRequestAtMillis;
            return this;
        }

        public Builder receivedResponseAtMillis(long receivedResponseAtMillis) {
            this.receivedResponseAtMillis = receivedResponseAtMillis;
            return this;
        }

        public void initExchange(Exchange deferredTrailers) {
            this.exchange = deferredTrailers;
        }

        public Response build() {
            if (request == null) throw new IllegalStateException("request == null");
            if (protocol == null) throw new IllegalStateException("protocol == null");
            if (code < 0) throw new IllegalStateException("code < 0: " + code);
            if (message == null) throw new IllegalStateException("message == null");
            return new Response(this);
        }
    }
}
