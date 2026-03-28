package com.github.ngeor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GpgTest {
    @TempDir
    private File gpgHomeDirectory;

    @Test
    void testListKeys() throws InterruptedException {
        assertThat(gpgHomeDirectory).isNotNull().exists().isDirectory().isEmptyDirectory();

        Gpg gpg = new Gpg(gpgHomeDirectory);
        gpg.listKeys();

        File[] files = gpgHomeDirectory.listFiles();
        assertThat(files)
                .hasSize(2)
                .allMatch(File::isFile)
                .extracting(File::getName)
                .containsExactlyInAnyOrder("pubring.kbx", "trustdb.gpg");
    }
}
