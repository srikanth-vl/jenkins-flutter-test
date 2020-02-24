package com.vassar.unifiedapp.utils;

import com.vassar.unifiedapp.err.UAAppErrorCodes;

public interface ProjectSubmissionConstants {
    String PROJECT_SUBMITTED_SUCCESSFULLY = "Project submitted successfully.";
    String PROJECT_SUBMISSION_VALIDATION_ERROR = "Validation failed for project.";
    String PROJECT_SUBMISSION_FAILED = "Project submission failed.";
    String PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC = "Project submitted offline!";
    String TOKEN_EXPIRY_MESSAGE = "Session Expired, Project Submitted Offline." +
            "\nPlease login again";
    String PROJECT_SUBMITTED_OFFLINE = "Project submitted offline";
    String PROJECT_DELETED_MESSAGE = "Project has already been deleted.";

    // Server Error Codes
    int SUBMISSION_VALIDATION_ERROR = 420;
    int APP_VERSION_MISMATCH_ERROR = 352;
    int SUCCESS = 200;
    int TOKEN_EXPIRY_ERROR = 350;
    int PROJECT_DELETED_ERROR = 351;
    int DEFAULT_SUBMISSION_RETRIES = 20;
    Long DEFAULT_RETRY_FREQUENCY = 7200000L;
}
