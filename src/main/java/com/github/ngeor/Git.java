package com.github.ngeor;

import io.vavr.control.Try;
import java.io.File;

public class Git extends ProcessHelper {
    public Git(File directory) {
        super("git", directory);
    }

    public Try<String> clone(String url) {
        return run("clone", url, ".");
    }

    public Try<String> init() {
        return run("init");
    }

    public Try<String> initBare(String defaultBranch) {
        return run("init", "--bare", "-b", defaultBranch);
    }

    public Try<String> configure(String username, String email) {
        return run("config", "user.name", username).flatMap(ignored -> run("config", "user.email", email));
    }

    public Try<String> setRemoteHead(String remote, String branch) {
        return run("remote", "set-head", remote, branch);
    }

    public Try<String> push() {
        return run("push");
    }

    public Try<String> commit(String message) {
        return run("commit", "-m", message);
    }

    public Try<String> add(String filePattern) {
        return run("add", filePattern);
    }

    public Try<String> getCurrentBranch() {
        return run("rev-parse", "--abbrev-ref", "HEAD");
    }

    public Try<String> createAndSwitchToBranch(String branch) {
        return run("checkout", "-b", branch);
    }
}
