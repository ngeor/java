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
import java.util.function.UnaryOperator;

public class ProcessHelper {
    private final List<String> command;
    private final File directory;

    public ProcessHelper(File directory, String... command) {
        this.command = List.of(command);
        this.directory = Objects.requireNonNull(directory);
    }

    public String run(String... args) throws InterruptedException {
        return runInternal(
                UnaryOperator.identity(),
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

    public void runNoOutput(UnaryOperator<ProcessBuilder> customizer, String... args) throws InterruptedException {
        runInternal(customizer, ignored -> null, args);
    }

    public void runNoOutput(String... args) throws InterruptedException {
        runNoOutput(UnaryOperator.identity(), args);
    }

    public ProcessBuilder createProcessBuilder(String... args) {
        List<String> command = new ArrayList<>(this.command.size() + args.length);
        command.addAll(this.command);
        command.addAll(List.of(args));
        return new ProcessBuilder().command(command).directory(directory);
    }

    private <T> T runInternal(UnaryOperator<ProcessBuilder> customizer, Function<Process, T> onSuccess, String... args)
            throws InterruptedException {
        ProcessBuilder processBuilder = customizer.apply(createProcessBuilder(args));
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
