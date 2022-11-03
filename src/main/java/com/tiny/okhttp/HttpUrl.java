package com.tiny.okhttp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpUrl {

    private String url;

    final String host;
    final int port;
    final String path;

    private static final Pattern urlPattern =
            Pattern.compile("^(http|https)://(?<host>[a-zA-Z0-9.]+):(?<port>\\d+)(?<path>[a-zA-Z/]*)");

    public HttpUrl(String url) {
        Matcher matcher = urlPattern.matcher(url);
        if(!matcher.matches()){
            throw new IllegalArgumentException("url is incorrect");
        }
        this.url = url;
        this.host = matcher.group("host");
        this.port = Integer.parseInt(matcher.group("port"));
        this.path = matcher.group("path");
    }

    public static HttpUrl get(String url) {
        return new HttpUrl(url);
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

}
