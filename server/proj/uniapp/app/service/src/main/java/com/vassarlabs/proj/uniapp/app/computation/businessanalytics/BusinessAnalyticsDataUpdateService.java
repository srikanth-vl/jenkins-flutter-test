package com.vassarlabs.proj.uniapp.app.computation.businessanalytics;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.BusinessAnalyticsData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.constants.ProjectListConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.BusinessAnalyticsDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.ComputationTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;

@Component
public class BusinessAnalyticsDataUpdateService {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired ProjectMasterDataCrudService projMasterCrudService;
	@Autowired SuperAppDataCrudService superAppDataCrudService;
	@Autowired DataRetrievalService dataRetrievalService;
	@Autowired ApplicationMetaDataCrudService appMetaCrudService;
	@Autowired FieldMetaDataCrudService fieldMetaDataCrudService;
	@Autowired BusinessAnalyticsDataCrudService businessAnalyticsCrudService;
	@Autowired ApplicationMetaDataCrudService applicationMetaDataCrudService;
	@Autowired ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	@Autowired 
	private IVLLogService logFactory;
	private IVLLogger logger;
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	public void execute() {
		List<SuperApplicationData> superApps = superAppDataCrudService.getAllLatestSuperAppData();
		if(superApps == null || superApps.isEmpty()) {
			return ;
		}
		for (SuperApplicationData superApplicationData : superApps) {
			List<ApplicationMetaData> apps =  applicationMetaDataCrudService.getApplicationMetaDataForSuperApp(superApplicationData.getSuperAppId());
			for (ApplicationMetaData app : apps) {
				updateBusinessAnalyticsData(superApplicationData.getSuperAppId(),app.getAppId());
			}
			
		}
	}
	public void updateBusinessAnalyticsData(UUID superAppId, UUID appId) {
		
		logger.info("In updateBusinessAnalyticsData() -  " + "Started Computation for Business analytics data for SuperApp :: "  + superAppId + " App :: " + appId);
		List<ProjectExternalInternalMapData> projectExternalToInternalDataList = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataByPartitionKey(superAppId, appId);
		if(projectExternalToInternalDataList == null || projectExternalToInternalDataList.isEmpty()) 
			return;
//		projectExternalToInternalDataList.stream().map(ProjectExternalInternalMapData::getProjectId).collect(Collectors.toList());
//		List<ProjectMasterData> projMasterData =  projMasterCrudService.findProjectMasterDataByPartitionKey(superAppId, appId, ProjectStates.INPROGRESS);
		List<UUID> projectIds = new ArrayList<UUID>();

		for (ProjectExternalInternalMapData projectExternalToInternalData : projectExternalToInternalDataList) {
			projectIds.add(projectExternalToInternalData.getProjectId());
		}
		
		List<FieldMetaData> fieldsList = fieldMetaDataCrudService.findFieldMetaDataByPartitionKey(superAppId, appId);
		Map<String, FieldMetaData> keyToMetaDataMap = new HashMap<String, FieldMetaData>();
		
		for (FieldMetaData fieldData : fieldsList) {
			if (fieldData.getComputationType() != null && !fieldData.getComputationType().isEmpty()) {
				if (!keyToMetaDataMap.containsKey(fieldData.getKey())) {
					keyToMetaDataMap.put(fieldData.getKey(), fieldData);
				} else {
					if (fieldData.getMetaDataVersion() > keyToMetaDataMap.get(fieldData.getKey()).getMetaDataVersion()) {
						keyToMetaDataMap.put(fieldData.getKey(), fieldData);
					}
				}
			}
		}
		if(keyToMetaDataMap == null || keyToMetaDataMap.isEmpty()) {
			logger.info(" updateBusinessAnalyticsData() -  " + "Computation fields not found for Business analytics data for SuperApp :: "  + superAppId + " App :: " + appId);
			return;
		}
		List<String> hierarchyList = appMetaCrudService.getEntityHeirarchyForApp(superAppId, appId);

		Map<UUID, Map<String, String>> projectIdToKeyValueMap = new HashMap<UUID, Map<String,String>>();
		
		try {
			projectIdToKeyValueMap = dataRetrievalService.getValueForAProject(superAppId, appId, projectIds, ProjectStates.INPROGRESS);
		} catch (DataNotFoundException e) {
			logger.info("updateBusinessAnalyticsData() - could not fetch projectIdToKeyValueMap for SuperApp :: "  + superAppId + " App :: " + appId);
			e.printStackTrace();
			return;
		}
		
		
		Map<String, Map<UUID, Map<String, String>>> locToKeyValueMap = new HashMap<String, Map<UUID,Map<String,String>>>();
		
		Map<UUID, Map<String,String>> projectValuesMap;
		for (UUID projectId : projectIdToKeyValueMap.keySet()) {
			Map<String, String> keyValueMap = projectIdToKeyValueMap.get(projectId);
			String parent = ProjectListConstants.NA_STRING;
			for (String entitiy : hierarchyList) {
				String locEntity;
				if (keyValueMap.containsKey(entitiy)) {
					locEntity = keyValueMap.get(entitiy);
					String location = parent+"##"+locEntity;
					projectValuesMap = new HashMap<UUID, Map<String,String>>();
					if (!locToKeyValueMap.containsKey(location)) {
						projectValuesMap.put(projectId, keyValueMap);
						locToKeyValueMap.put(location, projectValuesMap);
					} else {
						projectValuesMap = locToKeyValueMap.get(location);
						projectValuesMap.put(projectId, keyValueMap);
						locToKeyValueMap.put(location, projectValuesMap);
					}
					parent = locEntity;
				} else {
					break;
				}
			}
		}

		Map<String, Map<String, Object>> locationWiseComputedValues = new HashMap<>();
		for (String locString : locToKeyValueMap.keySet()) {
			Map<String, Object> computedValuesMap = new HashMap<>();
			Map<UUID, Map<String,String>> idtoValuesMap = locToKeyValueMap.get(locString);
			for (String key : keyToMetaDataMap.keySet()) {
				if (keyToMetaDataMap.get(key).getComputationType() != null) {
					String computationType = keyToMetaDataMap.get(key).getComputationType();
					if (ComputationTypes.getComputationTypeNameByValue(computationType) != null) {
						computeValue(idtoValuesMap, key, computationType, computedValuesMap);
					}
				}
				
			}
			locationWiseComputedValues.put(locString, computedValuesMap);
		}
		
		updateTableWithComputedValues(locationWiseComputedValues, superAppId, appId);
		logger.info("In updateBusinessAnalyticsData() -  " + "Completed Computation for Business analytics data for SuperApp :: "  + superAppId + " App :: " + appId);
		
	}

