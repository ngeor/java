package com.github.ngeor;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
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

    private ProcessHelper git;

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
        git = new ProcessHelper("git", directory.toFile());

        Err err = validate().orElse(null);
        if (err != null) {
            System.err.println(err.message());
            return err.code();
        }

        System.out.println("Hello World! dryRun was " + dryRun);
        System.out.println("Directory is " + directory);
        return 0;
    }

    private Optional<Err> validate() {
        return validateDirectoryExists()
                .or(this::validatePomXmlExists)
                .or(this::validateGitDirectoryExists)
                .or(this::validatePendingGitChanges)
                .or(this::validateSingleRemote);
    }

    private Optional<Err> validateDirectoryExists() {
        if (directory.toFile().isDirectory()) {
            return Optional.empty();
        }
        return Optional.of(new Err(1, "Directory " + directory + " does not exist"));
    }

    private Optional<Err> validatePomXmlExists() {
        if (directory.resolve("pom.xml").toFile().isFile()) {
            return Optional.empty();
        }
        return Optional.of(new Err(2, "Directory " + directory + " does not contain a pom.xml file"));
    }

    private Optional<Err> validateGitDirectoryExists() {
        if (directory.resolve(".git").toFile().isDirectory()) {
            return Optional.empty();
        }
        return Optional.of(new Err(3, "Directory " + directory + " does not contain a .git directory"));
    }

    private Optional<Err> validatePendingGitChanges() {
        try {
            if (hasPendingGitChanges()) {
                return Optional.of(new Err(4, "Directory " + directory + " contains pending git changes"));
            }
            return Optional.empty();
        } catch (ProcessFailException ex) {
            return Optional.of(new Err(5, "Could not check git status: " + ex.getMessage()));
        }
    }

    private Optional<Err> validateSingleRemote() {
        try {
            if (hasSingleRemote()) {
                return Optional.empty();
            }
            return Optional.of(new Err(6, "Directory " + directory + " does not have exactly one git remote"));
        } catch (ProcessFailException ex) {
            return Optional.of(new Err(7, "Could not check git remotes: " + ex.getMessage()));
        }
    }

    private boolean hasPendingGitChanges() {
        return !git.run("status", "--porcelain").isEmpty();
    }

    private boolean hasSingleRemote() {
        String output = git.run("remote");
        return output.lines().count() == 1;
    }
}
