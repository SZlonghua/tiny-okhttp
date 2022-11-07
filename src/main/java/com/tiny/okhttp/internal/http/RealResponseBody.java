package com.tiny.okhttp.internal.http;

import com.sun.istack.internal.Nullable;
import com.tiny.okhttp.MediaType;
import com.tiny.okhttp.ResponseBody;
import okio.BufferedSource;

public final class RealResponseBody extends ResponseBody {
    /**
     * Use a string to avoid parsing the content type until needed. This also defers problems caused
     * by malformed content types.
     */
    private final @Nullable
    String contentTypeString;
    private final long contentLength;
    private final BufferedSource source;

    public RealResponseBody(
            @Nullable String contentTypeString, long contentLength, BufferedSource source) {
        this.contentTypeString = contentTypeString;
        this.contentLength = contentLength;
        this.source = source;
    }

    @Override public MediaType contentType() {
        return contentTypeString != null ? MediaType.parse(contentTypeString) : null;
    }

    @Override public long contentLength() {
        return contentLength;
    }

    @Override public BufferedSource source() {
        return source;
    }
}
