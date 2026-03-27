package com.github.ngeor;

import java.util.Optional;
import java.util.stream.Stream;

public enum SemVerBump {
    MAJOR,
    MINOR,
    PATCH;

    public static Optional<SemVerBump> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }

        return Stream.of(SemVerBump.values())
                .filter(b -> b.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
