package com.github.ngeor;

import java.io.File;

public class Git extends ProcessHelper {
    public Git(File directory) {
        super("git", directory);
    }

    public Result<String, RuntimeException> clone(String url) {
        return run("clone", url, ".");
    }

    public Result<String, RuntimeException> init() {
        return run("init");
    }

    public Result<String, RuntimeException> initBare(String defaultBranch) {
        return run("init", "--bare", "-b", defaultBranch);
    }

    public Result<String, RuntimeException> configure(String username, String email) {
        return run("config", "user.name", username).andThen(() -> run("config", "user.email", email));
    }

    public Result<String, RuntimeException> setRemoteHead(String remote, String branch) {
        return run("remote", "set-head", remote, branch);
    }

    public Result<String, RuntimeException> push() {
        return run("push");
    }

    public Result<String, RuntimeException> commit(String message) {
        return run("commit", "-m", message);
    }

    public Result<String, RuntimeException> add(String filePattern) {
        return run("add", filePattern);
    }

    public Result<String, RuntimeException> getCurrentBranch() {
        return run("rev-parse", "--abbrev-ref", "HEAD");
    }

    public Result<String, RuntimeException> createAndSwitchToBranch(String branch) {
        return run("checkout", "-b", branch);
    }
}
