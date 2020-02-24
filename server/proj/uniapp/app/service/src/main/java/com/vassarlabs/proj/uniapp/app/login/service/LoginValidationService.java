package com.vassarlabs.proj.uniapp.app.login.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.AuthenticationException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.LoginRequestDetails;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserDetails;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.ILoginValidationService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTokenData;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.UserStates;
import com.vassarlabs.proj.uniapp.password.encrypt.decrypt.PasswordEncrypterDecrypterService;

@Component
public class LoginValidationService
	implements ILoginValidationService {

	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@Autowired UserMetaDataCrudService userMetaDataCrudService;
	@Autowired UserTokenDataCrudService tokenDataCrudService;
	@Autowired PasswordEncrypterDecrypterService passwordEncryptionDecryptionService;
	
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Override
	public ServiceOutputObject validateUser(LoginRequestDetails loginRequestDetails) 
			throws IOException, UserNotFoundException, CassandraConnectionFailureException, 
		    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, 
		    CassandraTypeMismatchException, AuthenticationException{
		
		if (loginRequestDetails == null) {
			logger.error("Invalid input - loginRequestDetails is NULL");
			return null;
		}
		
		String userId = loginRequestDetails.getUserId();
		UUID superAppId = loginRequestDetails.getSuperAppId();
		String password = loginRequestDetails.getPassword();
		UUID appId = superAppId;
		
		Map<String, Object> outputResponse = new HashMap<>();
		String userIdVal = null;
		UUID token = null;

		UserDBMetaData userDBMetaData = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);
		if (userDBMetaData == null || !userDBMetaData.isActive()) {
			logger.warn("User not found : " + superAppId + ":" + userId);
			throw new UserNotFoundException("User not found : " + superAppId + ":" + userId);
		} else {
			if (!passwordEncryptionDecryptionService.matchPassword(password, userDBMetaData.getPassword())) {
				throw new AuthenticationException("For user : " + userId);
			} else {
				userIdVal = userId;			
				UserTokenData tokenObject = insertTokenDataObject(superAppId, userId);
				token = tokenObject.getTokenId();
				long trackingTS = tokenObject.getInsertTs();
				UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.LOGIN,
						ServiceNamesConstants.LOGIN_NAME, objectMapper.writeValueAsString(loginRequestDetails), null, true, trackingTS);
				outputResponse.put("tokenid", String.valueOf(token));
			// TODO:: REMOVE
				// add departmentName To UserDetails
				String userDetailString = userDBMetaData.getUserDetails();
//				JsonNode jsonNode = objectMapper.readTree(userDetailString);
//				UserDetails userDetails  = objectMapper.treeToValue(jsonNode, UserDetails.class);
//				userDetails.setDepartment(userDBMetaData.getDepartmentName());
//				userDetailString = objectMapper.writeValueAsString(userDetails);
				
				outputResponse.put("userdetails",userDetailString);
				if(userIdVal != null) {
					outputResponse.put("userid", String.valueOf(userIdVal));
				}
				outputResponse.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
				ServiceOutputObject outputObject = new ServiceOutputObject(outputResponse, trackingObject, true);
				return outputObject;
			}
		}
	}

	private UserTokenData insertTokenDataObject(UUID superAppId, String userId) {
		
		UserTokenData tokenData = new UserTokenData();
		tokenData.setSuperAppId(superAppId);
		tokenData.setTokenId(UUIDUtils.getTrueTimeUUID());
		tokenData.setTokenExpired(0);
		tokenData.setInsertTs(System.currentTimeMillis());
		tokenData.setSyncTs(tokenData.getInsertTs());
		tokenData.setUserId(userId);
		tokenDataCrudService.insertUserTokenData(tokenData);
		return tokenData;
	}

}
