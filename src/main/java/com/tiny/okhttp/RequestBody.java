package com.tiny.okhttp;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.internal.Util;

import java.io.IOException;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class RequestBody {
    public abstract @Nullable MediaType contentType();
    public long contentLength() throws IOException {
        return -1;
    }
//    public abstract void writeTo(BufferedSink sink) throws IOException;

    public static RequestBody create(@Nullable MediaType contentType, String content) {
        Charset charset = UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = UTF_8;
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    public static RequestBody create(final @Nullable MediaType contentType, final byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    public static RequestBody create(final @Nullable MediaType contentType, final byte[] content,
                                     final int offset, final int byteCount) {
        if (content == null) throw new NullPointerException("content == null");
        Util.checkOffsetAndCount(content.length, offset, byteCount);
        return new RequestBody() {
            @Override public @Nullable MediaType contentType() {
                return contentType;
            }

            @Override public long contentLength() {
                return byteCount;
            }

            /*@Override public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }*/
        };
    }
}
