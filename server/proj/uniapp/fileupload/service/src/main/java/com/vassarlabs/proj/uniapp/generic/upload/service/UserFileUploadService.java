package com.vassarlabs.proj.uniapp.generic.upload.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.filereader.service.api.ICSVFileReaderService;
import com.vassarlabs.common.fileupload.event.api.IFileUploadEvent;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;
import com.vassarlabs.common.fileupload.pojo.impl.FileUploadResult;
import com.vassarlabs.common.filewriter.pojo.api.IFileWriterDetails;
import com.vassarlabs.common.filewriter.service.api.ICSVFileWriterService;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.ObjectNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.UserDetails;
import com.vassarlabs.proj.uniapp.app.data.deletion.UserDeletionService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;
import com.vassarlabs.proj.uniapp.enums.UserStates;
import com.vassarlabs.proj.uniapp.password.encrypt.decrypt.PasswordEncrypterDecrypterService;

@Component
public class UserFileUploadService
	extends UniAppFileUpload  {

	@Autowired private ICSVFileReaderService fileReaderService;
	@Autowired private ICSVFileWriterService fileWriterService;
	@Autowired private UserMetaDataCrudService userDataCrudService;
	
	@Autowired private UserDeletionService userDeletionService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired protected IVLLogService logFactory;
	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Autowired 
	PasswordEncrypterDecrypterService passwordEncryptDecryptService;
	
	@EventListener
	public <E> void fileUploadEvent(IFileUploadEvent<E> fileUploadEvent) throws DSPException, ObjectNotFoundException, JsonProcessingException, InvalidInputException {
		if(fileUploadEvent.getFileUploadDetails().getClassName().contains(CommonConstants.USER_DATA_CLASSNAME)) {
			try {
				logger.debug("Starting user details file upload");
				Map<E, List<IErrorObject>> rowToErrorListMap = fileUploadEvent.getFileUploadResult().getDataToErrorListMap();
				List<E> list = fileUploadEvent.getDataList();
				List<UserDBMetaData> userDataList = new ArrayList<>();
				Properties properties = fileUploadEvent.getFileUploadDetails().getProperties();
				String JsonConfiguration = properties.getProperty(CommonConstants.JSON_CONFIG);
				JsonNode node = objectMapper.readTree(JsonConfiguration);
		
				for(E object : list ) {
					List<IErrorObject> errors = populateUserData(object, userDataList, node);
					if(!errors.isEmpty()) {
						rowToErrorListMap.put(object, errors);
					} 
				}
				Map<String, UserDBMetaData> usersExistingData = userDataCrudService.findUserDataByPartitionKey(userDataList.get(0).getSuperAppId(), UserStates.INACTIVE).stream().collect(Collectors.toMap(UserDBMetaData::getUserId, Function.identity()));;
				for(UserDBMetaData userData: userDataList) {
					UserDBMetaData existingdata = usersExistingData.get(userData.getUserId());
					if(existingdata != null) {
						Map<UUID, String> newAppActions = userData.getAppActions();
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
							userData.setAppActions(existingAppActions);
						}
					}
				}
				userDataCrudService.insertUserMetaData(userDataList);
				if(!rowToErrorListMap.isEmpty()) {
					IFileWriterDetails fileWriterDetails = createFileWriterObject(fileUploadEvent);
					fileWriterService.writeCSVFile(fileWriterDetails, rowToErrorListMap);
				}
				consolidateCountOfRecords(rowToErrorListMap.size(),userDataList.size(), list.size(), fileUploadEvent.getFileUploadDetails(), fileUploadEvent.getFileUploadResult());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void uploadFile(IFileUploadDetails fileDetails)
			throws  InvalidInputException, IOException {
		Properties properties = fileDetails.getProperties();
		String JsonConfiguration = properties.getProperty(CommonConstants.JSON_CONFIG);
		JsonNode node = objectMapper.readTree(JsonConfiguration);
		String superAppIdStr = node.get(IFileUploadConstants.SUPERAPPID).asText();
		IFileUploadResult<?> fileUploadResult = new FileUploadResult<>();
		logger.debug("Starting Upload for file - " + fileDetails.getFileName());
		long startTS = System.currentTimeMillis();
		processQueryForUploadType(fileDetails.getProperties());
		fileReaderService.readCSVFile(fileDetails, fileUploadResult);
		printResult(fileUploadResult);
		addDefaultUser(superAppIdStr);
		logger.debug("Total Time taken for uploading -" + fileDetails.getFileName() + " : " + (System.currentTimeMillis() - startTS));
	}

	private List<IErrorObject> populateUserData(Object userDataInfo, List<UserDBMetaData> userDataList, JsonNode node) 
			throws IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException, InvalidInputException {
		
		List<IErrorObject> errorList = new ArrayList<>();
		UserDBMetaData userData = new UserDBMetaData();
		UserDetails userDetails = new UserDetails();
		Map<String, String> additionalPropertiesKeyToAttributeMap =  new HashMap<>();
		Map<UUID, String> appIdToActionsMap = new HashMap<>();
		Map<String, String> additionalPropertiesAttributeToValueMap = new HashMap<>();
		Map<String, UUID> appNamesToIdMap = new HashMap<>();
		JsonNode userDetailsNode = node.findValue(IFileUploadConstants.UserFileUploadConfigConstants.USER_DETAILS);
		JsonNode jsonNode = node.get(IFileUploadConstants.UserFileUploadConfigConstants.APP_ACTIONS);
		JsonNode additionalPropertiesNode = node.get(IFileUploadConstants.UserFileUploadConfigConstants.ADDITIONAL_PROPERTIES);
		Iterator<JsonNode> fieldNames = jsonNode.iterator();
		while(fieldNames.hasNext()) {
			JsonNode appNodes = fieldNames.next();
			String appIdStr = appNodes.get(IFileUploadConstants.UserFileUploadConfigConstants.VALUE).asText();
			if(appIdStr != null && !appIdStr.isEmpty()) {
				appIdToActionsMap.put(UUIDUtils.toUUID(appIdStr), appNodes.get(IFileUploadConstants.UserFileUploadConfigConstants.DEFAULT).asText());
				appNamesToIdMap.put(appNodes.get(IFileUploadConstants.UserFileUploadConfigConstants.KEY).asText(), UUIDUtils.toUUID(appIdStr));
			} else {
				logger.error("No app actions association information found");
				throw new InvalidInputException();
			}
		}
		Map<String, String> dbNameToFileNameMap = generateDBNameToFileNameMap(node, userDetailsNode, appNamesToIdMap,additionalPropertiesNode, additionalPropertiesKeyToAttributeMap);
		
		for (Field f : userDataInfo.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if(!(dbNameToFileNameMap.values().contains(f.getName()) || additionalPropertiesKeyToAttributeMap.containsKey(f.getName()))) {
				continue;
			}
			setValue(userData, userDetails, f.get(userDataInfo), dbNameToFileNameMap, f.getName(), appIdToActionsMap, appNamesToIdMap, errorList, additionalPropertiesKeyToAttributeMap, additionalPropertiesAttributeToValueMap);
		}
		String userDetailsStr = objectMapper.writeValueAsString(getUserDetailsMap(userDetails, additionalPropertiesAttributeToValueMap));
		userData.setUserDetails(userDetailsStr);
		if(userData.getPassword() == null) {
			userData.setPassword(passwordEncryptDecryptService.getEncryptedPassword(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.DEFAULT_PASSWORD)));
		}
		String superAppIdStr = node.get(IFileUploadConstants.SUPERAPPID).asText();
		UUID superAppId = UUIDUtils.toUUID(superAppIdStr);
		userData.setSuperAppId(superAppId);
		userData.setInsertTs(System.currentTimeMillis());
		userData.setActive(true);
		validateKeyFields(userData, errorList);
		
		boolean result = errorList.stream().anyMatch(x->x.getRowUploadStatus().equals(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
		if(!result) {
			Iterator<Entry<UUID, String>> iterator = userData.getAppActions().entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<UUID, String> element = iterator.next();
				if(element.getValue() == null || element.getValue().isEmpty() || element.getValue().equals("null")) {
					iterator.remove();
				}
			}
			userDataList.add(userData);
		}
		return errorList;
	}

	private List<IErrorObject> setValue(UserDBMetaData userData, UserDetails userDetails, Object object, Map<String, String> dbNameToFileNameMap, String key, Map<UUID, String> appIdToActionsMap, 
			Map<String, UUID> appNamesToIdMap, List<IErrorObject> errorList, Map<String, String> additionalPropertiesKeyToAttributeMap, Map<String, String> additionalProperties) {
		
		if(object != null) {
			String value = String.valueOf(object);
			if(key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.USER_EXT_ID))) {
				userData.setUserExtId(String.valueOf(value));
			} if(key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.USER_ID))) {
				userData.setUserId(String.valueOf(value));
			} if(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.PASSWORD) != null
					&& key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.PASSWORD))) {
				userData.setPassword(passwordEncryptDecryptService.getEncryptedPassword(value));
			} if(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.USER_NAME) != null 
					&& key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.USER_NAME))) {
				userDetails.setUserName(value);
			} if(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.MOBILE_NUMBER) != null
					&& key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.MOBILE_NUMBER))) {
				if(validateMobileNumber(value)) {
					userDetails.setMobileNumber(Long.parseLong(value));
					userData.setMobileNumber(Long.parseLong(value));
				} else {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "Invalid Mobile Number obtained - " + value, IErrorObject.ROW_PARTIAL_UPLOADED_MESSAGE));
					logger.error("Invalid Mobile Number obtained - " + value);
				}
			} if(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.EMAIL_ID) != null
					&& key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.EMAIL_ID))) {
				if(validateEmail(value)) {
					userDetails.setEmailId(value);
				} else {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "Invalid Email ID obtained - " + value,  IErrorObject.ROW_PARTIAL_UPLOADED_MESSAGE));
					logger.error("Invalid Email ID obtained - " + value);
				}
			} if(key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.DESIGNATION))) {
				userDetails.setDesignation(value);
			} if(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.ZONE) != null
					&& key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.ZONE))) {
				userDetails.setZone(value);
			} if(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.OTP_OBJECT) != null
					&& key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.OTP_OBJECT))) {
				userData.setOtpObject(value);
			} if(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.DEPT_NAME) != null 
					&& key.equalsIgnoreCase(dbNameToFileNameMap.get(IFileUploadConstants.UserFileUploadConfigConstants.DEPT_NAME))) {
				userData.setDepartmentName(value);
			} if(appNamesToIdMap.containsKey(key)) {
				UUID keyUUID = appNamesToIdMap.get(key);
				if(value != "null"
						&& !value.isEmpty()) {
					appIdToActionsMap.put(keyUUID, value);
				} else {
					appIdToActionsMap.remove(keyUUID);
				}
			}
			if(additionalPropertiesKeyToAttributeMap.containsKey(key)) {
				if(value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
					additionalProperties.put(additionalPropertiesKeyToAttributeMap.get(key), value);
				}
				
			}
		}
		userData.setAppActions(appIdToActionsMap);
		return errorList;
	}

	private void validateKeyFields(UserDBMetaData userData, List<IErrorObject> errorList) {
		if(userData.getUserId() == null
				|| userData.getUserId().isEmpty()) {
			errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.INVALID_USER, "UserID cannot be Null or Empty",  IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
		}
	}

	private Map<String, String> generateDBNameToFileNameMap(JsonNode node, JsonNode userDetailsNode, Map<String, UUID> appNamesToIdMap, JsonNode additionalProperties, Map<String, String> additionalPropertiesKeyToAttributeMap) {
		Map<String, String> dbToFileNameMap = new HashMap<>();
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.USER_ID, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.USER_ID, node));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.USER_EXT_ID, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.USER_EXT_ID, node));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.PASSWORD, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.PASSWORD, node));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.OTP_OBJECT, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.OTP_OBJECT, node));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.USER_NAME, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.USER_NAME, userDetailsNode));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.DESIGNATION, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.DESIGNATION, userDetailsNode));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.ZONE, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.ZONE, userDetailsNode));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.EMAIL_ID, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.EMAIL_ID, userDetailsNode));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.MOBILE_NUMBER, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.MOBILE_NUMBER, userDetailsNode));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.DEPT_NAME, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.DEPT_NAME, node));
		dbToFileNameMap.put(IFileUploadConstants.UserFileUploadConfigConstants.DEFAULT_PASSWORD, getTextValueFromJsonNode(IFileUploadConstants.UserFileUploadConfigConstants.DEFAULT_PASSWORD, node));

		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> result = mapper.convertValue(additionalProperties, Map.class);
		for (String attribute : result.keySet()) {
			additionalPropertiesKeyToAttributeMap.put(result.get(attribute), attribute);
		}
