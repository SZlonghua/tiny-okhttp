package com.tiny.okhttp;

import com.sun.istack.internal.Nullable;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MediaType {

    private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
    private static final String QUOTED = "\"([^\"]*)\"";
    private static final Pattern TYPE_SUBTYPE = Pattern.compile(TOKEN + "/" + TOKEN);
    private static final Pattern PARAMETER = Pattern.compile(
            ";\\s*(?:" + TOKEN + "=(?:" + TOKEN + "|" + QUOTED + "))?");

    private final String mediaType;
    private final String type;
    private final String subtype;
    private final @Nullable String charset;

    private MediaType(String mediaType, String type, String subtype, @Nullable String charset) {
        this.mediaType = mediaType;
        this.type = type;
        this.subtype = subtype;
        this.charset = charset;
    }

    public static @Nullable MediaType parse(String string) {
        try {
            return get(string);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static MediaType get(String string) {
        Matcher typeSubtype = TYPE_SUBTYPE.matcher(string);
        if (!typeSubtype.lookingAt()) {
            throw new IllegalArgumentException("No subtype found for: \"" + string + '"');
        }
        String type = typeSubtype.group(1).toLowerCase(Locale.US);
        String subtype = typeSubtype.group(2).toLowerCase(Locale.US);

        String charset = null;
        Matcher parameter = PARAMETER.matcher(string);
        for (int s = typeSubtype.end(); s < string.length(); s = parameter.end()) {
            parameter.region(s, string.length());
            if (!parameter.lookingAt()) {
                throw new IllegalArgumentException("Parameter is not formatted correctly: \""
                        + string.substring(s)
                        + "\" for: \""
                        + string
                        + '"');
            }

            String name = parameter.group(1);
            if (name == null || !name.equalsIgnoreCase("charset")) continue;
            String charsetParameter;
            String token = parameter.group(2);
            if (token != null) {
                // If the token is 'single-quoted' it's invalid! But we're lenient and strip the quotes.
                charsetParameter = (token.startsWith("'") && token.endsWith("'") && token.length() > 2)
                        ? token.substring(1, token.length() - 1)
                        : token;
            } else {
                // Value is "double-quoted". That's valid and our regex group already strips the quotes.
                charsetParameter = parameter.group(3);
            }
            if (charset != null && !charsetParameter.equalsIgnoreCase(charset)) {
                throw new IllegalArgumentException("Multiple charsets defined: \""
                        + charset
                        + "\" and: \""
                        + charsetParameter
                        + "\" for: \""
                        + string
                        + '"');
            }
            charset = charsetParameter;
        }

        return new MediaType(string, type, subtype, charset);
    }

    public @Nullable
    Charset charset() {
        return charset(null);
    }

    public @Nullable Charset charset(@Nullable Charset defaultValue) {
        try {
            return charset != null ? Charset.forName(charset) : defaultValue;
        } catch (IllegalArgumentException e) {
            return defaultValue; // This charset is invalid or unsupported. Give up.
        }
    }
}
