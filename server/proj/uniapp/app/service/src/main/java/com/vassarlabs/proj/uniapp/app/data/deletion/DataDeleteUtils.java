package com.vassarlabs.proj.uniapp.app.data.deletion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapDBData;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class DataDeleteUtils {
	
	@Autowired private UserMetaDataCrudService userMetaDataCrudService;
	@Autowired private UserProjectMapCrudService userProjectMappingCrudService;
	
	public void deleteUserProjectMapping(UUID superAppId, UUID appId, UUID projectId, List<String> userIdList, String userTypeInput, List<IErrorObject> errorList, List<String> userIdsToDelete) {

		Map<String, UserDBMetaData> userMetaDataMap = userMetaDataCrudService.getMetaDataForListOfUsers(superAppId, userIdList, UserStates.ACTIVE);
		List<String> userIdsToDeleteMappingFor = new ArrayList<>();
		for(String userId : userIdList) {
			if(!userMetaDataMap.containsKey(userId)) {
				errorList.add(new ErrorObject("INVALID USER ID", IErrorObject.ERROR, "Invalid user Id found for - " + userId , IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				continue;
			}
			userIdsToDeleteMappingFor.add(userId);
		}
		Integer userType = -1 ;
		if(userTypeInput != null && !userTypeInput.isEmpty()) {
			userType = UserPriorities.convertUserTypeToInt(userTypeInput);
		}
		if(projectId != null) {
			if(userType != -1 && userType >= 0) {
				userProjectMappingCrudService.deleteAllRecordsForUsersWithUserTypeAndGivenProject(superAppId, appId, userIdsToDeleteMappingFor, userType, projectId);
			
			} else {
				userProjectMappingCrudService.deleteAllRecordsForUsersAndGivenProject(superAppId, appId, userIdsToDeleteMappingFor, UserPriorities.getAllValues(), projectId);
				
			}
		} else {
			if(userType != -1 && userType >= 0) {
				userProjectMappingCrudService.deleteAllRecordsForUsersWithUserType(superAppId, appId, userIdsToDeleteMappingFor, userType);
			
			} else {
				userProjectMappingCrudService.deleteAllRecordsForGivenUsersForApp(superAppId, appId, userIdsToDeleteMappingFor);
			}
			
		}
	}
	public void deleteUserProjectMappingForGivenProjects(UUID superAppId, UUID appId, List<UUID> projectIds) { 
		Set<String> userIds = new HashSet<String>();	
		Set<Integer> userPriorties = new HashSet<>();
		Map<UUID, List<UserProjectMapDBData>>  projectIdToUsersData = userProjectMappingCrudService.getAllAssignedUsersForListOfProjects(superAppId, appId, projectIds);
		if (projectIdToUsersData  != null && !projectIdToUsersData.isEmpty()) {
			for(UUID projectId : projectIdToUsersData.keySet()) {
				Set<String> values = projectIdToUsersData.get(projectId).stream().map(UserProjectMapDBData::getUserId).collect(Collectors.toSet());
				userIds.addAll(values);
				Set<Integer> types = projectIdToUsersData.get(projectId).stream().map(UserProjectMapDBData::getUserType).collect(Collectors.toSet());
				userPriorties.addAll(types);
			}
		}	
		userProjectMappingCrudService.deleteAllRecordsForProjects(superAppId, appId, new ArrayList<>(userIds), new ArrayList<>(userPriorties), projectIds);
	}
	
}