package com.vassar.unifiedapp.err;

public class AppOfflineException extends UAException {

    public AppOfflineException(String errorCode) {
        super(errorCode);
    }

    public AppOfflineException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AppOfflineException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
