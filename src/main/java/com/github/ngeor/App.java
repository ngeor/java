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
            validateDirectoryExists()
                    .andThen(this::validatePomXmlExists)
                    .andThen(this::validateGitDirectoryExists)
                    .andThen(this::validatePendingGitChanges)
                    .andThen(this::validateSingleRemote)
                    .flatMap(this::getDefaultBranch)
                    .flatMap(defaultBranch -> getCurrentBranch().flatMap(currentBranch -> {
                        if (!defaultBranch.equals(currentBranch)) {
                            return switchBranch(defaultBranch);
                        }

                        return Result.ok("");
                    }))
                    .get();
        } catch (ProcessFailException ex) {
            System.err.println(ex.getMessage());
            return ex.getCode();
        }

        System.out.println("Hello World! dryRun was " + dryRun);
        System.out.println("Directory is " + directory);
        return 0;
    }

    private Result<String, ProcessFailException> validateDirectoryExists() {
        if (directory.toFile().isDirectory()) {
            return Result.ok("");
        }
        return Result.err(new ProcessFailException(1, "Directory " + directory + " does not exist"));
    }

    private Result<String, ProcessFailException> validatePomXmlExists() {
        if (directory.resolve("pom.xml").toFile().isFile()) {
            return Result.ok("");
        }
        return Result.err(new ProcessFailException(2, "Directory " + directory + " does not contain a pom.xml file"));
    }

    private Result<String, ProcessFailException> validateGitDirectoryExists() {
        if (directory.resolve(".git").toFile().isDirectory()) {
            return Result.ok("");
        }
        return Result.err(new ProcessFailException(3, "Directory " + directory + " does not contain a .git directory"));
    }

    private Result<String, ProcessFailException> validatePendingGitChanges() {
        return git.run("status", "--porcelain")
                .mapErr(e -> new ProcessFailException(4, "Could not check git status: " + e.getMessage()))
                .flatMap(output -> output.isEmpty()
                        ? Result.ok(output)
                        : Result.err(new ProcessFailException(
                                4, "Directory " + directory + " contains pending git changes")));
    }

    private Result<String, ProcessFailException> validateSingleRemote() {
        return git.run("remote")
                .mapErr(e -> new ProcessFailException(5, "Could not check git remotes: " + e.getMessage()))
                .flatMap(output -> output.lines().count() == 1
                        ? Result.ok(output)
                        : Result.err(new ProcessFailException(
                                5, "Directory " + directory + " does not have exactly one git remote")));
    }

    private Result<String, ProcessFailException> getDefaultBranch(String remote) {
        String prefix = "refs/remotes/" + remote + "/";
        return git.run("symbolic-ref", prefix + "HEAD")
                .mapErr(e -> new ProcessFailException(6, "Could not get default branch: " + e.getMessage()))
                .flatMap(fullName -> {
                    if (fullName.startsWith(prefix)) {
                        return Result.ok(fullName.substring(prefix.length()));
                    } else {
                        return Result.err(new ProcessFailException(6, "Invalid default branch: " + fullName));
                    }
                });
    }

    private Result<String, ProcessFailException> getCurrentBranch() {
        return git.getCurrentBranch()
                .mapErr(e -> new ProcessFailException(7, "Could not get current branch: " + e.getMessage()));
    }

    private Result<String, ProcessFailException> switchBranch(String branch) {
        return git.run("checkout", branch)
                .mapErr(e ->
                        new ProcessFailException(8, "Could not switch to branch " + branch + ": " + e.getMessage()));
    }
}
