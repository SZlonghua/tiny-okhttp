package com.tiny.okhttp.internal;

import okio.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.*;

public final class Util {

    private static final Options UNICODE_BOMS = Options.of(
            ByteString.decodeHex("efbbbf"),   // UTF-8
            ByteString.decodeHex("feff"),     // UTF-16BE
            ByteString.decodeHex("fffe"),     // UTF-16LE
            ByteString.decodeHex("0000ffff"), // UTF-32BE
            ByteString.decodeHex("ffff0000")  // UTF-32LE
    );

    private static final Charset UTF_32BE = Charset.forName("UTF-32BE");
    private static final Charset UTF_32LE = Charset.forName("UTF-32LE");

    private Util() {
    }

    public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException(name + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (AssertionError e) {
                throw e;
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
        try {
            return skipAll(source, timeout, timeUnit);
        } catch (IOException e) {
            return false;
        }
    }
    public static boolean skipAll(Source source, int duration, TimeUnit timeUnit) throws IOException {
        long now = System.nanoTime();
        long originalDuration = source.timeout().hasDeadline()
                ? source.timeout().deadlineNanoTime() - now
                : Long.MAX_VALUE;
        source.timeout().deadlineNanoTime(now + Math.min(originalDuration, timeUnit.toNanos(duration)));
        try {
            Buffer skipBuffer = new Buffer();
            while (source.read(skipBuffer, 8192) != -1) {
                skipBuffer.clear();
            }
            return true; // Success! The source has been exhausted.
        } catch (InterruptedIOException e) {
            return false; // We ran out of time before exhausting the source.
        } finally {
            if (originalDuration == Long.MAX_VALUE) {
                source.timeout().clearDeadline();
            } else {
                source.timeout().deadlineNanoTime(now + originalDuration);
            }
        }
    }


    public static Charset bomAwareCharset(BufferedSource source, Charset charset) throws IOException {
        switch (source.select(UNICODE_BOMS)) {
            case 0: return UTF_8;
            case 1: return UTF_16BE;
            case 2: return UTF_16LE;
            case 3: return UTF_32BE;
            case 4: return UTF_32LE;
            case -1: return charset;
            default: throw new AssertionError();
        }
    }

}
