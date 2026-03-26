package com.github.ngeor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Maven {
    private final ProcessHelper processHelper;

    public Maven(java.io.File directory) {
        processHelper = new ProcessHelper(directory, "mvn", "-B");
    }

    public void releasePrepare(MavenPrepareOptions options) throws InterruptedException {
        Validate.requireNotBlank(options.developmentVersion(), "developmentVersion cannot be blank");
        Validate.requireNotBlank(options.releaseVersion(), "releaseVersion cannot be blank");
        Validate.requireNotBlank(options.tag(), "tag cannot be blank");
        processHelper.runNoOutput(
                ProcessBuilder::inheritIO,
                "-DdevelopmentVersion=" + options.developmentVersion(),
                "-DreleaseVersion=" + options.releaseVersion(),
                "-Dtag=" + options.tag(),
                "-DpushChanges=" + options.pushChanges(),
                "-DcompletionGoals=" + options.completionGoals(),
                options.checkModificationExcludeList()
                        .map(s -> "-DcheckModificationExcludeList=" + s)
                        .orElse(""),
                "release:prepare");
    }

    public void releaseClean() throws InterruptedException {
        processHelper.runNoOutput("release:clean");
    }

    public void deploy(Map<String, String> env, String... args) throws InterruptedException {
        List<String> argsAsList = new ArrayList<>(List.of("-ntp"));
        argsAsList.addAll(Arrays.asList(args));
        argsAsList.add("deploy");
        ProcessBuilder pb = processHelper
                .createProcessBuilder(argsAsList.toArray(String[]::new))
                .inheritIO();
        pb.environment().putAll(env);
        try {
            int exitCode = pb.start().waitFor();
            if (exitCode != 0) {
                throw new ProcessFailException(exitCode, "Could not deploy");
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
