package controllers.com.vassarlabs.play.controllers;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.InvalidOtpException;
import com.vassarlabs.common.utils.err.InvalidPasswordException;
import com.vassarlabs.common.utils.err.MobileNoNotFoundException;
import com.vassarlabs.common.utils.err.OldAndNewPasswordMatchException;
import com.vassarlabs.common.utils.err.ResetPasswordMismatchException;
import com.vassarlabs.common.utils.err.SMSSendException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.GenerateOtpRequest;
import com.vassarlabs.proj.uniapp.api.pojo.ResetPasswordRequest;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.login.service.ResetPasswordService;

import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

@org.springframework.stereotype.Controller
public class CommonController extends BasicController{
	@Autowired
	private ResetPasswordService resetPasswordService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public Result generatePasswordResetOtp() throws JsonProcessingException {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String superAppStr =  jsonNode.findPath("super_app").asText();
		GenerateOtpRequest generateOtpRequestObject = objectMapper.treeToValue(jsonNode, GenerateOtpRequest.class);
		
		if ( !(validateInput(UUID.class, superAppStr) ) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}
		try {
			ServiceOutputObject serviceObject;
			serviceObject = resetPasswordService.generateOtp(generateOtpRequestObject);
			response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.GENERATE_OTP_FOR_PASSWORD_RESET_SUCCESSFUL);
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException
				| CassandraInvalidQueryException | CassandraTypeMismatchException | CassandraReadTimeoutException 
				| CassandraQuerySyntaxException | CassandraInternalException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (UserNotFoundException e) {
			Logger.error("In generateOtp()- User Not Found");
			response.setResult(false, ResponseConstants.USER_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (MobileNoNotFoundException e) {
			Logger.error("In generateOtp()- Mobile No not exist for the user");
			response.setResult(false, ResponseConstants.USER_MOBILE_NO_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_MOBILE_NO_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		}  catch (JsonProcessingException e) {
			Logger.error("In generateOtp()- Error processing json data");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In generateOtp()- Error processing IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DSPException e) {
			Logger.error("In generateOtp()- DSP Exception");
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (SMSSendException e) {Logger.error("In generateOtp()- SMS Send Exception");
			response.setResult(false, ResponseConstants.SMSSEND_EXCEPTION_ERROR_CODE, null, ResponseConstants.SMSSEND_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}
	public Result resetPassword() throws JsonProcessingException {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String superAppStr =  jsonNode.findPath("super_app").asText();
		ResetPasswordRequest resetPasswordRequestObject = objectMapper.treeToValue(jsonNode, ResetPasswordRequest.class);
		String otp =  jsonNode.findPath("otp").asText();
		String newPassword =  jsonNode.findPath("new_password").asText();
		//String confirmPassword = jsonNode.findPath("confirm_password").asText();
		if ( !(validateInput(UUID.class, superAppStr) ) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}
		if(otp.isEmpty() || newPassword.isEmpty()) {
			Logger.error(" In resetPassword()- Invalid Input Exception");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}
		try {
			ServiceOutputObject serviceObject;
			serviceObject = resetPasswordService.resetPassword(resetPasswordRequestObject);
			response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.PASSWORD_RESET_SUCCESSFUL);
		} catch (UserNotFoundException e) {
			Logger.error("In resetPassword() - User Not Found");
			response.setResult(false, ResponseConstants.USER_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		}  catch (InvalidOtpException e) {
			Logger.error("In resetPassword() - Invalid OTP Exception");
			response.setResult(false, ResponseConstants.INVALID_OTP_ERROR_CODE, null, ResponseConstants.INVALID_OTP_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (OldAndNewPasswordMatchException e) {
			Logger.error("In resetPassword() - Old and New Password Match Exception");
			response.setResult(false, ResponseConstants.OLD_AND_NEW_PASSWORD_MATCH_ERROR_CODE, null, ResponseConstants.OLD_AND_NEW_PASSWORD_MATCH_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			Logger.error("In resetPassword() - Invalid Password Exception");
			response.setResult(false, ResponseConstants.PASSWORD_PATTERN_MISMATCH_ERROR_CODE, null, ResponseConstants.PASSWORD_PATTERN_MISMATCH_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DSPException e) {
			Logger.error("In resetPassword() - DSP Exception");
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error(" In resetPassword()- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(response));
	}
	public Result resetPasswordFromBackend() throws JsonProcessingException {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String superAppStr =  jsonNode.findPath("super_app").asText();
		ResetPasswordRequest resetPasswordRequestObject = objectMapper.treeToValue(jsonNode, ResetPasswordRequest.class);
		if ( !(validateInput(UUID.class, superAppStr) ) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		try {
			ServiceOutputObject serviceObject;
			serviceObject = resetPasswordService.resetPasswordOnBackendRequest(resetPasswordRequestObject);
			response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.PASSWORD_RESET_SUCCESSFUL);
		} catch (UserNotFoundException e) {
			Logger.error("In resetPassword() - User Not Found");
			response.setResult(false, ResponseConstants.USER_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		}  catch (InvalidOtpException e) {
			Logger.error("In resetPassword() - Invalid OTP Exception");
			response.setResult(false, ResponseConstants.INVALID_OTP_ERROR_CODE, null, ResponseConstants.INVALID_OTP_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (OldAndNewPasswordMatchException e) {
			Logger.error("In resetPassword() - Old and New Password Match Exception");
			response.setResult(false, ResponseConstants.OLD_AND_NEW_PASSWORD_MATCH_ERROR_CODE, null, ResponseConstants.OLD_AND_NEW_PASSWORD_MATCH_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			Logger.error("In resetPassword() - Invalid Password Exception");
			response.setResult(false, ResponseConstants.PASSWORD_PATTERN_MISMATCH_ERROR_CODE, null, ResponseConstants.PASSWORD_PATTERN_MISMATCH_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DSPException e) {
			Logger.error("In resetPassword() - DSP Exception");
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error(" In resetPassword()- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(response));
	}
	

}
