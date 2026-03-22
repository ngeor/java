package com.github.ngeor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

/**
 * Integration tests for {@link App}.
 */
@ExtendWith(SystemStubsExtension.class)
class AppTest {
    @TempDir
    private Path tempDir;

    @TempDir
    private Path remoteDir;

    @SystemStub
    private SystemOut systemOut;

    @SystemStub
    private SystemErr systemErr;

    private int exitCode;

    private Git git;

    @BeforeEach
    void beforeEach() throws InterruptedException {
        git = new Git(tempDir.toFile());
        Git remoteGit = new Git(remoteDir.toFile());
        remoteGit.initBare("master");
    }

    @Test
    void testDirectoryDoesNotExist() {
        // act
        act(tempDir.resolve("oops"));

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + tempDir.resolve("oops").toAbsolutePath() + " does not exist");
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
                    .contains("Directory " + tempDir.toAbsolutePath() + " does not contain a pom.xml file");
        });
    }

    @Test
    void testDoesNotContainDotGit() throws IOException {
        // arrange
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isEqualTo(3);
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + tempDir.toAbsolutePath() + " does not contain a .git directory");
        });
    }

    @Test
    void testGitDirectoryIsCorrupt() throws IOException {
        // arrange
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");
        Files.createDirectory(tempDir.resolve(".git"));

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isEqualTo(4);
            softly.assertThat(systemErr.getText())
                    .contains("Ensure no pending git changes")
                    .contains("not a git repository");
        });
    }

    @Test
    void testGitHasUntrackedFiles() throws InterruptedException, IOException {
        // arrange
        git.init();
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + tempDir.toAbsolutePath() + " contains pending git changes");
        });
    }

    @Test
    void testGitHasStagedNonCommittedFiles() throws InterruptedException, IOException {
        // arrange
        git.init();
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");
        git.add("pom.xml");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + tempDir.toAbsolutePath() + " contains pending git changes");
        });
    }

    @Test
    void testNoGitRemote() throws InterruptedException, IOException {
        // arrange
        git.init();
        git.configure("Dummy User", "dummy@user.com");
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");
        git.add("pom.xml");
        git.commit("Initial commit");

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isNotZero();
            softly.assertThat(systemErr.getText())
                    .contains("Directory " + tempDir.toAbsolutePath() + " does not have exactly one git remote");
        });
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
        Files.writeString(tempDir.resolve("README.md"), "A readme file");
        git.add("README.md");
        git.commit("Added readme");
        git.push();

        git.reset(true, 1);
        assertThat(tempDir.resolve("README.md").toFile().exists())
                .as("README should be gone after git reset")
                .isFalse();

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isZero();
            softly.assertThat(tempDir.resolve("README.md").toFile().exists())
                    .as("README should be back after git pull")
                    .isTrue();
            softly.assertThat(systemOut.getText())
                    .contains("Hello World! dryRun was false")
                    .contains("Directory is " + tempDir.toAbsolutePath());
            softly.assertThat(systemErr.getText()).isEmpty();
        });
    }

    @Test
    void testDirectoryExistsAndContainsPomXml() throws IOException, InterruptedException {
        // arrange
        cloneRepoAndPushInitialCommit();

        // act
        act();

        // assert
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(exitCode).isZero();
            softly.assertThat(systemOut.getText())
                    .contains("Hello World! dryRun was false")
                    .contains("Directory is " + tempDir.toAbsolutePath());
            softly.assertThat(systemErr.getText()).isEmpty();
        });
    }

    private void cloneRepoAndPushInitialCommit() throws InterruptedException, IOException {
        String remotePath = remoteDir.toAbsolutePath().toString();
        git.clone(remotePath);
        git.configure("Dummy User", "dummy@user.com");
        try (InputStream inputStream = getClass().getResourceAsStream("/sample_pom.xml")) {
            assertThat(inputStream).isNotNull();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream.transferTo(byteArrayOutputStream);
            String contents = byteArrayOutputStream.toString(StandardCharsets.UTF_8)
                .replaceAll("\\$REMOTE", remotePath);
            assertThat(contents).contains(remotePath);
           Files.writeString(tempDir.resolve("pom.xml"), contents);
        }
        git.add("pom.xml");
        git.commit("Initial commit");
        // a push is needed to mark the default branch
        git.push();
        git.setRemoteHead("origin", "master");
    }

    private void act() {
        act(tempDir);
    }

    private void act(Path directory) {
        exitCode = App.executeWithoutExiting(new String[] {"--directory", directory.toString()});
    }
}
