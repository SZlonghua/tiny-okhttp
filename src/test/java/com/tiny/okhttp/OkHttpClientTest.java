package com.tiny.okhttp;

import junit.framework.TestCase;

import java.io.IOException;

public class OkHttpClientTest extends TestCase {

    public String get(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public void testGet() throws IOException {
        String result = get("http://localhost:8080/api/hello");
        System.out.println(result);
    }

}