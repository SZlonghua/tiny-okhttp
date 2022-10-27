package com.tiny.okhttp;


import com.sun.istack.internal.Nullable;

public final class Request {

    final HttpUrl url;
    final String method;
//    final Headers headers;
    final @Nullable RequestBody body;

    Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
//        this.headers = builder.headers.build();
        this.body = builder.body;
    }

    public HttpUrl url() {
        return url;
    }

    public String method() {
        return method;
    }

    public @Nullable RequestBody body() {
        return body;
    }

    public static class Builder {
        @Nullable HttpUrl url;
        String method;
//        Headers.Builder headers;
        @Nullable RequestBody body;

        public Builder() {
            this.method = "GET";
//            this.headers = new Headers.Builder();
        }

        public Builder url(String url) {
            if (url == null) throw new NullPointerException("url == null");
            this.url = HttpUrl.get(url);
            return this;
        }

        public Request build() {
            return new Request(this);
        }
        public Builder post(RequestBody body) {
            return method("POST", body);
        }
        public Builder method(String method, @Nullable RequestBody body) {
            if (method == null) throw new NullPointerException("method == null");
            if (method.length() == 0) throw new IllegalArgumentException("method.length() == 0");
            this.method = method;
            this.body = body;
            return this;
        }

        public String method() {
            return method;
        }
    }
}
