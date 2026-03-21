package com.github.ngeor;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A CLI that releases a Java library.
 */
@Command(name = "app", mixinStandardHelpOptions = true, version = "1.0", description = "Releases a Java library")
public final class App implements Callable<Integer> {
    @Option(names = "--dry-run", description = "Dry run, do not perform any changes")
    private boolean dryRun;

    @Option(names = "--directory", description = "The working directory", defaultValue = ".")
    private Path directory;

    private App() {}

    public static void main(String[] args) {
        System.exit(executeWithoutExiting(args));
    }

    static int executeWithoutExiting(String[] args) {
        return new CommandLine(new App()).execute(args);
    }

    @Override
    public Integer call() {
        // sanity check against Picocli framework and normalize directory
        Objects.requireNonNull(directory, "Directory cannot be null");
        directory = directory.toAbsolutePath().normalize();

        // ensure directory exists
        if (!directory.toFile().isDirectory()) {
            System.err.println("Directory " + directory + " does not exist");
            return 1;
        }

        // ensure directory contains pom.xml
        if (!directory.resolve("pom.xml").toFile().isFile()) {
            System.err.println("Directory " + directory + " does not contain a pom.xml file");
            return 2;
        }

        // ensure directory contains .git
        if (!directory.resolve(".git").toFile().isDirectory()) {
            System.err.println("Directory " + directory + " does not contain a .git directory");
            return 3;
        }

        System.out.println("Hello World! dryRun was " + dryRun);
        System.out.println("Directory is " + directory);
        return 0;
    }
}
