package com.github.ngeor;

public class ProcessFailException extends RuntimeException {
    public ProcessFailException(int exitCode, String stdErr) {
        super(String.format("%d %s", exitCode, stdErr));
    }
}
