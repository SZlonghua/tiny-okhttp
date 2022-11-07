package com.tiny.okhttp;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.internal.Util;
import okio.BufferedSource;

import java.io.IOException;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class ResponseBody {

    public ResponseBody() {
    }

    public abstract @Nullable MediaType contentType();

    public abstract long contentLength();

    public final String string() throws IOException {
        try (BufferedSource source = source()) {
            Charset charset = Util.bomAwareCharset(source, charset());
            return source.readString(charset);
        }
    }

    private Charset charset() {
        MediaType contentType = contentType();
        return contentType != null ? contentType.charset(UTF_8) : UTF_8;
    }

    public void close() {
        Util.closeQuietly(source());
    }

    public abstract BufferedSource source();
}
