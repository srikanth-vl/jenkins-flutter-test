package com.vassar.unifiedapp.err;

public abstract class UAException
    extends Exception {

    protected String errorCode;
    protected String message;
    protected Throwable cause;

    public UAException(String errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public UAException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public UAException(String erorCode, String message, Throwable cause) {
        super(message, cause);

        this.errorCode = errorCode;
        this.message = message;
        this.cause = cause;
    }
}