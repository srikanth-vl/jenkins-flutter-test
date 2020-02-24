package com.vassarlabs.proj.uniapp.app.data.insertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.exceptions.OperationTimedOutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.UserDetails;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.enums.UserStates;
import com.vassarlabs.proj.uniapp.password.encrypt.decrypt.PasswordEncrypterDecrypterService;
import com.vassarlabs.proj.uniapp.upload.pojo.UserMetaDataList;
import com.vassarlabs.proj.uniapp.upload.pojo.UserMetaDataRequest;

@Service
public class RESTUserMetaDataUpload {

	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired protected IVLLogService logFactory;
	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	public static final String DEFAULT_PASSWORD = "vassar";
	
	@Autowired private PasswordEncrypterDecrypterService passwordEncryptDecryptService;
	@Autowired private UserMetaDataCrudService userDataCrudService;

	public Map<String, List<IErrorObject>> insertUserMetaData(UserMetaDataRequest userMetaDataRequest) throws JsonProcessingException, InvalidInputException, DSPException, InterruptedException {

		Map<String, List<IErrorObject>> externalIdToErrorList = new HashMap<>();
		List<UserMetaDataList> userMetaDataList = userMetaDataRequest.getUserMetaData();
		int retryCount = 0;
			try {
			if(userMetaDataList == null || userMetaDataList.isEmpty()) {
				logger.error("User meta data list is null or empty"); 
				throw new InvalidInputException("User meta data list is null or empty");
			}
			List<UserDBMetaData> usersExistingDataList = userDataCrudService.findUserDataByPartitionKey(userMetaDataRequest.getSuperAppId(), UserStates.ACTIVE);
			Map<String, UserDBMetaData> usersExistingData = new HashMap<>();
			for(UserDBMetaData userMetaData : usersExistingDataList) {
				usersExistingData.put(userMetaData.getUserId(), userMetaData);
			}
			
			List<UserDBMetaData> insertList = new ArrayList<>();
			
			for(UserMetaDataList userMetaData : userMetaDataList) {
	
				List<IErrorObject> errorList = new ArrayList<>();
				if(userMetaData.getUserId() == null 
						|| userMetaData.getUserId().isEmpty()) {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.INVALID_USER, "UserID cannot be null or empty",  IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					externalIdToErrorList.put(CommonConstants.DEFAULT_KEY, errorList);
				}
				
				if(userMetaData.getUserExternalId() == null 
						|| userMetaData.getUserExternalId().isEmpty()) {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.INVALID_USER, "User External ID cannot be null or empty",  IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					externalIdToErrorList.put(CommonConstants.DEFAULT_KEY, errorList);
				}
				if(userMetaData.getAppActions() == null 
						|| userMetaData.getAppActions().isEmpty()) {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.EMPTY_ERROR_CODE, "User Actions cannot be null or empty",  IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					externalIdToErrorList.put(CommonConstants.DEFAULT_KEY, errorList);
				}
				UserDBMetaData existingdata = null;
				if(userMetaData.getUserId() != null) {
					existingdata = usersExistingData.get(userMetaData.getUserId());
				}
				UserDBMetaData userDBMetaData = new UserDBMetaData();
				
				long mobileNumber = validateMobileNumber(String.valueOf(userMetaData.getMobileNumber()));
				if(mobileNumber == 0) {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "Invalid Mobile Number obtained - " + userMetaData.getMobileNumber(), IErrorObject.ROW_PARTIAL_UPLOADED_MESSAGE));
					logger.error("Invalid Mobile Number obtained - " + userMetaData.getMobileNumber());
				}
				String emailId = getEmailId(userMetaData.getEmailId());
				if(emailId == null) {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "Invalid Email ID obtained - " + userMetaData.getEmailId(),  IErrorObject.ROW_PARTIAL_UPLOADED_MESSAGE));				
					logger.error("Invalid Email ID obtained - " + userMetaData.getEmailId());
				}
				userDBMetaData.setSuperAppId(userMetaDataRequest.getSuperAppId());
				userDBMetaData.setUserExtId(userMetaData.getUserExternalId());
				userDBMetaData.setUserId(userMetaData.getUserId());
				userDBMetaData.setDepartmentName(userMetaData.getDepartment());
				userDBMetaData.setMobileNumber(mobileNumber);
				String password;
				if(userMetaData.getPassword() == null || userMetaData.getPassword().isEmpty()) {
					password = DEFAULT_PASSWORD;
				} else {
					password = userMetaData.getPassword();
				}
				userDBMetaData.setPassword(passwordEncryptDecryptService.getEncryptedPassword(password));
				UserDetails userDetails = new UserDetails(userMetaData.getUserName(), userMetaData.getDesignation(), emailId, mobileNumber, userMetaData.getZone());
				userDBMetaData.setUserDetails(objectMapper.writeValueAsString(getUserDetailsMap(userDetails, userMetaData.getAdditionalProperties())));
				Map<UUID, String> appActions = getAppActions(existingdata, userMetaData);
				userDBMetaData.setAppActions(appActions);
				Map<UUID, String> mapUrls = getMapUrls(existingdata, userMetaData);
				userDBMetaData.setMapFileUrls(mapUrls);
				userDBMetaData.setActive(true);
				userDBMetaData.setInsertTs(System.currentTimeMillis());
				if(!errorList.isEmpty()) {
					String userId = userMetaData.getUserId();
					if(userId == null) {
						userId = CommonConstants.DEFAULT_KEY;
					}
					externalIdToErrorList.put(userId, errorList);
				}
				boolean result = errorList.stream().anyMatch(x->x.getRowUploadStatus().equals(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				if(!result)
					insertList.add(userDBMetaData);
			}
			userDataCrudService.insertUserMetaData(insertList);
		} catch(CassandraReadTimeoutException | CassandraWriteTimeoutException | OperationTimedOutException | CassandraConnectionFailureException e) {
			if(retryCount++ >= CommonConstants.MAX_RETRIES) {
				logger.error("Max retries reached... Could not insert the data");
				throw new DSPException();
			}
			logger.debug("Sleeping for 3 seconds.. before retrying for " + retryCount + " time....");
			Thread.sleep(3000);
		}
		return externalIdToErrorList;
	}
	
	private String getEmailId(String value) {
		if(value == null || value.isEmpty()) {
			return null;
		}
		if(!value.contains("@"))
			return null;
		return value;
	}
	
	private long validateMobileNumber(String mobileNumber) {
		if(mobileNumber == null || mobileNumber.isEmpty()) {
			return 0;
		}
		if(mobileNumber.matches("[0-9]+")) {
			if(mobileNumber.length() == 10) {
				return Long.parseLong(mobileNumber);
			}
		}
		return 0;
	}
	private Map<UUID, String> getAppActions(UserDBMetaData existingdata, UserMetaDataList userMetaData) {
		Map<UUID, String> newAppActions = userMetaData.getAppActions();
		if(existingdata != null) {
			Map<UUID, String> existingAppActions = existingdata.getAppActions();
			if( existingAppActions!= null) {
				for(UUID appId: newAppActions.keySet()) {
					if(existingAppActions.containsKey(appId)) {
						List<String> actionList = StringUtils.getStringListFromDelimitter(CommonConstants.DELIMITER, existingAppActions.get(appId) + CommonConstants.DELIMITER + newAppActions.get(appId));
						String actions = removeDuplicateActions(actionList);
						existingAppActions.put(appId, actions);
					} else {
						existingAppActions.put(appId, newAppActions.get(appId));
					}
				}
			}
			if(existingAppActions == null) {
				existingAppActions = new HashMap<>();
			}
			return existingAppActions;
		}
		if(newAppActions == null) {
			newAppActions = new HashMap<>();
		}
		return newAppActions;
	}
	
	public Map<UUID, String> getMapUrls(UserDBMetaData existingdata, UserMetaDataList userMetaData) {
		Map<UUID, String> updatedMapUrls = new HashMap<UUID, String>();
		String newMapUrlString = userMetaData != null  && userMetaData.getMapUrlsString() != null ? userMetaData.getMapUrlsString() : "";
		String[] newMapUrls = newMapUrlString.split(",");
		if(existingdata != null) {
			Map<UUID, String> existingMapUrls = existingdata.getMapFileUrls();
			if( existingMapUrls!= null && existingMapUrls.get(UUIDUtils.getDefaultUUID()) != null && !existingMapUrls.get(UUIDUtils.getDefaultUUID()).isEmpty()) {
				String existingMapUrlString = existingMapUrls.get(UUIDUtils.getDefaultUUID());
				String[] oldMapUrls = existingMapUrlString.split(",");
				List<String> oldMapUrlsList = Arrays.stream(oldMapUrls).collect(Collectors.toList());
				List<String> newMapUrlList = new ArrayList<String>();
				for(String mapUrl: newMapUrls) {
					if(!oldMapUrlsList.contains(mapUrl)) {
						newMapUrlList.add(mapUrl);
					}
				}
				String mapUrlString = String.join(",", newMapUrlList);
				if(!newMapUrlList.isEmpty()) {
					newMapUrlString = existingMapUrlString + "," + mapUrlString;
				} else {
					newMapUrlString = existingMapUrlString;
				}
			}
		} 
		if (!newMapUrlString.isEmpty()) {
			updatedMapUrls.put(UUIDUtils.getDefaultUUID(), newMapUrlString);
		}
		return updatedMapUrls;
	}
	
	private String removeDuplicateActions(List<String> actionList) {
		String actionString  = "";
		List<String> actionset = new ArrayList<String> ();
		actionList.forEach(a -> {
			if(!actionset.contains(a)) {
				actionset.add(a);
			}
		});
		actionString = StringUtils.getconcatenatedStringFromStringList(CommonConstants.DELIMITER, actionset);
		return actionString;
	}
	public Map<String, String> getUserDetailsMap(UserDetails userDetails, Map<String, String> additionalProperties) {
		if(additionalProperties == null ) {
			additionalProperties = new HashMap<>();
		}
		additionalProperties.put("name", userDetails.getUserName());
		additionalProperties.put("designation", userDetails.getDesignation());
		additionalProperties.put("email", userDetails.getEmailId());
		additionalProperties.put("zone", userDetails.getZone());
		additionalProperties.put("mobile", String.valueOf(userDetails.getMobileNumber()));
		return additionalProperties;
		
		
	}
}
