package com.vassarlabs.proj.uniapp.app.login.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidOtpException;
import com.vassarlabs.common.utils.err.InvalidPasswordException;
import com.vassarlabs.common.utils.err.MobileNoNotFoundException;
import com.vassarlabs.common.utils.err.OldAndNewPasswordMatchException;
import com.vassarlabs.common.utils.err.SMSSendException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.prod.sms.service.impl.SMSServiceImpl;
import com.vassarlabs.proj.uniapp.api.pojo.GenerateOtpRequest;
import com.vassarlabs.proj.uniapp.api.pojo.OtpObject;
import com.vassarlabs.proj.uniapp.api.pojo.ResetPasswordRequest;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserData;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.kafkaqueue.service.QueueUserDataReceiverService;
import com.vassarlabs.proj.uniapp.app.kafkaqueue.service.SendToQueueUserDataService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTokenData;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.UserStates;
import com.vassarlabs.proj.uniapp.password.encrypt.decrypt.PasswordEncrypterDecrypterService;

@Component
public class ResetPasswordService {
	@Autowired UserMetaDataCrudService userMetaDataCrudService;
	@Autowired  ApplicationProperties properties;
	@Autowired PasswordEncrypterDecrypterService passwordEncryptionDecryptionService;
	@Autowired UserTokenDataCrudService userTokenDataCrudService;
	@Autowired  SMSServiceImpl smsService;
	@Autowired QueueUserDataReceiverService queueUserDataReceiverService;
	ObjectMapper objectMapper = new ObjectMapper();

	public ServiceOutputObject resetPassword(ResetPasswordRequest requestObject )
			throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, UserNotFoundException, JsonParseException, JsonMappingException, 
			IOException, InvalidOtpException, InvalidPasswordException, DSPException, OldAndNewPasswordMatchException{
		if(requestObject == null) {
			return null;
		}

		String userId = requestObject.getUserId();
		UUID superAppId = requestObject.getSuperAppId();
		int otp = requestObject.getOtp();
		String newPassword = requestObject.getNewPassword();
		UUID appId = superAppId;
		UUID tokenId = UUIDUtils.getDefaultUUID();

		UserDBMetaData userDBMetaData = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);

