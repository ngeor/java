package com.github.ngeor;

import java.util.Objects;

public record StepDefinition(String name, Step step) {
    public StepDefinition {
        Validate.requireNotBlank(name, "Step name is required");
        Objects.requireNonNull(step, "Step cannot be null");
    }
}
