package com.github.ngeor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;

/**
 * Integration tests for {@link App}.
 */
@ExtendWith(SystemStubsExtension.class)
class AppTest {
    @TempDir
    private Path workingDir;

    @TempDir
    private Path remoteDir;

    @SystemStub
    private SystemErr systemErr;

    private int exitCode;

    private Git git;

    @BeforeEach
    void beforeEach() throws InterruptedException {
        git = new Git(workingDir.toFile());
        Git remoteGit = new Git(remoteDir.toFile());
        remoteGit.initBare("master");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                ".",
                "1",
                "1.",
                "1.2",
                "1..2",
                "1..2.3",
                "1.2.3.4",
                "1.2.3.abc",
                "...",
                "1.2.3",
                "1.2.3-",
                "1.2.3-FIX"
            })
    void testNonSemVerOrNonSnapshotDevelopmentVersion(String developmentVersion) {
        // act
        act(builder -> builder.releaseVersion("1.2.3").developmentVersion(developmentVersion));
        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText()).contains("Invalid development version: " + developmentVersion);
        });
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                ".",
                "1",
                "1.",
                "1.2",
                "1..2",
                "1..2.3",
                "1.2.3.4",
                "1.2.3.abc",
                "...",
                "1.2.3-SNAPSHOT",
                "1.2.3-"
            })
    void testNonSemVerOrSnapshotReleaseVersion(String releaseVersion) {
        // act
        act(builder -> builder.releaseVersion(releaseVersion).developmentVersion("2.0.0-SNAPSHOT"));
        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText()).contains("Invalid release version: " + releaseVersion);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.2.3", "1.2.4", "1.3.0", "2.0.0"})
    void testDevelopmentVersionMustBeGreaterThanReleaseVersion(String releaseVersion) {
        // act
        act(builder -> builder.releaseVersion(releaseVersion).developmentVersion("1.2.3-SNAPSHOT"));
        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText()).contains("Development version must be greater than release version");
        });
    }

    @Test
    void testDirectoryDoesNotExist() {
        // act
        act(workingDir.resolve("oops"));

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + workingDir.resolve("oops").toAbsolutePath() + " does not exist");
        });
    }

    @Test
    void testDoesNotContainPomXml() {
        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + workingDir.toAbsolutePath() + " does not contain a pom.xml file");
        });
    }

    @Test
    void testDoesNotContainDotGit() throws IOException {
        // arrange
        Files.writeString(workingDir.resolve("pom.xml"), "<project></project>");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + workingDir.toAbsolutePath() + " does not contain a .git directory");
        });
    }

    @Test
    void testGitDirectoryIsCorrupt() throws IOException {
        // arrange
        Files.writeString(workingDir.resolve("pom.xml"), "<project></project>");
        Files.createDirectory(workingDir.resolve(".git"));

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Ensure no pending git changes")
                    .contains("not a git repository");
        });
    }

    @Test
    void testGitHasUntrackedFiles() throws InterruptedException, IOException {
        // arrange
        git.init();
        Files.writeString(workingDir.resolve("pom.xml"), "<project></project>");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + workingDir.toAbsolutePath() + " contains pending git changes");
        });
    }

    @Test
    void testGitHasStagedNonCommittedFiles() throws InterruptedException, IOException {
        // arrange
        git.init();
        Files.writeString(workingDir.resolve("pom.xml"), "<project></project>");
        git.add("pom.xml");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + workingDir.toAbsolutePath() + " contains pending git changes");
        });
    }

    @Test
    void testNoGitRemote() throws InterruptedException, IOException {
        // arrange
        git.init();
        git.configure("Dummy User", "dummy@user.com");
        Files.writeString(workingDir.resolve("pom.xml"), "<project></project>");
        git.add("pom.xml");
        git.commit("Initial commit");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + workingDir.toAbsolutePath() + " does not have exactly one git remote");
        });
    }

    @Test
    void testGitTagAlreadyExists() throws InterruptedException, IOException {
        // arrange
        cloneRepoAndPushInitialCommit();
        git.createTag("v1.2.0", "Releasing version 1.2.0");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText()).contains("Git tag v1.2.0 already exists");
        });
    }

    @ParameterizedTest
    @MethodSource("existingTagsConflictingWithReleaseVersion")
    void testGitTagsConflictingWithReleaseVersion(List<String> tags, String highestVersion)
            throws InterruptedException, IOException {
        // arrange
        cloneRepoAndPushInitialCommit();
        for (String tag : tags) {
            git.createTag(tag, "Releasing version " + tag);
        }
        // act
        act();
        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Release version 1.2.0 must be after version derived from git tag: " + highestVersion);
        });
    }

    private static Stream<Arguments> existingTagsConflictingWithReleaseVersion() {
        return Stream.of(
                Arguments.of(List.of("1.2.0"), "1.2.0"),
                Arguments.of(List.of("1.1.0", "1.2.0"), "1.2.0"),
                Arguments.of(List.of("1.1.0", "1.3.0"), "1.3.0"),
                Arguments.of(List.of("v1.3.0"), "1.3.0"),
                Arguments.of(List.of("v1.3.0", "1.3.0"), "1.3.0"),
                Arguments.of(List.of("v1.3.0", "1.2.1"), "1.3.0"),
                Arguments.of(List.of("oops", "v.Oops", "1.4.0"), "1.4.0"));
    }

    @ParameterizedTest
    @MethodSource("existingTagsWithoutConflictingWithReleaseVersion")
    void testGitTagsWithoutConflictingWithReleaseVersion(List<String> tags) throws InterruptedException, IOException {
        // arrange
        cloneRepoAndPushInitialCommit();
        for (String tag : tags) {
            git.createTag(tag, "Releasing version " + tag);
        }
        // act
        act();
        // assert
        assertThat(exitCode).isZero();
    }

    private static Stream<Arguments> existingTagsWithoutConflictingWithReleaseVersion() {
        return Stream.of(
                Arguments.of(List.of("1.1.0")),
                Arguments.of(List.of("v1.1.0")),
                Arguments.of(List.of("1.1.0", "1.1.1")),
                Arguments.of(List.of("oops", "v.Oops")));
    }

    @Test
    void testNotOnDefaultBranch() throws InterruptedException, IOException {
        // arrange
        cloneRepoAndPushInitialCommit();
        // switch to a different branch
        git.createAndSwitchToBranch("feature");

        // act
        act();

        // assert
        String currentBranch = git.getCurrentBranch();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isZero();
            softly.assertThat(currentBranch).isEqualTo("master");
        });
    }

    @Test
    void testGetsLatestFromUpstream() throws IOException, InterruptedException {
        // arrange
        cloneRepoAndPushInitialCommit();
        Files.writeString(workingDir.resolve("README.md"), "A readme file");
        git.add("README.md");
        git.commit("Added readme");
        git.push();

        git.reset(true, 1);
        assertThat(workingDir.resolve("README.md").toFile().exists())
                .as("README should be gone after git reset")
                .isFalse();

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isZero();
            softly.assertThat(workingDir.resolve("README.md").toFile().exists())
                    .as("README should be back after git pull")
                    .isTrue();
            softly.assertThat(systemErr.getText()).isEmpty();
        });
    }

    @Test
    void testFullFlow() throws IOException, InterruptedException {
        // arrange
        cloneRepoAndPushInitialCommit();
        final String tag = "v1.2.0";

        // act
        act();

        // assert
        String pomXmlContents = Files.readString(workingDir.resolve("pom.xml"));
        List<String> tags = git.tag().lines().toList();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isZero();
            softly.assertThat(systemErr.getText()).isEmpty();
            softly.assertThat(pomXmlContents)
                    .contains("<version>1.2.1-SNAPSHOT</version>")
                    .contains("<tag>HEAD</tag>");
            softly.assertThat(tags).containsExactly(tag);
        });
        assertThat(git.statusPorcelain())
                .as("Should not have any pending changes")
                .isEmpty();
        git.switchToBranch(tag);
        String tagPomXmlContents = Files.readString(workingDir.resolve("pom.xml"));
        SoftAssertions.assertSoftly(softly -> softly.assertThat(tagPomXmlContents)
                .contains("<version>1.2.0</version>")
                .contains("<tag>" + tag + "</tag>"));
        String changeLogContents = Files.readString(workingDir.resolve("CHANGELOG.md"));
        assertThat(changeLogContents).contains("[1.2.0]").contains("- Initial commit");
    }

    @Test
    void testOptionalDevelopmentVersion() throws IOException, InterruptedException {
        // arrange
        cloneRepoAndPushInitialCommit();
        final String tag = "v1.2.0";

        // act
        act(builder -> builder.developmentVersion(Optional.empty()));

        // assert
        String pomXmlContents = Files.readString(workingDir.resolve("pom.xml"));
        List<String> tags = git.tag().lines().toList();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isZero();
            softly.assertThat(systemErr.getText()).isEmpty();
            softly.assertThat(pomXmlContents)
                    .contains("<version>1.3.0-SNAPSHOT</version>")
                    .contains("<tag>HEAD</tag>");
            softly.assertThat(tags).containsExactly(tag);
        });
        assertThat(git.statusPorcelain())
                .as("Should not have any pending changes")
                .isEmpty();
        git.switchToBranch(tag);
        String tagPomXmlContents = Files.readString(workingDir.resolve("pom.xml"));
        SoftAssertions.assertSoftly(softly -> softly.assertThat(tagPomXmlContents)
                .contains("<version>1.2.0</version>")
                .contains("<tag>" + tag + "</tag>"));
        String changeLogContents = Files.readString(workingDir.resolve("CHANGELOG.md"));
        assertThat(changeLogContents).contains("[1.2.0]").contains("- Initial commit");
    }

    @Test
    void testCustomTag() throws IOException, InterruptedException {
        // arrange
        cloneRepoAndPushInitialCommit();
        final String tag = "java-1.9.0";

        // act
        act(builder -> builder.developmentVersion("2.0.0-SNAPSHOT")
                .releaseVersion("1.9.0")
                .tag(tag));

        // assert
        String pomXmlContents = Files.readString(workingDir.resolve("pom.xml"));
        List<String> tags = git.tag().lines().toList();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isZero();
            softly.assertThat(systemErr.getText()).isEmpty();
            softly.assertThat(pomXmlContents)
                    .contains("<version>2.0.0-SNAPSHOT</version>")
                    .contains("<tag>HEAD</tag>");
            softly.assertThat(tags).containsExactly(tag);
        });
        git.switchToBranch(tag);
        String tagPomXmlContents = Files.readString(workingDir.resolve("pom.xml"));
        SoftAssertions.assertSoftly(softly -> softly.assertThat(tagPomXmlContents)
                .contains("<version>1.9.0</version>")
                .contains("<tag>" + tag + "</tag>"));
    }

    private void cloneRepoAndPushInitialCommit() throws InterruptedException, IOException {
        String remotePath = remoteDir.toAbsolutePath().toString();
        git.clone(remotePath);
        git.configure("Dummy User", "dummy@user.com");
        String contents = IOUtil.readResource("/sample_pom.xml").replaceAll("\\$REMOTE", remotePath);
        assertThat(contents).contains(remotePath);
        Files.writeString(workingDir.resolve("pom.xml"), contents);
        git.add("pom.xml");
        Files.writeString(workingDir.resolve(".gitignore"), "target/");
        git.add(".gitignore");
        git.commit("feat: Initial commit");
        // a push is needed to mark the default branch
        git.push();
        git.setRemoteHead("origin", "master");
    }

    private void act() {
        act(UnaryOperator.identity());
    }

    private void act(Path directory) {
        act(builder -> builder.directory(directory.toString()));
    }

    private void act(UnaryOperator<AppOptionsBuilder> customizer) {
        AppOptions options = customizer
                .apply(new AppOptionsBuilder()
                        .directory(workingDir.toString())
                        .developmentVersion("1.2.1-SNAPSHOT")
                        .releaseVersion("1.2.0"))
                .build();
        List<String> args = new ArrayList<>(List.of("--directory", options.directory()));
        options.developmentVersion().ifPresent(s -> args.addAll(List.of("--development-version", s)));
        options.releaseVersion().ifPresent(s -> args.addAll(List.of("--release-version", s)));
        options.tag().ifPresent(s -> args.addAll(List.of("--tag", s)));
        exitCode = App.executeWithoutExiting(args.toArray(String[]::new));
    }
}
