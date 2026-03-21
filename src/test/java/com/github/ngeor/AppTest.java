package com.github.ngeor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/**
 * Integration tests for {@link App}.
 */
class AppTest {
    @TempDir
    private Path tempDir;

    @Test
    void testApp() {
        assertThat(tempDir).isNotNull();
        assertThatNoException().isThrownBy(() -> App.executeWithoutExiting(new String[] {
            "--directory",
            tempDir.toString()
        }));
    }
}
