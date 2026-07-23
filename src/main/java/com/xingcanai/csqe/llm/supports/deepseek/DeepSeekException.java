package com.xingcanai.csqe.llm.supports.deepseek;

public class DeepSeekException extends RuntimeException {

    private final String errorCode;
    private final boolean automaticRetryAllowed;

    public DeepSeekException(String errorCode, boolean automaticRetryAllowed, String message) {
        super(message);
        this.errorCode = errorCode;
        this.automaticRetryAllowed = automaticRetryAllowed;
    }

    public DeepSeekException(String errorCode, boolean automaticRetryAllowed, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.automaticRetryAllowed = automaticRetryAllowed;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isAutomaticRetryAllowed() {
        return automaticRetryAllowed;
    }
}