//		System.out.println(additionalPropertiesKeyToAttributeMap);
		for(String key : appNamesToIdMap.keySet()) {
			dbToFileNameMap.put(key, key);
		}
		return dbToFileNameMap;
	}

	private void processQueryForUploadType(Properties properties) throws InvalidInputException, IOException {
		String jsonConfigStr = properties.getProperty(CommonConstants.JSON_CONFIG);
		String uploadType = getUploadType(jsonConfigStr);
		if(uploadType == null) {
			uploadType = CommonConstants.DEFAULT_UPLOAD_TYPE;
		}
		if(!CommonConstants.getAllUploadTypes().contains(uploadType)) {
			logger.error("Invalid File Upload Type found");
			throw new InvalidInputException();
		}
		if(uploadType.equals(CommonConstants.DELETE_INSERT_UPLOAD)) {
			disableAllUsersOfASuperApp(getSuperAppId(jsonConfigStr));
		}		
	}
	
	private void addDefaultUser(String superAppIdStr) {
		UUID superAppId = UUIDUtils.toUUID(superAppIdStr);
		String defaultUserId = CommonConstants.DEFAULT_USER_ID;
		UserDBMetaData defaultUser = userDataCrudService.findUserDataByUserIdKey(superAppId, defaultUserId, UserStates.INACTIVE);
		if(defaultUser == null) {
			logger.info("In UserFileUplaodService - Default User not found" + " for superAppId -" + superAppIdStr);
			defaultUser = new UserDBMetaData();
			defaultUser.setUserId(defaultUserId);
			defaultUser.setActive(true);
			defaultUser.setUserExtId(defaultUserId);
			defaultUser.setSuperAppId(superAppId);
			defaultUser.setInsertTs(System.currentTimeMillis());
			userDataCrudService.insertUserMetaData(defaultUser);
			logger.info("In UserFileUplaodService - Inserted Default User" + " for superAppId -" + superAppIdStr);
		}
		
	}
	
	private void disableAllUsersOfASuperApp(UUID superAppId) {
		List<UserDBMetaData> userDBDetailsData = userDataCrudService.findUserDataByPartitionKey(superAppId, UserStates.INACTIVE);
		userDeletionService.disableAllUsersOfASuperApp(superAppId, userDBDetailsData);
//		for (UserDBMetaData user : userDBDetailsData) {
//			if(!user.getUserId().equals(CommonConstants.DEFAULT_USER_ID)) {
//				user.setActive(false);
//				user.setInsertTs(System.currentTimeMillis());
//				userDataCrudService.insertUserMetaData(user);
//				if(user.getUserId() != null) {
//					userTokenCrudService.expireAllTokenForAUser(superAppId, user.getUserId());
//					if(user.getAppActions() != null && !user.getAppActions().isEmpty()) {
//						List<UUID> appIds = new ArrayList<UUID>();
//						appIds.addAll(user.getAppActions().keySet());
//						userProjectMapCrudService.deleteAllRecordsForAUser(superAppId, appIds, user.getUserId()); 
//					}
//				}
//			}
//		}
	}
	
	/*public void refreshUserProjectMapping(Properties properties) throws IOException {
		String jsonStringNode = properties.getProperty(CommonConstants.JSON_CONFIG);
		JsonNode node = objectMapper.readTree(jsonStringNode);
		UUID superAppId = getSuperAppId(jsonStringNode);
		JsonNode jsonNode = node.get(IFileUploadConstants.UserFileUploadConfigConstants.APP_ACTIONS);
		Iterator<JsonNode> fieldNames = jsonNode.iterator();
		List<UUID> appIds = new ArrayList<UUID>();
		while(fieldNames.hasNext()) {
			JsonNode appNodes = fieldNames.next();
			String appIdStr = appNodes.get(IFileUploadConstants.UserFileUploadConfigConstants.VALUE).asText();
			if(appIdStr != null && !appIdStr.isEmpty()) {
				appIds.add(UUIDUtils.toUUID(appIdStr));	
			}
		}
		if(appIds.size() > 0) {
			for (UUID appId: appIds) {
				refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId); 
			} 
		}
	}*/
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
