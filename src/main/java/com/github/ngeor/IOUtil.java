package com.github.ngeor;

import java.io.*;
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

    public static void deleteRecursively(File path) {
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                }
            }
        }
        System.out.printf("Deleting %s%n", path);
        path.delete();
    }
}
