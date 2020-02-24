package com.vassarlabs.proj.uniapp.generic.upload.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants.UserProjectMappingUploadConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class UserProjectMapUtils
extends UniAppFileUpload {

	@Autowired UserProjectMapCrudService userProjectMappingCrudService;
	@Autowired UserMetaDataCrudService userMetaDataCrudService;
	@Autowired ProjectMasterDataCrudService masterDataCrudService;
	ObjectMapper objectMapper = new ObjectMapper();

	public List<IErrorObject> populateUserProjectMappingData(Object mappingObject, Map<String, Map<Integer, UserProjectMapping>> userProjectMappingMap,
			JsonNode userMappingNode, UUID superAppId, UUID appId, Map<String, ProjectExternalInternalMapData> externalToInternalIdMapData, UUID projectId) throws IllegalArgumentException, IllegalAccessException {

		List<IErrorObject> errorList = new ArrayList<>();
		
		for(JsonNode objectNode : userMappingNode) {
			UserProjectMapping userProjectMapping = new UserProjectMapping();
			userProjectMapping.setSuperAppId(superAppId);
			userProjectMapping.setAppId(appId);
			userProjectMapping.setInsertTs(System.currentTimeMillis());
			List<Field> fieldsList = getRequiredFields(mappingObject, objectNode);
			String userExtId = null;
			for (Field f : fieldsList) {	
				f.setAccessible(true);
				if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.UserProjectMappingUploadConstants.USER_ID, objectNode))) {
					userExtId = String.valueOf(f.get(mappingObject));
					if(userExtId == null || userExtId.isEmpty() || userExtId.equals("null")) {
						userProjectMapping = null;
						break;
					}
					String userIntId = userMetaDataCrudService.getExternalToInternalMappingFromMV(superAppId, userExtId);
					if(userIntId == null) {
						errorList.add(new ErrorObject("INVALID USER ID", IErrorObject.ERROR, "Invalid user Id found for - " + userExtId , IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						return errorList;
					}
					userProjectMapping.setUserId(userIntId);
					String userTypeStr = getTextValueFromJsonNode(IFileUploadConstants.UserProjectMappingUploadConstants.USER_TYPE, objectNode);
					if(userTypeStr.matches("[0-9]+")) {
						userProjectMapping.setUserType(Integer.parseInt(userTypeStr));
					} else {
						int userType = convertUserTypeToInt(userTypeStr);
						if(userType == CommonConstants.NAInteger) {
							errorList.add(new ErrorObject("INVALID USER TYPE", IErrorObject.ERROR, "Invalid user type found - " + userTypeStr, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						}
						userProjectMapping.setUserType(userType);
					}
				} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.UserProjectMappingUploadConstants.PROJECT_ID, objectNode))) {
					
					String extProjectId =  String.valueOf(f.get(mappingObject));
					if(externalToInternalIdMapData.get(extProjectId) == null) {
						errorList.add(new ErrorObject("NO INTERNAL ID FOUND", IErrorObject.ERROR, "No project Internal ID found corresponding to external ID - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						return errorList;
					}
					projectId = externalToInternalIdMapData.get(extProjectId).getProjectId();
					if(projectId == null) {
						errorList.add(new ErrorObject("NO INTERNAL ID FOUND", IErrorObject.ERROR, "No project Internal ID found corresponding to external ID - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						return errorList;
					}
					ProjectMasterData masterData = masterDataCrudService.findProjectMasterDataByPrimaryKey(superAppId, appId, projectId, CommonConstants.DEFAULT_DATE, MasterDataKeyNames.STATE_KEY, ProjectStates.ALL);
					if(masterData == null) {
						errorList.add(new ErrorObject("NO INTERNAL ID FOUND", IErrorObject.ERROR, "No project Internal ID found corresponding to external ID - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
						return errorList;
					}
					if(masterData.getValue().equals(ProjectStates. DELETED.getValue())) {
						errorList.add(new ErrorObject("NO PROJECT ID FOUND", IErrorObject.ERROR, "Invalid ProjectId Found :: Project Deleted - " + extProjectId, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					}
				}
				
			}
			if(userProjectMapping != null) {
				UserDBMetaData userMetaData = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userProjectMapping.getUserId(), UserStates.ACTIVE);
			
				if(userMetaData == null) {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.INVALID_USER, "No User Details found in user meta data for user ID - " + userProjectMapping.getUserId(), IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				}
				if(!errorList.isEmpty()) {
					return errorList;
				}
				if(!userProjectMappingMap.containsKey(userProjectMapping.getUserId())) {
					userProjectMappingMap.put(userProjectMapping.getUserId(), new HashMap<>());
				}
				List<UUID> projectIdList = null;
				Map<Integer, UserProjectMapping> userTypeToProjectMapping = userProjectMappingMap.get(userProjectMapping.getUserId());
				if(!userTypeToProjectMapping.containsKey(userProjectMapping.getUserType())) {
					userTypeToProjectMapping.put(userProjectMapping.getUserType(), userProjectMapping);
				} 
				projectIdList = userTypeToProjectMapping.get(userProjectMapping.getUserType()).getProjectList();
				if(projectIdList == null) {
					projectIdList = new ArrayList<>();
				}
				if(!projectIdList.contains(projectId))
					projectIdList.add(projectId);
				userProjectMapping.setProjectList(projectIdList);
				userTypeToProjectMapping.put(userProjectMapping.getUserType(), userProjectMapping);
			}
		}
		return errorList;
	}

	private List<Field> getRequiredFields(Object mappingObject, JsonNode objectNode) {
		List<Field> fields = new ArrayList<>();
		for(Field field : mappingObject.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			if(field.getName().equals(getTextValueFromJsonNode(UserProjectMappingUploadConstants.USER_ID, objectNode))
					|| field.getName().equals(getTextValueFromJsonNode(UserProjectMappingUploadConstants.PROJECT_ID, objectNode))) {
				fields.add(field);
			}
		}
		return fields;
	}

	private int convertUserTypeToInt(String userTypeStr) {
		if(userTypeStr.toLowerCase().contains(UserPriorities.Primary.name().toLowerCase())) {
			return UserPriorities.Primary.getValue();
		} else if(userTypeStr.toLowerCase().contains(UserPriorities.Secondary.name().toLowerCase())) {
			return UserPriorities.Secondary.getValue();
		} else if(userTypeStr.toLowerCase().contains(UserPriorities.Default.name().toLowerCase())) {
			return UserPriorities.Default.getValue();
		} else {
			return CommonConstants.NAInteger;
		}
	}

	public Map<String,Map<Integer, UserProjectMapping>> getUserProjectMappingMap(UUID superAppId, UUID appId) throws IOException {
		Map<String,Map<Integer, UserProjectMapping>> userProjectMappingMap = new HashMap<>();
		List<UserProjectMapping> userProjectMappingFromDB = userProjectMappingCrudService.findUserProjectMappingByPartitionKey(superAppId, appId);
		if(userProjectMappingFromDB != null) {
			for(UserProjectMapping mapping : userProjectMappingFromDB) {
				if(!userProjectMappingMap.containsKey(mapping.getUserId())) {
					userProjectMappingMap.put(mapping.getUserId(), new HashMap<>());
				}
				Map<Integer, UserProjectMapping> userTypeToMappingMap = userProjectMappingMap.get(mapping.getUserId());
				userTypeToMappingMap.put(mapping.getUserType(), mapping);
			}
		}
		return userProjectMappingMap;
	}

	public void processQueryForUploadType(String uploadType, UUID superAppId, UUID appId) throws InvalidInputException, IOException {
		if(uploadType == null) {
			uploadType = CommonConstants.DEFAULT_UPLOAD_TYPE;
		}
		if(!CommonConstants.getAllUploadTypes().contains(uploadType)) {
			throw new InvalidInputException();
		}
		if(uploadType.equals(CommonConstants.DELETE_INSERT_UPLOAD)) {
			userProjectMappingCrudService.deleteAllRecords(superAppId, appId);
		}		
	}
}
