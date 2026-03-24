package com.github.ngeor;

public final class Validate {
    private Validate() {}

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
