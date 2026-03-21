package com.github.ngeor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private ProcessHelper git;

    @BeforeEach
    void beforeEach() {
        git = new ProcessHelper("git", tempDir.toFile());
    }

    @Test
    void testDirectoryDoesNotExist() {
        // act
        act(tempDir.resolve("oops"));

        // assert
        assertThat(exitCode).isNotZero();
        assertThat(systemErr.getText())
                .contains("Directory " + tempDir.resolve("oops").toAbsolutePath() + " does not exist");
    }

    @Test
    void testDoesNotContainPomXml() {
        // act
        act();

        // assert
        assertThat(exitCode).isNotZero();
        assertThat(systemErr.getText())
                .contains("Directory " + tempDir.toAbsolutePath() + " does not contain a pom.xml file");
    }

    @Test
    void testDoesNotContainDotGit() throws IOException {
        // arrange
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");

        // act
        act();

        // assert
        assertThat(exitCode).isEqualTo(3);
        assertThat(systemErr.getText())
                .contains("Directory " + tempDir.toAbsolutePath() + " does not contain a .git directory");
    }

    @Test
    void testGitDirectoryIsCorrupt() throws IOException {
        // arrange
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");
        Files.createDirectory(tempDir.resolve(".git"));

        // act
        act();

        // assert
        assertThat(exitCode).isNotZero();
        assertThat(systemErr.getText()).contains("Could not check git status").contains("not a git repository");
    }

    @Test
    void testGitHasUntrackedFiles() throws Exception {
        // arrange
        git.runCheck("init");
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");

        // act
        act();

        // assert
        assertThat(exitCode).isNotZero();
        assertThat(systemErr.getText())
                .contains("Directory " + tempDir.toAbsolutePath() + " contains pending git changes");
    }

    @Test
    void testGitHasStagedNonCommittedFiles() throws Exception {
        // arrange
        git.runCheck("init");
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");
        git.runCheck("add", "pom.xml");

        // act
        act();

        // assert
        assertThat(exitCode).isNotZero();
        assertThat(systemErr.getText())
                .contains("Directory " + tempDir.toAbsolutePath() + " contains pending git changes");
    }

    @Test
    void testNoGitRemote() throws Exception {
        // arrange
        git.runCheck("init");
        git.runCheck("config", "user.name", "Dummy User");
        git.runCheck("config", "user.email", "dummy@user.com");
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");
        git.runCheck("add", "pom.xml");
        git.runCheck("commit", "-m", "Initial commit");

        // act
        act();

        // assert
        assertThat(exitCode).isNotZero();
        assertThat(systemErr.getText())
                .contains("Directory " + tempDir.toAbsolutePath() + " does not have exactly one git remote");
    }

    @Test
    void testDirectoryExistsAndContainsPomXml() throws Exception {
        // arrange
        ProcessHelper remoteGit = new ProcessHelper("git", remoteDir.toFile());
        remoteGit.runCheck("init", "--bare");
        git.runCheck("clone", remoteDir.toAbsolutePath().toString(), ".");
        git.runCheck("config", "user.name", "Dummy User");
        git.runCheck("config", "user.email", "dummy@user.com");
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");
        git.runCheck("add", "pom.xml");
        git.runCheck("commit", "-m", "Initial commit");

        // act
        act();

        // assert
        assertThat(exitCode).isZero();
        assertThat(systemOut.getText())
                .contains("Hello World! dryRun was false")
                .contains("Directory is " + tempDir.toAbsolutePath());
        assertThat(systemErr.getText()).isEmpty();
    }

    private void act() {
        act(tempDir);
    }

    private void act(Path directory) {
        exitCode = App.executeWithoutExiting(new String[] {"--directory", directory.toString()});
    }
}