		if(userDBMetaData == null || !userDBMetaData.isActive()) {
			IErrorObject errorObject = new ErrorObject("InvalidUser", IErrorObject.INVALID_USER, "UserId not found", 0);
			throw new UserNotFoundException(errorObject);
		} else {

			String otpExpirationInterval = properties.getProperty(BackendPropertiesConstants.OTP_EXPIRATION_INTERVAL);
			if(userDBMetaData.getOtpObject() == null || userDBMetaData.getOtpObject().isEmpty()) {
				throw new InvalidOtpException("OTP entered is invalid or expired.");
			}

			OtpObject otpObject = objectMapper.readValue(userDBMetaData.getOtpObject(), new TypeReference<OtpObject>(){});

			if(otpObject.getOtp() != (otp) || (System.currentTimeMillis() - otpObject.getTimestamp()) > Long.parseLong(otpExpirationInterval)) {
				throw new InvalidOtpException("OTP entered is invalid or expired.");
			}

			if(passwordEncryptionDecryptionService.matchPassword(newPassword, userDBMetaData.getPassword())) {
				throw new OldAndNewPasswordMatchException("New password and old password cannot be same.");
			} else if(!newPassword.matches(CommonConstants.PASSWORD_PATTERN)) {
				throw new InvalidPasswordException("Password must contain atleast 6 characters including upper, lowercase, alphabet, number and special character ");
			} else {
				if(userMetaDataCrudService.resetPassword(superAppId, userId,userDBMetaData.getUserExtId(), passwordEncryptionDecryptionService.getEncryptedPassword(newPassword))) {
					UserData userDataObject = new UserData();
					userDataObject.setSuperAppId(superAppId);
					userDataObject.setUserId(userId);
					userDataObject.setPassword(newPassword);
					userDataObject.setInsertTs(System.currentTimeMillis());
					queueUserDataReceiverService.execute(userDataObject);
				} else {
					throw new DSPException("Password reset failed");
				}
				List<UserTokenData> tokens = userTokenDataCrudService.findByPartitionKey(superAppId, userId);
				List<UserTokenData> activeTokens = new ArrayList<UserTokenData>();
				for(UserTokenData token : tokens) {
					if(token != null && token.getTokenExpired() == 0) {
						token.setTokenExpired(1);
						token.setSyncTs(System.currentTimeMillis());					
						activeTokens.add(token);
					}
				}
				if(activeTokens != null && !activeTokens.isEmpty()) {
					
					userTokenDataCrudService.insertUserTokenData(activeTokens);
				}
				long trackingTS = System.currentTimeMillis();
				UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, tokenId, APITypes.RESET_PASSWORD,
						"Reset Password", CommonConstants.REQUEST_TYPE_POST, null, true, trackingTS);
				ServiceOutputObject outputObject = new ServiceOutputObject();
				outputObject.setSuccessful(true);
				outputObject.setTrackingObject(trackingObject);
				return outputObject;

			}
		}

	}

	public ServiceOutputObject generateOtp(GenerateOtpRequest requestObject) throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraWriteTimeoutException, 
	CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, UserNotFoundException, JsonParseException, JsonMappingException, IOException,
	DSPException, SMSSendException, MobileNoNotFoundException {

		String smsCountryUsername = properties.getProperty(BackendPropertiesConstants.SMS_COUNTRY_USERNAME);
		String smsCountryPassword = properties.getProperty(BackendPropertiesConstants.SMS_COUNTRY_PASSWORD);
		String smsCountryUrl = properties.getProperty(BackendPropertiesConstants.SMS_COUNTRY_BASE_URL);
		if(requestObject == null) {
			return null;
		}

		String userId = requestObject.getUserId();
		UUID superAppId = requestObject.getSuperAppId();
		UUID appId = superAppId;
		UUID tokenId = UUIDUtils.getDefaultUUID();

		UserDBMetaData userDBMetaData = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);
		String otpExpirationInterval = properties.getProperty(BackendPropertiesConstants.OTP_EXPIRATION_INTERVAL);
		
		if(userDBMetaData == null || !userDBMetaData.isActive()) {
			IErrorObject errorObject = new ErrorObject("InvalidUser", IErrorObject.INVALID_USER, "UserId not found", 0);
			throw new UserNotFoundException(errorObject);
		} else {
			Long mobileNo = userDBMetaData.getMobileNumber();

			if(mobileNo == null || !String.valueOf(mobileNo).matches(CommonConstants.MOBILE_NIMBER_PATTERN)) {
				throw new MobileNoNotFoundException("Mobile no is not registered with your account");
			} else {
				OtpObject otpObject = new OtpObject();
				Boolean createNewOtp = true;
				
				if(userDBMetaData.getOtpObject() != null && !userDBMetaData.getOtpObject().isEmpty()) {
					otpObject = objectMapper.readValue(userDBMetaData.getOtpObject(), new TypeReference<OtpObject>(){});
					if((System.currentTimeMillis() - otpObject.getTimestamp()) < Long.parseLong(otpExpirationInterval)) {
						createNewOtp = false;
					}
				}
				if(createNewOtp) {
					Random rnd = new Random();
					int otp = 10000 + rnd.nextInt(90000);
					otpObject = new OtpObject();
					otpObject.setOtp(otp);
					long timestamp = System.currentTimeMillis();
					otpObject.setTimestamp(timestamp); 
				}
				Long ts  = otpObject.getTimestamp( )+ Long.parseLong(otpExpirationInterval) ; 
				String otpExpirationDate = DateUtils.getDateInFormat("HH:mm",ts);
				String otpObjectString = objectMapper.writeValueAsString(otpObject);
				String OtpMessage = String.valueOf(otpObject.getOtp()) + CommonConstants.RESET_PASSWORD_OTP_MESSAGE + otpExpirationDate;
				if(createNewOtp && !userMetaDataCrudService.setOtpObject(superAppId, userId,userDBMetaData.getUserExtId(), otpObjectString)) {
					throw new DSPException("OTP generation failed.");
				} else {
					smsService.sendMessage(smsCountryUrl, smsCountryUsername, smsCountryPassword, mobileNo, OtpMessage);
					long trackingTS = System.currentTimeMillis();
					UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, tokenId, APITypes.GENERATE_OTP,
							"Generate OTP", CommonConstants.REQUEST_TYPE_POST, null, true, trackingTS);
					ServiceOutputObject outputObject = new ServiceOutputObject();
					outputObject.setSuccessful(true);
					outputObject.setTrackingObject(trackingObject);
					return outputObject;
				}
			}
		}
	}
	public ServiceOutputObject resetPasswordOnBackendRequest(ResetPasswordRequest requestObject )
			throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, UserNotFoundException, JsonParseException, JsonMappingException, 
			IOException, InvalidOtpException, InvalidPasswordException, DSPException, OldAndNewPasswordMatchException{
		if(requestObject == null) {
			return null;
		}

		String userId = requestObject.getUserId();
		UUID superAppId = requestObject.getSuperAppId();
		String newPassword = requestObject.getNewPassword();
		UUID appId = superAppId;
		UUID tokenId = UUIDUtils.getDefaultUUID();

		UserDBMetaData userDBMetaData = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);

		if(userDBMetaData == null || !userDBMetaData.isActive()) {
			IErrorObject errorObject = new ErrorObject("InvalidUser", IErrorObject.INVALID_USER, "UserId not found", 0);
			throw new UserNotFoundException(errorObject);
		} else {

			if(!userMetaDataCrudService.resetPassword(superAppId, userId,userDBMetaData.getUserExtId(), passwordEncryptionDecryptionService.getEncryptedPassword(newPassword))) {
				throw new DSPException("Password reset failed");
			}
			List<UserTokenData> tokens = userTokenDataCrudService.findByPartitionKey(superAppId, userId);
			List<UserTokenData> activeTokens = new ArrayList<UserTokenData>();
			for(UserTokenData token : tokens) {
				if(token.getTokenExpired() == 0) {
					token.setTokenExpired(1);
					token.setSyncTs(System.currentTimeMillis());					
					activeTokens.add(token);
				}
			}
			if(activeTokens != null && !activeTokens.isEmpty()) {
				userTokenDataCrudService.insertUserTokenData(activeTokens);
			}
			long trackingTS = System.currentTimeMillis();
			UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, tokenId, APITypes.RESET_PASSWORD,
					"Reset Password", CommonConstants.REQUEST_TYPE_POST, null, true, trackingTS);
			ServiceOutputObject outputObject = new ServiceOutputObject();
			outputObject.setSuccessful(true);
			outputObject.setTrackingObject(trackingObject);
			return outputObject;

		}

	}

}
