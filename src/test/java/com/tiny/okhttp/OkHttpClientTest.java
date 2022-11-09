package com.tiny.okhttp;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    public CompletableFuture<String> postAsync(String url, String json) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        CompletableFuture<String> result = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();
                result.complete(r);
            }
        });
        return result;
    }

    public void testGet() throws IOException {
        String result = get("http://localhost:8080/api/hello");
        System.out.println(result);
    }
    public void testPost() throws IOException {
        String result = post("http://localhost:8080/api/hello","{\"key\":\"liao\"}");
        System.out.println(result);
    }

    public void testPostAsync() throws IOException, ExecutionException, InterruptedException {
        CompletableFuture<String> result = postAsync("http://localhost:8080/api/hello","{\"key\":\"liao\"}");
        System.out.println(result.get());
    }

}