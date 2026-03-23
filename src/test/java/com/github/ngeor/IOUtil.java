package com.github.ngeor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class IOUtil {
    private IOUtil() {}

    public static String readResource(String path) {
        try {
            try (InputStream inputStream = IOUtil.class.getResourceAsStream(path)) {
                if (inputStream == null) {
                    throw new IllegalArgumentException("Resource not found: " + path);
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                inputStream.transferTo(byteArrayOutputStream);
                return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
