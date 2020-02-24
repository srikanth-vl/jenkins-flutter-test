package com.vassarlabs.proj.uniapp.app.computation.businessanalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.BusinessAnalyticsData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectSubmissionAnalyticsData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.constants.ProjectListConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.BusinessAnalyticsDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FormSubmittedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectSubmissionAnalyticsDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.ComputationTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;

@Component
public class BusinessAnalyticsDataUpdateServiceScript {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired ProjectMasterDataCrudService projMasterCrudService;
	@Autowired SuperAppDataCrudService superAppDataCrudService;
	@Autowired DataRetrievalService dataRetrievalService;
	@Autowired ApplicationMetaDataCrudService appMetaCrudService;
	@Autowired FieldMetaDataCrudService fieldMetaDataCrudService;
	@Autowired BusinessAnalyticsDataCrudService businessAnalyticsCrudService;
	@Autowired ApplicationMetaDataCrudService applicationMetaDataCrudService;
	@Autowired ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	@Autowired ProjectSubmissionAnalyticsDataCrudService projectSubmissionAnalyticsDataCrudService;
	@Autowired FormSubmittedDataCrudService formSubmittedDataCrudService;
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
				if(String.valueOf(app.getAppId()).equalsIgnoreCase("6bf61cb2-d659-3b93-950b-59075ce434df"))
				{
					System.out.println("Dont execute for MI_TANKS"  + app.getAppId());
					continue;
				}
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
		
//		List<FieldMetaData> fieldsList = fieldMetaDataCrudService.findFieldMetaDataByPartitionKey(superAppId, appId);
//		Map<String, FieldMetaData> keyToMetaDataMap = new HashMap<String, FieldMetaData>();
		
//		for (FieldMetaData fieldData : fieldsList) {
//			if (fieldData.getComputationType() != null && !fieldData.getComputationType().isEmpty()) {
//				if (!keyToMetaDataMap.containsKey(fieldData.getKey())) {
//					keyToMetaDataMap.put(fieldData.getKey(), fieldData);
//				} else {
//					if (fieldData.getMetaDataVersion() > keyToMetaDataMap.get(fieldData.getKey()).getMetaDataVersion()) {
//						keyToMetaDataMap.put(fieldData.getKey(), fieldData);
//					}
//				}
//			}
//		}
//		if(keyToMetaDataMap == null || keyToMetaDataMap.isEmpty()) {
//			logger.info(" updateBusinessAnalyticsData() -  " + "Computation fields not found for Business analytics data for SuperApp :: "  + superAppId + " App :: " + appId);
//			return;
//		}
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
			for (String attribute : hierarchyList) {
				String locEntity;
				if(hierarchyList.size() == 1 && attribute.equalsIgnoreCase(CommonConstants.PROJECT_SUBMISSION_ANALYTICS_DEFAULT_HIERARCHY_ELEMENT) && keyValueMap.get(attribute) ==  null) {
					locEntity = CommonConstants.PROJECT_SUBMISSION_ANALYTICS_ALL_PROJECTS;
				} else if (keyValueMap.containsKey(attribute) && keyValueMap.get(attribute)!= null) {
					locEntity = keyValueMap.get(attribute);
				}
				else {
					break;
				}
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
			} 
		}
		

		Map<String, Set<UUID>> locationWiseProjectIds= new HashMap<>();
		for (String locString : locToKeyValueMap.keySet()) {
			Map<UUID, Map<String,String>> idtoValuesMap = locToKeyValueMap.get(locString);
			Set<UUID> projectIdsForEntity = idtoValuesMap.keySet();
			locationWiseProjectIds.put(locString, projectIdsForEntity);
		}
		updateProjectSubmissionTables(superAppId, appId, locationWiseProjectIds);
		logger.info("In updateBusinessAnalyticsData() -  " + "Completed Computation for Business analytics data for SuperApp :: "  + superAppId + " App :: " + appId);
		
	}
	

	private void updateProjectSubmissionTables(UUID superAppId, UUID appId, Map<String, Set<UUID>> locationStringToProjectIds)  {
		int maxDate = 99999999;
		for (String locationString : locationStringToProjectIds.keySet()) {
			String[] locEntities = locationString.split("##");
			String parentEntity = locEntities[0];
			String childEntity = locEntities[1];
//			ProjectSubmissionAnalyticsData data = new ProjectSubmissionAnalyticsData();
//			data.setApplicationId(appId);
//			data.setSuperAppId(superAppId);
//			data.setDate(maxDate);
//			data.setEntity(childEntity);
//			data.setParentEntity(parentEntity);
//			data.setFailedSubmissionProjectIds(new HashSet<>());
//			data.setSuccessfulSubmissionProjectIds(new HashSet<>());
//			data.setNoOfFailedSubmissions(0);
//			data.setNoOfSuccessfulSubmissions(0);
//			data.setInsertTs(System.currentTimeMillis());	
//			projectSubmissionAnalyticsDataCrudService.insertHierarchicalData(data);
			BusinessAnalyticsData businessAnalyticsData = businessAnalyticsCrudService.findByPrimaryKey(superAppId, appId, parentEntity, childEntity);
			if(businessAnalyticsData == null) {
				businessAnalyticsData = new BusinessAnalyticsData();
				businessAnalyticsData.setApplicationId(appId);
				businessAnalyticsData.setSuperAppId(superAppId);
				businessAnalyticsData.setParentEntity(parentEntity);
				businessAnalyticsData.setChildEntity(childEntity);
				businessAnalyticsData.setComputedValues(null);
				businessAnalyticsData.setInsertTs(System.currentTimeMillis());
			}
			businessAnalyticsData.setProjectIds(locationStringToProjectIds.get(locationString));
			businessAnalyticsCrudService.insertBusinessAnalyticsData(businessAnalyticsData);
			Set<UUID> projectIds = locationStringToProjectIds.get(locationString);
			if(projectIds == null) {
				continue;
			}
			List<UUID> projectIdsForEntity = new ArrayList<>(projectIds);
			Map<UUID,List<FormSubmitData>> formSubmitData = formSubmittedDataCrudService.findFormSubmittedDataForListOfProjects(superAppId, appId, projectIdsForEntity);
			Map<UUID,Map<Long,Map<String,String>>> projectIdTokeyValue = new HashMap<>();	
			for (UUID  projectId : formSubmitData.keySet()) {
				Map<Long, Map<String,String>> keyToValue = new HashMap<>();
				for(FormSubmitData formData : formSubmitData.get(projectId)) {
					if(!keyToValue.containsKey(formData.getTimestamp())) {
						keyToValue.put(formData.getTimestamp(), new HashMap<String, String>());
						Map<String,String> map = keyToValue.get(formData.getTimestamp());
						map.put("user_id", formData.getUserId());
						keyToValue.put(formData.getTimestamp(), map);	
					}
					Map<String,String> map = keyToValue.get(formData.getTimestamp());
					map.put(formData.getKey(),formData.getValue());
					keyToValue.put(formData.getTimestamp(), map);	
				}
				projectIdTokeyValue.put(projectId, keyToValue);

			}
			Map<Integer, ProjectSubmissionAnalyticsData> dateToPriojectSubmissionAnalytics = new HashMap<>();

			for (UUID projectId : projectIdTokeyValue.keySet()) {
				if(projectIdTokeyValue.get(projectId) == null) {
					continue;
				}
				for(Long timestamp :projectIdTokeyValue.get(projectId).keySet()) {
					if(projectIdTokeyValue.get(projectId).get(timestamp) == null ) {
						continue;
					}
					Integer dateOfSubmission = DateUtils.getYYYYMMdd(timestamp);
					ProjectSubmissionAnalyticsData submissionAnalytics = dateToPriojectSubmissionAnalytics.get(dateOfSubmission);
					if(submissionAnalytics == null) {
						submissionAnalytics = projectSubmissionAnalyticsDataCrudService.findByPrimaryKey(superAppId, appId, parentEntity, dateOfSubmission, childEntity);
						if(submissionAnalytics == null) {
						submissionAnalytics = new ProjectSubmissionAnalyticsData();
						submissionAnalytics.setApplicationId(appId);
						submissionAnalytics.setSuperAppId(superAppId);
						submissionAnalytics.setDate(dateOfSubmission);
						submissionAnalytics.setEntity(childEntity);
						submissionAnalytics.setParentEntity(parentEntity);
						submissionAnalytics.setFailedSubmissionProjectIds(new HashSet<>());
						submissionAnalytics.setNoOfFailedSubmissions(0);
						submissionAnalytics.setSuccessfulSubmissionProjectIds(new HashSet<>());
						submissionAnalytics.setNoOfSuccessfulSubmissions(0);
						submissionAnalytics.setInsertTs(System.currentTimeMillis());
						submissionAnalytics.setUserIds(new HashSet<>());
						} 
						submissionAnalytics.setNoOfSuccessfulSubmissions(submissionAnalytics.getNoOfSuccessfulSubmissions() == null ? 0 : submissionAnalytics.getNoOfSuccessfulSubmissions());
					}
					Set<UUID> sucessfullProjectsSubmitted = submissionAnalytics.getSuccessfulSubmissionProjectIds()== null ? new HashSet<>() : submissionAnalytics.getSuccessfulSubmissionProjectIds();
					sucessfullProjectsSubmitted.add(projectId);
					Set<String> userIds = submissionAnalytics.getUserIds() == null ? new HashSet<>() :  submissionAnalytics.getUserIds();
					userIds.add(projectIdTokeyValue.get(projectId).get(timestamp).get("user_id"));
					int noOfSubmissionOnThisdate = projectIdTokeyValue.get(projectId).get(timestamp) != null ? 1 : 0;
					submissionAnalytics.setNoOfSuccessfulSubmissions(submissionAnalytics.getNoOfSuccessfulSubmissions() + noOfSubmissionOnThisdate);
					submissionAnalytics.setUserIds(userIds);
					dateToPriojectSubmissionAnalytics.put(dateOfSubmission, submissionAnalytics);
				}
			}
			for (Integer date : dateToPriojectSubmissionAnalytics.keySet()) {
				ProjectSubmissionAnalyticsData projectSubmissionAnalyticsData = dateToPriojectSubmissionAnalytics.get(date);
				projectSubmissionAnalyticsDataCrudService.insertHierarchicalData(projectSubmissionAnalyticsData);
				
			}
			
		}
		
	}
}
