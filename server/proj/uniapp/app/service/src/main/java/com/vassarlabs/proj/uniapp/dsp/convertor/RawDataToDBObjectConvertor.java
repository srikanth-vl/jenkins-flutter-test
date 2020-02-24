package com.vassarlabs.proj.uniapp.dsp.convertor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;

@Component
public class RawDataToDBObjectConvertor {
	
	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;

	public Map<String, ProjectExternalInternalMapData> generateExternalToInternalIdMap(UUID superAppId, UUID appId, List<String> externalProjIds) {
		Map<String, ProjectExternalInternalMapData> externalToInternalIdMapData = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, externalProjIds);
		if(externalToInternalIdMapData == null) {
			externalToInternalIdMapData = new HashMap<>();
		}
		for(String extId : externalProjIds) {
			ProjectExternalInternalMapData projectExternalToInternalData = externalToInternalIdMapData.get(extId);
			UUID projectId = null;
			if(projectExternalToInternalData == null ) {
				projectId = UUIDUtils.getTrueTimeUUID();
				externalToInternalIdMapData.put(extId, createProjectExternalInternalDataObject(superAppId,appId, extId, projectId));
			}
		}
		return externalToInternalIdMapData;
	}
	
	public ProjectExternalInternalMapData createProjectExternalInternalDataObject(UUID superAppId, UUID appId,
			String extId, UUID projectId) {
		ProjectExternalInternalMapData projectExternalInternalMapData = new ProjectExternalInternalMapData();
		projectExternalInternalMapData.setSuperAppId(superAppId);
		projectExternalInternalMapData.setAppId(appId);
		projectExternalInternalMapData.setProjectId(projectId);
		projectExternalInternalMapData.setInsertTs(System.currentTimeMillis());
		projectExternalInternalMapData.setProjectExternalId(extId);
		return projectExternalInternalMapData;
	}
	
	public ProjectMasterData getMasterDataCommonFields(UUID superAppId, UUID appId, int date, UUID projectId) {
		ProjectMasterData masterData =  new ProjectMasterData();
		masterData.setSuperAppId(superAppId);
		masterData.setApplicationId(appId);
		masterData.setInsertTs(System.currentTimeMillis());
		masterData.setDate(date);
		masterData.setProjectId(projectId);
		return masterData;
	}
}
