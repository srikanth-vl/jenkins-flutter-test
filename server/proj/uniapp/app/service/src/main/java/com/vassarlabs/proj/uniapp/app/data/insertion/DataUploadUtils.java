package com.vassarlabs.proj.uniapp.app.data.insertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class DataUploadUtils {
	
	@Autowired private UserMetaDataCrudService userMetaDataCrudService;
	@Autowired private ProjectMasterDataCrudService masterDataCrudService;
	@Autowired private UserProjectMapCrudService userProjectMappingCrudService;
	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;	
	
	public void populateUserProjectMapping(UUID superAppId, UUID appId, UUID projectId, List<String> userIdList, String userTypeInput,
			Map<String, Map<Integer, UserProjectMapping>> userProjectMappingMap, List<IErrorObject> errorList, List<String> userIdsToDelete) {
		
		Map<String, UserDBMetaData> userMetaDataMap = userMetaDataCrudService.getMetaDataForListOfUsers(superAppId, userIdList, UserStates.ACTIVE);
		for(String userId : userIdList) {
			if(!userMetaDataMap.containsKey(userId)) {
				errorList.add(new ErrorObject("INVALID USER ID", IErrorObject.ERROR, "Invalid user Id found for - " + userId , IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				return;
			}
			if(!userProjectMappingMap.containsKey(userId)) {
				userProjectMappingMap.put(userId, new HashMap<>());
			}
			// By default assign DEFAULT user type 
			Integer userType = UserPriorities.Default.getValue();

			if(userTypeInput != null) {
				userType = UserPriorities.convertUserTypeToInt(userTypeInput);
				// Delete all the previously assigned user to the project with default priority(during Master data upload) 
				// and insert with the primary/secondary user priority
				if(userType != UserPriorities.Default.getValue()) {
					userIdsToDelete.add(userId);
				}
			}
			
			Map<Integer, UserProjectMapping> userTypeToMapping = userProjectMappingMap.get(userId);
			
			if(!userTypeToMapping.containsKey(userType)) {
				UserProjectMapping userProjectMappingObject = new UserProjectMapping();
				userProjectMappingObject.setSuperAppId(superAppId);
				userProjectMappingObject.setAppId(appId);
				userProjectMappingObject.setInsertTs(System.currentTimeMillis());
				userProjectMappingObject.setUserId(userId);
				userProjectMappingObject.setUserType(userType);
				userProjectMappingObject.setProjectList(new ArrayList<>());
				userTypeToMapping.put(userType, userProjectMappingObject);
			}
			UserProjectMapping userProjectMappingObject = userTypeToMapping.get(userType);
			userProjectMappingObject.getProjectList().add(projectId);
			userTypeToMapping.put(userType, userProjectMappingObject);
			userProjectMappingMap.put(userId, userTypeToMapping);
		}
	}
	
//	private int convertUserTypeToInt(String userTypeStr) {
//		if(userTypeStr.toLowerCase().contains(UserPriorities.Primary.name().toLowerCase())) {
//			return UserPriorities.Primary.getValue();
//		} else if(userTypeStr.toLowerCase().contains(UserPriorities.Secondary.name().toLowerCase())) {
//			return UserPriorities.Secondary.getValue();
//		} else if(userTypeStr.toLowerCase().contains(UserPriorities.Default.name().toLowerCase())) {
//			return UserPriorities.Default.getValue();
//		} else {
//			return CommonConstants.NAInteger;
//		}
//	}
	
	public void insertRecordsIntoDatabase(List<ProjectMasterData> masterDataList, Map<String, Map<Integer, UserProjectMapping>> userProjectMappingMap,
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMap) {

		List<ProjectExternalInternalMapData> externalToInternalIdList = new ArrayList<>();
		List<UserProjectMapping> userProjMappingInsertList = new ArrayList<>();
		if(masterDataList != null) {
			masterDataCrudService.insertListOfProjectMasterData(masterDataList);
		}
		for(String userId : userProjectMappingMap.keySet()) {
			for(Integer userType : userProjectMappingMap.get(userId).keySet()) {
				userProjMappingInsertList.add(userProjectMappingMap.get(userId).get(userType));
			}
		}
		userProjectMappingCrudService.insertUserProjectMappingData(userProjMappingInsertList);
		for(String externalId : externalToInternalIdMap.keySet()) {
			externalToInternalIdList.add(externalToInternalIdMap.get(externalId));
		}
		projectExternalToInternalMappingCrudService.insertListOfProjectExternalInternalMapData(externalToInternalIdList);		
	}
}