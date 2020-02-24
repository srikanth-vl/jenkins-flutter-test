class ProjectSubmissionConstants {
  static String PROJECT_SUBMITTED_SUCCESSFULLY = "Project submitted successfully.";
  static String PROJECT_SUBMISSION_VALIDATION_ERROR = "Validation failed for project.";
  static String PROJECT_SUBMISSION_FAILED = "Project submission failed.";
  static String PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC = "Project submitted offline!";
  static String TOKEN_EXPIRY_MESSAGE = "Session Expired, Project Submitted Offline." +
      "\nPlease login again";
  static String PROJECT_SUBMITTED_OFFLINE = "Project submitted offline";
  static String PROJECT_DELETED_MESSAGE = "Project has already been deleted.";

  // Server Error Codes
  static int SUBMISSION_VALIDATION_ERROR = 420;
  static int APP_VERSION_MISMATCH_ERROR = 352;
  static int SUCCESS = 200;
  static int TOKEN_EXPIRY_ERROR = 350;
  static int PROJECT_DELETED_ERROR = 351;
  static int DEFAULT_SUBMISSION_RETRIES = 20;
  static int DEFAULT_RETRY_FREQUENCY = 7200000;
}
