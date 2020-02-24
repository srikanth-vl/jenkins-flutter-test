package com.vassarlabs.proj.uniapp.app.update.projects.status.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;

@Component
public class ProjectDisableService {
	@Autowired 
	private ProjectMasterDataCrudService projectmasterDataCrudService;

	@Autowired 
	private UserProjectMapCrudService  userProjectMapCrudService;
	
	@Autowired
	private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	
	@Autowired 
	private RefreshUserProjectMapping  refreshUserProjectMapping;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public void disableAllProjectForApp(UUID superAppId, UUID appId) {
		long timestamp = System.currentTimeMillis();
		List<UUID> projectIds = projectExternalToInternalMappingCrudService.findAllProjectsInApp(superAppId, appId);
		List<UUID> projectIdsToAssign = new ArrayList<>();
		Map<UUID, ProjectMasterData> projectIdToDataList = projectmasterDataCrudService.findDataForProjectsState(superAppId, appId, projectIds);
		List<ProjectMasterData> projectMasterDataList = projectIdToDataList.values().stream().collect(Collectors.toList());
		if(projectMasterDataList == null || projectMasterDataList.isEmpty()) {
			return;
		}
		List<ProjectMasterData> updatedRecords = new ArrayList<>();
		projectMasterDataList.stream().forEach(object -> {
			if (object.getKey().equals(MasterDataKeyNames.STATE_KEY)) {
				projectIdsToAssign.add(object.getProjectId());
				object.setValue(ProjectStates.DELETED.getValue());
				object.setInsertTs(timestamp);
				updatedRecords.add(object);
			}
		});
		
		projectmasterDataCrudService.insertListOfProjectMasterData(updatedRecords);
		
//		List<ApplicationFormData> appforms = applicationFormDataCrudService.findApplicationFormDataByPartitionKey(superAppId, appId, ActiveFlags.ACTIVE);
//		if(!(appforms == null || appforms.isEmpty())) {
//			for (ApplicationFormData form : appforms) {
//				if(!form.getProjectId().equals(UUIDUtils.getDefaultUUID())) {
//					form.setActiveFlag(ActiveFlags.INACTIVE.getValue());
//					form.setInsertTs(timestamp);
//				}
//			}
//			applicationFormDataCrudService.insertListOfApplicationFormData(appforms);
//		}
		userProjectMapCrudService.deleteAllRecords(superAppId, appId);
		refreshUserProjectMapping.assignProjectsToDefaultUser(superAppId, appId, projectIdsToAssign);
	}
	
	public void disableProject(UUID superAppId, UUID appId, UUID projectId) {
		long timestamp = System.currentTimeMillis();
		ProjectMasterData projectMasterData = projectmasterDataCrudService.findProjectMasterDataByPrimaryKey(superAppId, appId, projectId,CommonConstants.DEFAULT_DATE, MasterDataKeyNames.STATE_KEY, ProjectStates.ALL);
		if(projectMasterData == null ) {
			return;
		}
		projectMasterData.setValue(ProjectStates.DELETED.getValue());
		projectMasterData.setInsertTs(timestamp);
		projectmasterDataCrudService.insertProjectMasterData(projectMasterData);
	}
	
	public void disableProject(UUID superAppId, UUID appId, List<UUID> projectIds ) {
		long timestamp = System.currentTimeMillis();
		Map<UUID, ProjectMasterData> projectToStateDataMap = projectmasterDataCrudService.findDataForProjectsState(superAppId, appId, projectIds);
		if(projectToStateDataMap == null || projectToStateDataMap.isEmpty()) {
			return;
		}
		List<ProjectMasterData> dataToInsert = new ArrayList<>();
		for (UUID uuid : projectIds) {
			ProjectMasterData data = projectToStateDataMap.get(uuid);
			if(data != null) {
				data.setValue(ProjectStates.DELETED.getValue());
				data.setInsertTs(timestamp);
				dataToInsert.add(data);
			}
			
		}
		projectmasterDataCrudService.insertListOfProjectMasterData(dataToInsert);
	}
}
