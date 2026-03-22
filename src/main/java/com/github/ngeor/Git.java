package com.github.ngeor;

import java.io.File;

public class Git extends ProcessHelper {
    public Git(File directory) {
        super("git", directory);
    }

    public void clone(String url) throws InterruptedException {
        Validate.requireNotBlank(url, "clone url cannot be blank");
        runNoOutput("clone", url, ".");
    }

    public void init() throws InterruptedException {
        runNoOutput("init");
    }

    public void initBare(String defaultBranch) throws InterruptedException {
        Validate.requireNotBlank(defaultBranch, "defaultBranch cannot be blank");
        runNoOutput("init", "--bare", "-b", defaultBranch);
    }

    public void configure(String username, String email) throws InterruptedException {
        Validate.requireNotBlank(username, "username cannot be blank");
        Validate.requireNotBlank(email, "email cannot be blank");
        runNoOutput("config", "user.name", username);
        runNoOutput("config", "user.email", email);
    }

    public void setRemoteHead(String remote, String branch) throws InterruptedException {
        Validate.requireNotBlank(remote, "remote cannot be blank");
        Validate.requireNotBlank(branch, "branch cannot be blank");
        runNoOutput("remote", "set-head", remote, branch);
    }

    public void push() throws InterruptedException {
        runNoOutput("push");
    }

    public void commit(String message) throws InterruptedException {
        Validate.requireNotBlank(message, "message cannot be blank");
        runNoOutput("commit", "-m", message);
    }

    public void add(String filePattern) throws InterruptedException {
        Validate.requireNotBlank(filePattern, "filePattern cannot be blank");
        runNoOutput("add", filePattern);
    }

    public String getCurrentBranch() throws InterruptedException {
        return run("rev-parse", "--abbrev-ref", "HEAD");
    }

    public void createAndSwitchToBranch(String branch) throws InterruptedException {
        Validate.requireNotBlank(branch, "branch cannot be blank");
        runNoOutput("checkout", "-b", branch);
    }

    public void switchToBranch(String branch) throws InterruptedException {
        Validate.requireNotBlank(branch, "branch cannot be blank");
        runNoOutput("checkout", branch);
    }

    public String getDefaultBranch(String remote) throws InterruptedException {
        Validate.requireNotBlank(remote, "remote cannot be blank");
        String prefix = "refs/remotes/" + remote + "/";
        String result = run("symbolic-ref", prefix + "HEAD");
        if (result == null || result.isBlank() || !result.startsWith(prefix)) {
            throw new GitException("Unexpected response from git symbolic-ref");
        }

        result = result.substring(prefix.length());
        if (result.isBlank()) {
            throw new GitException("Unexpected response from git symbolic-ref");
        }

        return result;
    }
}
