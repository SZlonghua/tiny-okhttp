package com.tiny.okhttp;

import java.io.IOException;

public interface Callback {

    void onFailure(Call call, IOException e);

    void onResponse(Call call, Response response) throws IOException;
}
