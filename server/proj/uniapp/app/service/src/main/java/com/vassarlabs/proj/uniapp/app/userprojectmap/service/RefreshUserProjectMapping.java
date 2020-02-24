package com.vassarlabs.proj.uniapp.app.userprojectmap.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class RefreshUserProjectMapping {
	@Autowired UserProjectMapCrudService userProjMappingData;
	@Autowired ProjectMasterDataCrudService projectMasterDataCrudService;
	@Autowired UserMetaDataCrudService userMetaDataCrudService;
	@Autowired ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;

	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}


	ObjectMapper objectMapper = new ObjectMapper();
	boolean showMap = false;


	public void refreshProjectIdsAssignedToDeafaultUser(UUID superAppId, UUID appId) {
//		List<ProjectExternalInternalMapData> projectExternalToInternalDataList = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataByPartitionKey(superAppId, appId);
//		if(projectExternalToInternalDataList == null || projectExternalToInternalDataList.isEmpty()) 
//			return;
//		projectExternalToInternalDataList.stream().map(ProjectExternalInternalMapData::getProjectId).collect(Collectors.toList());
		List<UUID> projectIds = new ArrayList<>();
		List<String> userIds = new ArrayList<String> ();	
		List<UserDBMetaData>  users = userMetaDataCrudService.findUserDataByPartitionKey(superAppId, UserStates.ACTIVE);
		if ( users  != null && users.size() > 0) {
			for (UserDBMetaData user: users) {
				if(!userIds.contains(user.getUserId()) && !user.getUserId().equals(CommonConstants.DEFAULT_USER_ID) )	 {
					userIds.add(user.getUserId());
				}
			}
		}
		List<UUID> assignedProjectIds = userProjMappingData.getAllProjectIdsAssignedtoUsersForApp(superAppId, appId, userIds );
		Map<UUID, ProjectMasterData> projectIdToDataList = projectMasterDataCrudService.findDataForProjectsState(superAppId, appId, projectIds);
		projectIds.clear();
		for(UUID projectId : projectIdToDataList.keySet()) {
			if(!projectIdToDataList.get(projectId).getValue().equals(ProjectStates.DELETED.getValue())) {
				projectIds.add(projectId);
			}
		}
		projectIds.removeAll(assignedProjectIds);
		assignProjectsToDefaultUser(superAppId, appId, projectIds);
		
	}
	
	public void assignProjectsToDefaultUser (UUID superAppId, UUID appId, List<UUID> projectIds ) {
		logger.info("Inside re-assign projects to Deafult User : Started");
		UserProjectMapping defaultUserProjectMapping =  userProjMappingData.findUserProjectMappingByPrimaryKey(superAppId, appId, CommonConstants.DEFAULT_USER_ID, UserPriorities.Default.getValue());
		if(defaultUserProjectMapping == null) {
			defaultUserProjectMapping = new UserProjectMapping();
			defaultUserProjectMapping.setSuperAppId(superAppId);
			defaultUserProjectMapping.setAppId(appId);
			defaultUserProjectMapping.setProjectList(projectIds);
			defaultUserProjectMapping.setUserId(CommonConstants.DEFAULT_USER_ID);
			defaultUserProjectMapping.setUserType(UserPriorities.Default.getValue());
			defaultUserProjectMapping.setInsertTs(System.currentTimeMillis());
		}
		else {
			userProjMappingData.deleteAllRecordsForAUser(superAppId, Arrays.asList(appId), CommonConstants.DEFAULT_USER_ID);
			defaultUserProjectMapping.setProjectList(projectIds);
			defaultUserProjectMapping.setInsertTs(System.currentTimeMillis());
		}
		userProjMappingData.insertUserProjectMappingData(defaultUserProjectMapping);
		logger.info("Inside re-assign projects to Deafult User: Re-Assigned");
	}
	
}
