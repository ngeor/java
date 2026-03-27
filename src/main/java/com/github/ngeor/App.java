package com.github.ngeor;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A CLI that releases a Java library.
 */
@Command(name = "app", mixinStandardHelpOptions = true, version = "1.0", description = "Releases a Java library")
public final class App implements Callable<Integer> {
    @Option(names = "--directory", description = "The working directory", defaultValue = ".")
    private Path directory;

    @Option(
            names = "--development-version",
            description =
                    "The next development version (defaults to the next minor version after the given release version)")
    private String developmentVersion;

    @Parameters(
            description =
                    "The release version. Can be specified as major|minor|patch to bump relatively to the highest semver git tag",
            index = "0")
    private String releaseVersion;

    @Option(
            names = "--tag",
            description = "Override the git tag for the release (defaults to v plus the release version)")
    private String tag;

    private Git git;
    private String remote;
    private String currentBranch;
    private String defaultBranch;
    private Maven maven;
    private SemVer releaseSemVer;
    private final GitTags gitTags = new GitTags();

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

        List<StepDefinition> steps = List.of(
                new StepDefinition("Validate release version", new ValidateReleaseVersion()::validateReleaseVersion),
                new StepDefinition("Validate development version", this::validateDevelopmentVersion),
                new StepDefinition("Check if directory exists", this::validateDirectoryExists),
                new StepDefinition("Check if pom.xml exists", this::validatePomXmlExists),
                new StepDefinition("Check if directory is a git working directory", this::validateGitDirectoryExists),
                new StepDefinition("Ensure no pending git changes", this::validatePendingGitChanges),
                new StepDefinition("Ensure single git remote", this::validateSingleRemote),
                new StepDefinition("Get current git branch", this::getCurrentBranch),
                new StepDefinition("Get default git branch", this::getDefaultBranch),
                new StepDefinition("Validate git tags", this::validateGitTags),
                new StepDefinition("Ensure on default git branch", this::ensureOnDefaultBranch),
                new StepDefinition("Get latest changes from upstream", git::pull),
                new StepDefinition("Clean Maven release", maven::releaseClean),
                new StepDefinition("Run git-cliff", this::runGitCliff),
                new StepDefinition("Prepare Maven release", this::prepareRelease),
                new StepDefinition("Push upstream", git::pushFollowTags),
                new StepDefinition("Final Maven release:clean", maven::releaseClean));

        Pipeline pipeline = new Pipeline(steps, List.of());
        return pipeline.call();
    }

    class ValidateReleaseVersion {

        public void validateReleaseVersion() throws InterruptedException {
            Validate.requireNotBlank(releaseVersion, "Missing required parameter: '<releaseVersion>'");
            if (isRelativeBump()) {
                validateRelativeReleaseVersion();
            } else {
                validateAbsoluteReleaseVersion();
            }
        }

        private boolean isRelativeBump() {
            return SemVerBump.parse(releaseVersion).isPresent();
        }

        private void validateRelativeReleaseVersion() throws InterruptedException {
            SemVer highestVersion = gitTags.highestVersion()
                    .orElseThrow(() -> new IllegalStateException("No existing semver git tags"));
            SemVerBump bump = SemVerBump.parse(releaseVersion)
                    .orElseThrow(() -> new IllegalStateException("Invalid release version: " + releaseVersion));
            releaseSemVer = highestVersion.bump(bump);
        }

        private void validateAbsoluteReleaseVersion() {
            try {
                releaseSemVer = SemVer.parse(releaseVersion);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid release version: " + releaseVersion, ex);
            }
            if (releaseSemVer.suffix() != null) {
                throw new IllegalArgumentException("Invalid release version: " + releaseVersion);
            }
        }
    }

    private void validateDevelopmentVersion() {
        final SemVer developmentSemVer;

        if (developmentVersion == null || developmentVersion.isBlank()) {
            developmentVersion =
                    releaseSemVer.bump(SemVerBump.MINOR).withSuffix("SNAPSHOT").toString();
        }

        try {
            developmentSemVer = SemVer.parse(developmentVersion);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid development version: " + developmentVersion, ex);
        }
        if (!"SNAPSHOT".equals(developmentSemVer.suffix())) {
            throw new IllegalArgumentException("Invalid development version: " + developmentVersion);
        }

        if (developmentSemVer.compareTo(releaseSemVer) <= 0) {
            throw new IllegalArgumentException("Development version must be greater than release version");
        }
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

    private void validateGitTags() throws InterruptedException {
        if (tag == null || tag.isBlank()) {
            tag = "v" + releaseSemVer.toString();
        }

        if (gitTags.hasTag(tag)) {
            throw new IllegalStateException("Git tag " + tag + " already exists");
        }

        SemVer latestVersion = gitTags.highestVersion().orElse(null);
        if (latestVersion != null && releaseSemVer.compareTo(latestVersion) <= 0) {
            throw new IllegalStateException("Release version " + releaseSemVer
                    + " must be after version derived from git tag: " + latestVersion);
        }
    }

    class GitTags {
        private Set<String> tags;
        private SortedSet<SemVer> versions;

        public boolean hasTag(String tag) throws InterruptedException {
            load();
            return tags.contains(tag);
        }

        public Optional<SemVer> highestVersion() throws InterruptedException {
            load();
            return versions.isEmpty() ? Optional.empty() : Optional.of(versions.last());
        }

        private boolean isLoaded() {
            return tags != null && versions != null;
        }

        private void load() throws InterruptedException {
            if (isLoaded()) {
                return;
            }
            tags = git.tag().lines().collect(Collectors.toSet());
            versions = tags.stream()
                    .map(this::tryConvertTagToSemVer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        private SemVer tryConvertTagToSemVer(String tag) {
            try {
                if (tag.startsWith("v")) {
                    return SemVer.parse(tag.substring(1));
                } else {
                    return SemVer.parse(tag);
                }
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    private void ensureOnDefaultBranch() throws InterruptedException {
        if (!defaultBranch.equals(currentBranch)) {
            git.switchToBranch(defaultBranch);
        }
    }

    private void runGitCliff() throws InterruptedException {
        ProcessHelper gitCliff = new ProcessHelper(directory.toFile(), "git-cliff");
        gitCliff.runNoOutput("-o", "CHANGELOG.md", "-t", tag);
        // add it to git but don't commit, that will be done by maven release prepare
        git.add("CHANGELOG.md");
    }

    private void prepareRelease() throws InterruptedException {
        maven.releasePrepare(new MavenPrepareOptionsBuilder()
                .developmentVersion(developmentVersion)
                .releaseVersion(releaseSemVer.toString())
                .tag(tag)
                // git-cliff has modified it so ignore that it is modified
                .checkModificationExcludeList("CHANGELOG.md")
                .build());
    }
}
