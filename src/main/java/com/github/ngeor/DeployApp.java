package com.github.ngeor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "deploy-app",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Deploys a library to Central Maven")
public final class DeployApp implements Callable<Integer> {
    @Option(names = "--directory", description = "The working directory", defaultValue = ".")
    private Path directory;

    @Option(names = "--gpg-key-contents", description = "The GPG key contents (base64 encoded)")
    private String gpgKeyContents;

    @Option(names = "--gpg-key-name", description = "The GPG key name", required = true)
    private String gpgKeyName;

    @Option(names = "--gpg-passphrase", description = "The GPG passphrase", required = true)
    private String gpgPassphrase;

    @Option(names = "--maven-username", description = "The Central Maven username", required = true)
    private String mavenUsername;

    @Option(names = "--maven-password", description = "The Central Maven password", required = true)
    private String mavenPassword;

    private Maven maven;
    private Gpg gpg;
    private Path tempDirectory;
    private Path tempGpgKey;
    private Path tempMavenSettings;

    private DeployApp() {}

    public static void main(String[] args) {
        System.exit(executeWithoutExiting(args));
    }

    static int executeWithoutExiting(String[] args) {
        return new CommandLine(new DeployApp()).execute(args);
    }

    @Override
    public Integer call() throws InterruptedException {
        directory = directory.toAbsolutePath().normalize();
        maven = new Maven(directory.toFile());

        List<StepDefinition> steps = List.of(
                new StepDefinition("Create temp directory", this::createTempDirectory),
                new StepDefinition("Prepare GPG", this::prepareGpg),
                new StepDefinition("Write GPG key to file", this::writeGpgKeyContentsToFile),
                new StepDefinition("Import GPG key", this::importGpgKey),
                new StepDefinition("Write temp Maven settings", this::writeTempMavenSettings),
                new StepDefinition("Deploy", this::deploy));

        List<StepDefinition> tearDownSteps =
                List.of(new StepDefinition("Remove temp directory", this::removeTempDirectory));

        Pipeline pipeline = new Pipeline(steps, tearDownSteps);
        return pipeline.call();
    }

    private void createTempDirectory() {
        try {
            tempDirectory = Files.createTempDirectory("temp");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void prepareGpg() throws InterruptedException {
        // use the tempDirectory as GPG's home directory,
        // to not interfere with any existing GPG keys
        gpg = new Gpg(tempDirectory.toFile());
        // Lists the GPG keys.
        // This is mainly used as a workaround to prime the gpg folders before importing the keys.
        gpg.listKeys();
    }

    private void writeGpgKeyContentsToFile() {
        String gpgKeyContentsDecoded = new String(Base64.getDecoder().decode(gpgKeyContents), StandardCharsets.UTF_8);
        tempGpgKey = tempDirectory.resolve("keys.asc");
        try {
            Files.writeString(tempGpgKey, gpgKeyContentsDecoded);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void importGpgKey() throws InterruptedException {
        gpg.importKey(gpgPassphrase, tempGpgKey.toFile());
    }

    private void writeTempMavenSettings() {
        String template = """
            <settings>
                <servers>
                    <server>
                        <id>central</id>
                        <username>%s</username>
                        <password>%s</password>
                    </server>
                </servers>
            </settings>
            """;
        String xml = template.formatted(mavenUsername, mavenPassword);
        tempMavenSettings = tempDirectory.resolve("settings.xml");
        try {
            Files.writeString(tempMavenSettings, xml);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void deploy() throws InterruptedException {
        maven.deploy(
                // The env variable name where the GnuPG passphrase is set.
                // This is the recommended way to pass passphrase for signing in batch mode execution of Maven.
                // The default value is MAVEN_GPG_PASSPHRASE.
                // It is possible to override with user property `gpg.passphraseEnvName`.
                Map.of("MAVEN_GPG_PASSPHRASE", gpgPassphrase),
                "-s",
                tempMavenSettings.toString(),
                // skip surefire tests
                "-DskipTests=true",
                // skip failsafe tests
                "-DskipITs=true",
                // skip checkstyle
                "-Dcheckstyle.skip=true",
                // skip jacoco
                "-Djacoco.skip=true",
                // skip invoker
                "-Dinvoker.skip=true",
                // skip spotless
                "-Dspotless.check.skip=true",
                // skip sortpom
                "-Dsort.skip=true",
                // activate gpg profile (the project that is deployed needs to have this)
                "-Pgpg",
                // specify the GPG key name
                "-Dgpg.keyname=" + gpgKeyName,
                // use the temp directory as GPG's homedir to find the keys
                "-Dgpg.homedir=" + tempDirectory.toString());
    }

    private void removeTempDirectory() {
        if (tempDirectory != null) {
            IOUtil.deleteRecursively(tempDirectory.toFile());
        }
    }
}
