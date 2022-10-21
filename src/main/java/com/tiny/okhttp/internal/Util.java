package com.tiny.okhttp.internal;

public final class Util {

    private Util() {
    }

    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
}
