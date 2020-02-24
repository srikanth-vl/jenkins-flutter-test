package com.vassar.unifiedapp.err;

public class ServerFetchException extends UAException {

    public ServerFetchException(String errorCode) {
        super(errorCode);
    }

    public ServerFetchException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ServerFetchException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
