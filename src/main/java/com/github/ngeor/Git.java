package com.github.ngeor;

import java.io.File;

public class Git extends ProcessHelper {
    public Git(File directory) {
        super("git", directory);
    }

    public void clone(String url) throws InterruptedException {
        runNoOutput("clone", url, ".");
    }

    public void init() throws InterruptedException {
        runNoOutput("init");
    }

    public void initBare(String defaultBranch) throws InterruptedException {
        runNoOutput("init", "--bare", "-b", defaultBranch);
    }

    public void configure(String username, String email) throws InterruptedException {
        runNoOutput("config", "user.name", username);
        runNoOutput("config", "user.email", email);
    }

    public void setRemoteHead(String remote, String branch) throws InterruptedException {
        runNoOutput("remote", "set-head", remote, branch);
    }

    public void push() throws InterruptedException {
        runNoOutput("push");
    }

    public void commit(String message) throws InterruptedException {
        runNoOutput("commit", "-m", message);
    }

    public void add(String filePattern) throws InterruptedException {
        runNoOutput("add", filePattern);
    }

    public String getCurrentBranch() throws InterruptedException {
        return run("rev-parse", "--abbrev-ref", "HEAD");
    }

    public void createAndSwitchToBranch(String branch) throws InterruptedException {
        runNoOutput("checkout", "-b", branch);
    }
}
