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

    @Option(
            names = "--tag",
            description = "Override the git tag for the release (defaults to v plus the release version")
    private String tag;

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

        if (tag == null || tag.isBlank()) {
            tag = "v" + releaseVersion;
        }

        List<StepDefinition> steps = List.of(
                new StepDefinition("Check if directory exists", this::validateDirectoryExists),
                new StepDefinition("Check if pom.xml exists", this::validatePomXmlExists),
                new StepDefinition("Check if directory is a git working directory", this::validateGitDirectoryExists),
                new StepDefinition("Ensure no pending git changes", this::validatePendingGitChanges),
                new StepDefinition("Ensure single git remote", this::validateSingleRemote),
                new StepDefinition("Get current git branch", this::getCurrentBranch),
                new StepDefinition("Get default git branch", this::getDefaultBranch),
                new StepDefinition("Ensure git tag does not exist", this::ensureGitTagDoesNotExist),
                new StepDefinition("Ensure on default git branch", this::ensureOnDefaultBranch),
                new StepDefinition("Get latest changes from upstream", git::pull),
                new StepDefinition("Clean Maven release", maven::releaseClean),
                new StepDefinition("Prepare Maven release", this::prepareRelease),
                new StepDefinition("Run git-cliff", this::runGitCliff),
                new StepDefinition("Push upstream", git::pushFollowTags),
                new StepDefinition("Final Maven release:clean", maven::releaseClean));

        int stepNumber = 0;
        for (StepDefinition stepDefinition : steps) {
            try {
                stepNumber++;
                stepDefinition.step().run();
            } catch (ProcessFailException ex) {
                System.err.printf(
                        "[%s] Child process exited with code %d : %s%n",
                        stepDefinition.name(), ex.getCode(), ex.getMessage());
                return stepNumber;
            } catch (RuntimeException ex) {
                System.err.printf("[%s] %s%n", stepDefinition.name(), ex.getMessage());
                return stepNumber;
            }
        }

        return 0;
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

    private void ensureGitTagDoesNotExist() throws InterruptedException {
        if (git.tag().lines().anyMatch(tag::equals)) {
            throw new IllegalStateException("Git tag " + tag + " already exists");
        }
    }

    private void ensureOnDefaultBranch() throws InterruptedException {
        if (!defaultBranch.equals(currentBranch)) {
            git.switchToBranch(defaultBranch);
        }
    }

    private void prepareRelease() throws InterruptedException {
        maven.releasePrepare(developmentVersion, releaseVersion, tag);
    }

    private void runGitCliff() throws InterruptedException {
        ProcessHelper gitCliff = new ProcessHelper("git-cliff", directory.toFile());
        gitCliff.runNoOutput("-o", "CHANGELOG.md");
        git.add("CHANGELOG.md");
        git.commit("Updated changelog");
    }
}
