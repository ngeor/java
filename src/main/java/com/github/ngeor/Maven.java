package com.github.ngeor;

public class Maven {
    private final ProcessHelper processHelper;

    public Maven(java.io.File directory) {
        processHelper = new ProcessHelper("mvn", directory);
    }

    public void releasePrepare(MavenPrepareOptions options) throws InterruptedException {
        Validate.requireNotBlank(options.developmentVersion(), "developmentVersion cannot be blank");
        Validate.requireNotBlank(options.releaseVersion(), "releaseVersion cannot be blank");
        Validate.requireNotBlank(options.tag(), "tag cannot be blank");
        processHelper.runNoOutput(
                "-B",
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
        processHelper.runNoOutput("-B", "release:clean");
    }
}
