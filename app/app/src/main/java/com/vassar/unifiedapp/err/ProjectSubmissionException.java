package com.vassar.unifiedapp.err;

public class ProjectSubmissionException extends UAException {

    public ProjectSubmissionException(String errorCode) {
        super(errorCode);
    }

    public ProjectSubmissionException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ProjectSubmissionException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
