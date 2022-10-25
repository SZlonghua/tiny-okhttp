package com.tiny.okhttp;

public final class HttpUrl {

    private String url;

    public HttpUrl(String url) {
        this.url = url;
    }

    public static HttpUrl get(String url) {
        return new HttpUrl(url);
    }


}
