package com.github.ngeor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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

    public String run(String... args) {
        List<String> command = new ArrayList<>(1 + args.length);
        command.add(this.command);
        command.addAll(List.of(args));

        ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(directory);
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            throw new ProcessFailException("Could not start process", ex);
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ProcessFailException("Interrupted while waiting for process", ex);
        }

        if (exitCode != 0) {
            String stdErr;
            try {
                stdErr = readerToString(process.errorReader());
            } catch (IOException ex) {
                throw new ProcessFailException("Could not read process error stream", ex);
            }
            throw new ProcessFailException(exitCode, stdErr);
        }

        String output;
        try {
            output = readerToString(process.inputReader());
        } catch (IOException ex) {
            throw new ProcessFailException("Could not read process output stream", ex);
        }
        return output;
    }

    private static String readerToString(BufferedReader reader) throws IOException {
        try (reader) {
            StringWriter stringWriter = new StringWriter();
            reader.transferTo(stringWriter);
            return stringWriter.toString();
        }
    }
}
