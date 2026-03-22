package com.github.ngeor;

import static io.vavr.API.$;
import static io.vavr.API.Case;

import io.vavr.control.Try;
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
        } catch (ProcessFailException ex) {
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
        throw new ProcessFailException(1, "Directory " + directory + " does not exist");
    }

    private void validatePomXmlExists() {
        if (directory.resolve("pom.xml").toFile().isFile()) {
            return;
        }
        throw new ProcessFailException(2, "Directory " + directory + " does not contain a pom.xml file");
    }

    private void validateGitDirectoryExists() {
        if (directory.resolve(".git").toFile().isDirectory()) {
            return;
        }
        throw new ProcessFailException(3, "Directory " + directory + " does not contain a .git directory");
    }

    private Try<String> validatePendingGitChanges() {
        return Try.of(() -> git.run("status", "--porcelain"))
                .mapFailure(
                        Case($(), e -> new ProcessFailException(4, "Could not check git status: " + e.getMessage())))
                .flatMap(output -> output.isEmpty()
                        ? Try.success(output)
                        : Try.failure(new ProcessFailException(
                                4, "Directory " + directory + " contains pending git changes")));
    }

    private Try<String> validateSingleRemote() {
        return Try.of(() -> git.run("remote"))
                .mapFailure(
                        Case($(), e -> new ProcessFailException(5, "Could not check git remotes: " + e.getMessage())))
                .flatMap(output -> output.lines().count() == 1
                        ? Try.success(output)
                        : Try.failure(new ProcessFailException(
                                5, "Directory " + directory + " does not have exactly one git remote")));
    }

    private Try<String> ensureOnDefaultBranch(String remote) {
        return getDefaultBranch(remote)
                .flatMap(defaultBranch -> getCurrentBranch().flatMap(currentBranch -> {
                    if (!defaultBranch.equals(currentBranch)) {
                        return switchBranch(defaultBranch);
                    }

                    return Try.success("");
                }));
    }

    private Try<String> getDefaultBranch(String remote) {
        String prefix = "refs/remotes/" + remote + "/";
        return Try.of(() -> git.run("symbolic-ref", prefix + "HEAD"))
                .mapFailure(
                        Case($(), e -> new ProcessFailException(6, "Could not get default branch: " + e.getMessage())))
                .flatMap(fullName -> {
                    if (fullName.startsWith(prefix)) {
                        return Try.success(fullName.substring(prefix.length()));
                    } else {
                        return Try.failure(new ProcessFailException(6, "Invalid default branch: " + fullName));
                    }
                });
    }

    private Try<String> getCurrentBranch() {
        return Try.of(git::getCurrentBranch)
                .mapFailure(
                        Case($(), e -> new ProcessFailException(7, "Could not get current branch: " + e.getMessage())));
    }

    private Try<String> switchBranch(String branch) {
        return Try.of(() -> git.run("checkout", branch))
                .mapFailure(Case(
                        $(),
                        e -> new ProcessFailException(
                                8, "Could not switch to branch " + branch + ": " + e.getMessage())));
    }
}