	private void computeValue(Map<UUID, Map<String, String>> idtoValuesMap, String key, String computationType, Map<String, Object> computedValuesMap) {
		
		Double[] computedValue = { 0.0 };
		if (computationType!= null && (computationType.equalsIgnoreCase(ComputationTypes.SUM.getValue()) || computationType.equalsIgnoreCase(ComputationTypes.AVERAGE.getValue()))) {
			idtoValuesMap.forEach((projId, valuesMap)->{
				if(valuesMap != null && !valuesMap.isEmpty() && valuesMap.get(key) != null && !valuesMap.get(key).isEmpty()) {
					computedValue[0] = computedValue[0] + Double.parseDouble(valuesMap.get(key));
				}
			});
			if (computationType.equalsIgnoreCase(ComputationTypes.AVERAGE.getValue())) {
				computedValue[0] = computedValue[0]/(idtoValuesMap.size()); 
			}
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);
			Double dbvalue = computedValue[0];
			computedValue[0] = Double.parseDouble(df.format(dbvalue));
		} else if (computationType!= null && computationType.equalsIgnoreCase(ComputationTypes.MIN.getValue())) {
			idtoValuesMap.forEach((projId, valuesMap)->{
				valuesMap.forEach((k,v)-> {
					if (k.equalsIgnoreCase(key) && (computedValue[0] > Long.parseLong(v))) {
						computedValue[0] = Double.parseDouble(v);
					}
				});
			});
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);
			Double dbvalue = computedValue[0];
			computedValue[0] = Double.parseDouble(df.format(dbvalue));
		} else if (computationType!= null && computationType.equalsIgnoreCase(ComputationTypes.MAX.getValue())) {
			idtoValuesMap.forEach((projId, valuesMap)->{
				valuesMap.forEach((k,v)-> {
					if (k.equalsIgnoreCase(key) && (computedValue[0] < Long.parseLong(v))) {
						computedValue[0] = Double.parseDouble(v);
					}
				});
			});
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);
			Double dbvalue = computedValue[0];
			computedValue[0] = Double.parseDouble(df.format(dbvalue));
		} else if (computationType!= null && computationType.equalsIgnoreCase(ComputationTypes.COUNT.getValue())) {
			Integer[] frequency  = {0};
			idtoValuesMap.forEach((projId, valuesMap)->{
				if(valuesMap != null && !valuesMap.isEmpty() && valuesMap.get(key) != null && !valuesMap.get(key).isEmpty()) {
					frequency[0] = frequency[0] + 1;
				}
			});
			computedValuesMap.put(key, frequency[0]);
		}
		if(computationType!= null && !computationType.equalsIgnoreCase(ComputationTypes.COUNT.getValue())) {
			Double value = computedValue[0];
			if (value != null) {
				computedValuesMap.put(key, value);
			}
		}
	}
	
	private void updateTableWithComputedValues(Map<String, Map<String, Object>> locationWiseComputedValues, UUID superAppId, UUID appId) {
		
		List<BusinessAnalyticsData> businessDataList = new ArrayList<BusinessAnalyticsData>();

		for (String locationString : locationWiseComputedValues.keySet()) {
			String[] locEntities = locationString.split("##");
			BusinessAnalyticsData businessData = new BusinessAnalyticsData();
			businessData.setSuperAppId(superAppId);
			businessData.setApplicationId(appId);
			businessData.setParentEntity(locEntities[0]);
			businessData.setChildEntity(locEntities[1]);
			
			Map<String, Object> computedValuesMap = locationWiseComputedValues.get(locationString);
			
			String computedValues = null;
			try {
				computedValues = objectMapper.writeValueAsString(computedValuesMap);
			} catch (JsonProcessingException e) {
				logger.info("updateBusinessAnalyticsData() - eror occured while JsonProcessing of Map " +computedValuesMap);
				e.printStackTrace();
			}
			businessData.setComputedValues(computedValues);
			businessData.setInsertTs(System.currentTimeMillis());
			
			businessDataList.add(businessData);
		}
		
		businessAnalyticsCrudService.insertBusinessAnalyticsData(businessDataList);
	}

}
