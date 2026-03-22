package com.github.ngeor;

import java.nio.file.Path;
import java.util.List;
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

    @Option(names = "--development-version", description = "The next development version", required = true)
    private String developmentVersion;

    @Option(names = "--release-version", description = "The release version", required = true)
    private String releaseVersion;

    private Git git;
    private String remote;
    private String currentBranch;
    private String defaultBranch;
    private Maven maven;

    private App() {}

    public static void main(String[] args) {
        System.exit(executeWithoutExiting(args));
    }

    static int executeWithoutExiting(String[] args) {
        return new CommandLine(new App()).execute(args);
    }

    @Override
    public Integer call() throws InterruptedException {
        // sanity check against Picocli framework and normalize directory
        Objects.requireNonNull(directory, "Directory cannot be null");
        directory = directory.toAbsolutePath().normalize();
        git = new Git(directory.toFile());
        maven = new Maven(directory.toFile());

        List<Step> steps = List.of(
                this::validateDirectoryExists,
                this::validatePomXmlExists,
                this::validateGitDirectoryExists,
                this::validatePendingGitChanges,
                this::validateSingleRemote,
                this::getCurrentBranch,
                this::getDefaultBranch,
                this::ensureOnDefaultBranch,
                git::pull,
                maven::releaseClean,
                this::prepareRelease);

        List<String> stepNames = List.of(
                "Check if directory exists",
                "Check if pom.xml exists",
                "Check if directory is a git working directory",
                "Ensure no pending git changes",
                "Ensure single git remote",
                "Get current git branch",
                "Get default git branch",
                "Ensure on default git branch",
                "Get latest changes from upstream",
                "Clean Maven release",
                "Prepare Maven release");

        int stepNumber = 1;
        try {
            for (Step step : steps) {
                step.run();
                stepNumber++;
            }
        } catch (ProcessFailException ex) {
            System.err.println(stepNames.get(stepNumber - 1) + ": " + ex.getCode() + " - " + ex.getMessage());
            return stepNumber;
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
            return stepNumber;
        }

        System.out.println("Hello World! dryRun was " + dryRun);
        System.out.println("Directory is " + directory);
        return 0;
    }

    @FunctionalInterface
    interface Step {
        void run() throws InterruptedException;
    }

    private void validateDirectoryExists() {
        if (directory.toFile().isDirectory()) {
            return;
        }
        throw new IllegalStateException("Directory " + directory + " does not exist");
    }

    private void validatePomXmlExists() {
        if (directory.resolve("pom.xml").toFile().isFile()) {
            return;
        }
        throw new IllegalStateException("Directory " + directory + " does not contain a pom.xml file");
    }

    private void validateGitDirectoryExists() {
        if (directory.resolve(".git").toFile().isDirectory()) {
            return;
        }
        throw new IllegalStateException("Directory " + directory + " does not contain a .git directory");
    }

    private void validatePendingGitChanges() throws InterruptedException {
        String status = git.statusPorcelain();
        if (!status.isEmpty()) {
            throw new IllegalStateException("Directory " + directory + " contains pending git changes");
        }
    }

    private void validateSingleRemote() throws InterruptedException {
        String output = git.remote();
        if (output.lines().count() != 1) {
            throw new IllegalStateException("Directory " + directory + " does not have exactly one git remote");
        }

        remote = output;
    }

    private void getCurrentBranch() throws InterruptedException {
        currentBranch = git.getCurrentBranch();
    }

    private void getDefaultBranch() throws InterruptedException {
        defaultBranch = git.getDefaultBranch(remote);
    }

    private void ensureOnDefaultBranch() throws InterruptedException {
        if (!defaultBranch.equals(currentBranch)) {
            git.switchToBranch(defaultBranch);
        }
    }

    private void prepareRelease() throws InterruptedException {
        String tag = "v" + releaseVersion;
        maven.releasePrepare(developmentVersion, releaseVersion, tag);
    }
}
