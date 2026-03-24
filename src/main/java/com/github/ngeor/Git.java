package com.github.ngeor;

import java.io.File;

public class Git {
    private final ProcessHelper processHelper;

    public Git(File directory) {
        processHelper = new ProcessHelper("git", directory);
    }

    public void clone(String url) throws InterruptedException {
        Validate.requireNotBlank(url, "clone url cannot be blank");
        processHelper.runNoOutput("clone", url, ".");
    }

    public void init() throws InterruptedException {
        processHelper.runNoOutput("init");
    }

    public void initBare(String defaultBranch) throws InterruptedException {
        Validate.requireNotBlank(defaultBranch, "defaultBranch cannot be blank");
        processHelper.runNoOutput("init", "--bare", "-b", defaultBranch);
    }

    public void configure(String username, String email) throws InterruptedException {
        Validate.requireNotBlank(username, "username cannot be blank");
        Validate.requireNotBlank(email, "email cannot be blank");
        processHelper.runNoOutput("config", "user.name", username);
        processHelper.runNoOutput("config", "user.email", email);
    }

    public void setRemoteHead(String remote, String branch) throws InterruptedException {
        Validate.requireNotBlank(remote, "remote cannot be blank");
        Validate.requireNotBlank(branch, "branch cannot be blank");
        processHelper.runNoOutput("remote", "set-head", remote, branch);
    }

    public void push() throws InterruptedException {
        processHelper.runNoOutput("push");
    }

    public void pushFollowTags() throws InterruptedException {
        processHelper.runNoOutput("push", "--follow-tags");
    }

    public void commit(String message) throws InterruptedException {
        Validate.requireNotBlank(message, "message cannot be blank");
        processHelper.runNoOutput("commit", "-m", message);
    }

    public void add(String filePattern) throws InterruptedException {
        Validate.requireNotBlank(filePattern, "filePattern cannot be blank");
        processHelper.runNoOutput("add", filePattern);
    }

    public String getCurrentBranch() throws InterruptedException {
        return processHelper.run("rev-parse", "--abbrev-ref", "HEAD");
    }

    public void createAndSwitchToBranch(String branch) throws InterruptedException {
        Validate.requireNotBlank(branch, "branch cannot be blank");
        processHelper.runNoOutput("checkout", "-b", branch);
    }

    public void switchToBranch(String branch) throws InterruptedException {
        Validate.requireNotBlank(branch, "branch cannot be blank");
        processHelper.runNoOutput("checkout", branch);
    }

    public String getDefaultBranch(String remote) throws InterruptedException {
        Validate.requireNotBlank(remote, "remote cannot be blank");
        String prefix = "refs/remotes/" + remote + "/";
        String result = processHelper.run("symbolic-ref", prefix + "HEAD");
        if (result == null || result.isBlank() || !result.startsWith(prefix)) {
            throw new GitException("Unexpected response from git symbolic-ref");
        }

        result = result.substring(prefix.length());
        if (result.isBlank()) {
            throw new GitException("Unexpected response from git symbolic-ref");
        }

        return result;
    }

    public String statusPorcelain() throws InterruptedException {
        return processHelper.run("status", "--porcelain");
    }

    public String remote() throws InterruptedException {
        return processHelper.run("remote");
    }

    public void pull() throws InterruptedException {
        processHelper.runNoOutput("pull");
    }

    public void reset(boolean hard, int numberOfCommits) throws InterruptedException {
        processHelper.runNoOutput("reset", hard ? "--hard" : "--soft", "HEAD~" + numberOfCommits);
    }

    public String tag() throws InterruptedException {
        return processHelper.run("tag");
    }

    public void createTag(String tag, String message) throws InterruptedException {
        Validate.requireNotBlank(tag, "tag cannot be blank");
        Validate.requireNotBlank(message, "message cannot be blank");
        processHelper.runNoOutput("tag", "-m", message, tag);
    }
}
