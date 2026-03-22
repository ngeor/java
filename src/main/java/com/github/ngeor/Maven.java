package com.github.ngeor;

public class Maven {
    private final ProcessHelper processHelper;

    public Maven(java.io.File directory) {
        processHelper = new ProcessHelper("mvn", directory);
    }

    public void releaseClean() throws InterruptedException {
        processHelper.runNoOutput("release:clean");
    }
}
