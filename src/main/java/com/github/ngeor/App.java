package com.github.ngeor;

import io.vavr.control.Try;

import java.io.UncheckedIOException;
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

    private Git git;

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
        git = new Git(directory.toFile());

        try {
            Try.run(this::validateDirectoryExists)
                    .andThen(this::validatePomXmlExists)
                    .andThen(this::validateGitDirectoryExists)
                    .flatMap(ignored -> validatePendingGitChanges())
                    .flatMap(ignored -> validateSingleRemote())
                    .flatMap(this::ensureOnDefaultBranch)
                    .get();
        } catch (AppException ex) {
            System.err.println(ex.getMessage());
            return ex.getCode();
        }

        System.out.println("Hello World! dryRun was " + dryRun);
        System.out.println("Directory is " + directory);
        return 0;
    }

    private void validateDirectoryExists() {
        if (directory.toFile().isDirectory()) {
            return;
        }
        throw new AppException(1, "Directory " + directory + " does not exist");
    }

    private void validatePomXmlExists() {
        if (directory.resolve("pom.xml").toFile().isFile()) {
            return;
        }
        throw new AppException(2, "Directory " + directory + " does not contain a pom.xml file");
    }

    private void validateGitDirectoryExists() {
        if (directory.resolve(".git").toFile().isDirectory()) {
            return;
        }
        throw new AppException(3, "Directory " + directory + " does not contain a .git directory");
    }

    private Try<String> validatePendingGitChanges() {
        return wrapFailure(Try.of(() -> git.run("status", "--porcelain")), 4, "Could not check git status")
                .flatMap(output -> output.isEmpty()
                        ? Try.success(output)
                        : Try.failure(new AppException(
                                4, "Directory " + directory + " contains pending git changes")));
    }

    private Try<String> validateSingleRemote() {
        return wrapFailure(Try.of(() -> git.run("remote")), 5, "Could not check git remotes")
                .flatMap(output -> output.lines().count() == 1
                        ? Try.success(output)
                        : Try.failure(new AppException(
                                5, "Directory " + directory + " does not have exactly one git remote")));
    }

    private Try<Void> ensureOnDefaultBranch(String remote) {
        return getDefaultBranch(remote)
                .flatMap(defaultBranch -> getCurrentBranch().flatMap(currentBranch -> {
                    if (!defaultBranch.equals(currentBranch)) {
                        return switchBranch(defaultBranch);
                    }

                    return Try.success(null);
                }));
    }

    private Try<String> getDefaultBranch(String remote) {
        String prefix = "refs/remotes/" + remote + "/";
        return wrapFailure(Try.of(() -> git.run("symbolic-ref", prefix + "HEAD")), 6, "Could not get default branch")
                .flatMap(fullName -> {
                    if (fullName.startsWith(prefix)) {
                        return Try.success(fullName.substring(prefix.length()));
                    } else {
                        return Try.failure(new AppException(6, "Invalid default branch: " + fullName));
                    }
                });
    }

    private Try<String> getCurrentBranch() {
        return wrapFailure(Try.of(git::getCurrentBranch), 7, "Could not get current branch");
    }

    private Try<Void> switchBranch(String branch) {
        return wrapFailure(Try.run(() -> git.switchToBranch(branch)), 8, "Could not switch to branch " + branch);
    }

    private <T> Try<T> wrapFailure(Try<T> t, int code, String message) {
        if (t.isFailure()) {
            Throwable cause = t.getCause();
            if (cause instanceof UncheckedIOException || cause instanceof ProcessFailException) {
                return Try.failure(new AppException(code, message + ": " + cause.getMessage()));
            }
        }

        // leave success and InterruptedException alone
        return t;
    }
}
