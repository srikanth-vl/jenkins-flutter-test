package com.vassarlabs.proj.uniapp.app.projects.latestdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.DataSubmitField;
import com.vassarlabs.proj.uniapp.crud.service.FormSubmittedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;

@Component
public class ProjectListDataRetreivalService {
	
	@Autowired FormSubmittedDataCrudService formDataCrudService;
	@Autowired ProjectMasterDataCrudService projMasterCrudService;
	@Autowired DataRetrievalService dataService;
	
	public Map<String, Map<String, String>> getLatestDataFromProjExtIds (UUID superApp, UUID appId, Map<String, UUID> extToInternalIdMap) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		Map<String, Map<String, String>> output = new HashMap<>();
		
		Map<UUID, Map<String, String>> projectsData = dataService.getValueForListOfProjectWithSyncTSAndUserId(superApp, appId, new ArrayList<>(extToInternalIdMap.values()));
		
		if (extToInternalIdMap != null) {
			for (String extId : extToInternalIdMap.keySet()) {
				UUID internalId = extToInternalIdMap.get(extId);
				Map<String, String> mapOutput = projectsData.get(internalId);
				if (mapOutput != null) {
					output.put(extId, mapOutput);
				}
			}
		}
		return output;
	}
	
	public Map<String, Map<String, DataSubmitField>> getLatestDataFromProjExtIdsWithDataSubmitField (UUID superApp, UUID appId, Map<String, UUID> extToInternalIdMap) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		
		Map<String, Map<String, DataSubmitField>> output = new HashMap<>();
		
		Map<UUID, Map<String, DataSubmitField>> projectsData = dataService.getValueForListOfProjectWithSyncTSUserIdAndDataSubmitField(superApp, appId, new ArrayList<>(extToInternalIdMap.values()));
		
		if (extToInternalIdMap != null) {
			for (String extId : extToInternalIdMap.keySet()) {
				UUID internalId = extToInternalIdMap.get(extId);
				Map<String, DataSubmitField> mapOutput = projectsData.get(internalId);
				if (mapOutput != null) {
					output.put(extId, mapOutput);
				}
			}
		}
		return output;
	}
}