package com.github.ngeor;

import io.vavr.control.Try;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessHelper {
    private final String command;
    private final File directory;

    public ProcessHelper(String command, File directory) {
        this.command = Objects.requireNonNull(command);
        this.directory = Objects.requireNonNull(directory);
    }

    public Try<String> run(String... args) {
        List<String> command = new ArrayList<>(1 + args.length);
        command.add(this.command);
        command.addAll(List.of(args));

        ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(directory);
        return Try.of(processBuilder::start).flatMap(this::waitForProcess).flatMap(process -> {
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return readerToString(process.errorReader())
                        .flatMap(stdErr -> Try.failure(new ProcessFailException(exitCode, stdErr)));
            }

            return readerToString(process.inputReader());
        });
    }

    private Try<Process> waitForProcess(Process process) {
        return Try.of(() -> {
            process.waitFor();
            return process;
        });
    }

    private static Try<String> readerToString(BufferedReader reader) {
        return Try.of(() -> {
            try (reader) {
                StringWriter stringWriter = new StringWriter();
                reader.transferTo(stringWriter);
                return stringWriter.toString().strip();
            }
        });
    }
}
