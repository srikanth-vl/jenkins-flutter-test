package controllers.com.vassarlabs.play.constants;

public class ResponseConstants {

	public static final int SUCCESS_CODE = 200;

	public static final int SERVER_CONNECTION_ERROR = -999;
	public static final int TOKEN_NULL_ERROR_CODE = 300;
	public static final int TOKEN_EXPIRED_ERROR_CODE = 350;
	public static final int AUTHENTICATION_ERROR_CODE = 370;
	public static final int USER_NOT_FOUND_ERROR_CODE = 375;
	public static final int JSON_MAPPING_ERROR_CODE = 380;
	public static final int JSON_PARSING_ERROR_CODE = 385;
	public static final int JSON_PROCESSING_ERROR_CODE = 390;
	public static final int IO_ERROR_CODE = 395;
	public static final int APP_META_DATA_NOT_FOUND_ERROR_CODE = 399;
	public static final int CASSANDRA_EXCEPTION_ERROR_CODE = 400;
	public static final int LOGOUT_UNSUCCESSFUL_ERROR_CODE = 405;
	public static final int SERVICE_OUTPUT_NULL_CODE = 410;
	public static final int VALIDATION_FAILURE = 420;
	public static final int FAILURE = 421;
	public static final int REQUEST_MAP_NULL_ERROR_CODE = 100;
	public static final int NO_DATA_FOUND_ERROR_CODE = 108;
	public static final int INVALID_INPUT_ERROR_CODE = 110;
	public static final int VALIDATION_EXCEPTION_ERROR_CODE = 111;
	public static final int DATA_DELETION_ERROR = 351;
	public static final int APP_VERSION_MISTMATCH_ERROR_CODE = 352;

	public static final int INVALID_OTP_ERROR_CODE = 121;
	public static final int RESET_PASSWORD_MISMATCH_ERROR_CODE  = 122;
	public static final int OLD_AND_NEW_PASSWORD_MATCH_ERROR_CODE  = 123;
	public static final int PASSWORD_PATTERN_MISMATCH_ERROR_CODE  = 124;
	public static final int USER_MOBILE_NO_NOT_FOUND_ERROR_CODE  = 125;
	public static final int SMSSEND_EXCEPTION_ERROR_CODE = 126;
	public static final int SYNC_PERIOD_EXCEEDED = 351;
	public static final String PROJECT_SYNC_SUCCESS = "All projects synced successfully";
	public static final String UNSUCCESSFUL_OPERATION = "Unsuccessful operation";
	public static final String IMAGE_SYNC_SUCCESS = "Image saved to the system";
	public static final String TOKEN_NULL_ERROR_MESSAGE = "Token not found. Please try again!";
	public static final String TOKEN_EXPIRED_ERROR_MESSAGE = "Invalid Token - Token Expired. Please try again!";
	public static final String AUTHENTICATION_ERROR_MESSAGE = "Username and Password don't match. Please try again!";
	public static final String USER_NOT_FOUND_ERROR_MESSAGE = "User not found. Please try again!";
	public static final String JSON_MAPPING_ERROR_MESSAGE = "Invalid input! Error occured while mapping Json";
	public static final String JSON_PARSING_ERROR_MESSAGE = "Invalid input! Error occured while parsing Json";
	public static final String JSON_PROCESSING_ERROR_MESSAGE = "Invalid Input! Error occured while processing Json";
	public static final String IO_ERROR_MESSAGE = "I/O error occured";
	public static final String APP_META_DATA_NOT_FOUND_ERROR_MESSAGE = "Information about app not found";
	public static final String CASSANDRA_EXCEPTION_ERROR_MESSAGE = "Connection issue - can't connect to database";
	public static final String SYNC_PERIOD_EXCEEDED_MESSAGE = "Form Submisson Exceeded Sync Period!";

	public static final String LOGOUT_SUCCESS = "Logout successful";
	public static final String LOGOUT_UNSUCCESSFUL = "Logout unsuccessful. Try again later";
	public static final String TOKEN_EXPIRED = "Token Expired";

	public static final String SERVICE_OUTPUT_NULL_MESSAGE = "Service Parameters are NULL. Can't process further";
	public static final String REQUEST_MAP_NULL_ERROR_MESSAGE = "Request map is NULL";
	public static final String NO_DATA_FOUND_ERROR_MESSAGE = "No data found for the request";
	public static final String INVALID_INPUT_ERROR_MESSAGE = "Invalid input";
	public static final String VALIDATION_EXCEPTION_ERROR_MESSAGE = "Not valid data";

	public static final String INVALID_OTP_ERROR_MESSAGE = "Invalid OTP or OTP is Expired";
	public static final String RESET_PASSWORD_MISMATCH_ERROR_MESSAGE = "New Password and Confirm Password did not match";
	public static final String OLD_AND_NEW_PASSWORD_MATCH_ERROR_MESSAGE = "New and old Password cannot be same";
	public static final String PASSWORD_PATTERN_MISMATCH_ERROR_MESSAGE = "Password must contain atleast 6 characters including UPPER/lowercase and numbers";
	public static final String USER_MOBILE_NO_NOT_FOUND_ERROR_MESSAGE = "Mobile number is not registered";
	public static final String SMSSEND_EXCEPTION_ERROR_MESSAGE = "OTP could not be send. Please try again!";
	public static final String PASSWORD_RESET_SUCCESSFUL = "Password reset successful";
	public static final String GENERATE_OTP_FOR_PASSWORD_RESET_SUCCESSFUL = "OTP sent to registered moble number";
	public static final String PROBLEM_DATA_RELAY = "Problem in relaying data to other servers";
	
	public static final String SERVER_CONNECTION_PROBLEM = "Problem connecting to the server. Please try again later";
	
	public static final String PROJECT_DATA = "project_data";
	public static final String USER_DATA = "user_data";
	public static final String USER_MAP = "user_details";

	public static final String SUCCESSFUL_MESSAGE = "Successful operation";
	
}
