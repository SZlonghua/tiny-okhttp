package com.tiny.okhttp;


public final class Request {

    public static class Builder {
        public Builder url(String url) {
            return this;
        }
        public Request build() {
            return new Request();
        }
    }
}
