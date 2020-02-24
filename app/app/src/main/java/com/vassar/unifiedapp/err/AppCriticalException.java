package com.vassar.unifiedapp.err;

public class AppCriticalException
    extends UAException {

    public AppCriticalException(String errorCode) {
        super(errorCode);
    }

    public AppCriticalException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AppCriticalException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
