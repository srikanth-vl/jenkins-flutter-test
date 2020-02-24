package com.vassar.unifiedapp.err;

public class UAConfigException
        extends UAException {

    public UAConfigException(String errorCode) {
        super(errorCode);
    }

    public UAConfigException(String errorCode, String message) {
        super(errorCode, message);
    }

    public UAConfigException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
