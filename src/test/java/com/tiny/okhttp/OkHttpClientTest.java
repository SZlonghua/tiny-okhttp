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


    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public String post(String url, String json) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public void testGet() throws IOException {
        String result = get("http://localhost:8080/api/hello");
        System.out.println(result);
    }
    public void testPost() throws IOException {
        String result = post("http://localhost:8080/api/hello","{\"key\":\"liao\"}");
        System.out.println(result);
    }

}