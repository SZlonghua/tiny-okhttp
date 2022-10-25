package com.tiny.okhttp;


import com.sun.istack.internal.Nullable;

public final class Request {

    public static class Builder {
        @Nullable HttpUrl url;
        String method;
//        Headers.Builder headers;
        @Nullable RequestBody body;

        public Builder url(String url) {
            if (url == null) throw new NullPointerException("url == null");
            this.url = HttpUrl.get(url);
            return this;
        }

        public Request build() {
            return new Request();
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
    }
}
