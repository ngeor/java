package com.github.ngeor;

public class ProcessFailException extends RuntimeException {
    public ProcessFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessFailException(int exitCode, String stdErr) {
        super(String.format("%d %s", exitCode, stdErr));
    }
}
