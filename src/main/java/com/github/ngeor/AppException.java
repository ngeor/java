package com.github.ngeor;

public class AppException extends RuntimeException {
    private final int code;

    public AppException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
