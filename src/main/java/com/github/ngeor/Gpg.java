package com.github.ngeor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class Gpg {
    private final ProcessHelper processHelper;

    public Gpg(File directory) {
        this.processHelper = new ProcessHelper("gpg", directory);
    }

    public void listKeys() throws InterruptedException {
        processHelper.runNoOutput("--list-keys");
    }

    public void importKey(String passphrase, File gpgKeyFile) throws InterruptedException {
        ProcessBuilder pb1 = processHelper.createProcessBuilder(
                "--batch", "--yes", "--passphrase=" + passphrase, "--output", "-", gpgKeyFile.toString());
        ProcessBuilder pb2 = processHelper.createProcessBuilder("--batch", "--import", "--quiet");
        List<Process> pipeline;
        try {
            pipeline = ProcessBuilder.startPipeline(List.of(pb1, pb2));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        for (Process process : pipeline) {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ProcessFailException(exitCode, "Could not import GPG key");
            }
        }
    }
}
