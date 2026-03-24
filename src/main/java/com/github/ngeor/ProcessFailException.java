package com.github.ngeor;

public class ProcessFailException extends RuntimeException {
    private final int code;

    public ProcessFailException(int code, String stdErr) {
        super(stdErr);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
