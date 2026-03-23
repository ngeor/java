package com.github.ngeor;

public class Maven {
    private final ProcessHelper processHelper;

    public Maven(java.io.File directory) {
        processHelper = new ProcessHelper("mvn", directory);
    }

    public void releasePrepare(String developmentVersion, String releaseVersion, String tag)
            throws InterruptedException {
        Validate.requireNotBlank(developmentVersion, "developmentVersion cannot be blank");
        Validate.requireNotBlank(releaseVersion, "releaseVersion cannot be blank");
        Validate.requireNotBlank(tag, "tag cannot be blank");
        processHelper.runNoOutput(
                "-B",
                "-DdevelopmentVersion=" + developmentVersion,
                "-DreleaseVersion=" + releaseVersion,
                "-Dtag=" + tag,
                "-DpushChanges=false",
                "-DcompletionGoals=validate",
                "-DcheckModificationExcludeList=CHANGELOG.md",
                "release:prepare");
    }

    public void releaseClean() throws InterruptedException {
        processHelper.runNoOutput("-B", "release:clean");
    }
}
