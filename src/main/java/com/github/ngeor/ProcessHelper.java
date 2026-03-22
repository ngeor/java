package com.github.ngeor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ProcessHelper {
    private final String command;
    private final File directory;

    public ProcessHelper(String command, File directory) {
        this.command = Objects.requireNonNull(command);
        this.directory = Objects.requireNonNull(directory);
    }

    public String run(String... args) throws InterruptedException {
        return runInternal(
                process -> {
                    String stdOut;
                    try {
                        stdOut = readerToString(process.inputReader());
                    } catch (IOException ex) {
                        throw new UncheckedIOException("Could not read output stream", ex);
                    }

                    return stdOut;
                },
                args);
    }

    public void runNoOutput(String... args) throws InterruptedException {
        runInternal(ignored -> null, args);
    }

    private <T> T runInternal(Function<Process, T> onSuccess, String... args) throws InterruptedException {
        List<String> command = new ArrayList<>(1 + args.length);
        command.add(this.command);
        command.addAll(List.of(args));

        ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(directory);
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not start process", ex);
        }

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            return onSuccess.apply(process);
        }

        String stdErr;
        try {
            stdErr = readerToString(process.errorReader());
            if (stdErr.isBlank()) {
                // some processes do not write their errors to stderr but stdout instead
                stdErr = readerToString(process.inputReader());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read error stream", ex);
        }

        throw new ProcessFailException(exitCode, stdErr);
    }

    private static String readerToString(BufferedReader reader) throws IOException {
        try (reader) {
            StringWriter stringWriter = new StringWriter();
            reader.transferTo(stringWriter);
            return stringWriter.toString().strip();
        }
    }
}
