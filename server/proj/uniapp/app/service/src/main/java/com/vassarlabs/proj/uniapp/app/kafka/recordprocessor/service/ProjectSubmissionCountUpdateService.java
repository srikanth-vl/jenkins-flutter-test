package com.vassarlabs.proj.uniapp.app.kafka.recordprocessor.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectSubmissionAnalyticsData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectSubmissionAnalyticsDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;

@Component
public class ProjectSubmissionCountUpdateService {

	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;
	@Autowired private DataRetrievalService dataRetrievalService;
	@Autowired private ProjectSubmissionAnalyticsDataCrudService projectSubmissionAnalyticsDataCrudService;
	private ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	public void execute(AppFormDataSubmittedList submittedDataObject, boolean isSubmissionSucessful) throws IOException {

		if (submittedDataObject == null) {
			logger.warn("Submitted Appform data list is NULL");
			return ;
		}

		if (submittedDataObject.isEmpty()) {
			logger.warn("Submitted Appform data list is EMPTY");
			return ;
		}

		List<AppFormData> formDataList = submittedDataObject.getAppFormDataList();
			UUID superAppId = submittedDataObject.getSuperAppId();
			UUID appId = submittedDataObject.getAppId();
		for (AppFormData dataFromApp : formDataList) {
			Map<String, String> keyToDataValue = new HashMap<>();
			Map<String, String> metaDataInstanceMap = metaDataInstanceIdToMap(dataFromApp.getMetaDataInstanceId());
			if (FormTypes.UPDATE.getValue() == Integer.parseInt(metaDataInstanceMap.get(CommonConstants.FORM_TYPE))) {
				List<String> attributeHierarchy = applicationMetaDataCrudService.getEntityHeirarchyForApp(submittedDataObject.getSuperAppId(), submittedDataObject.getAppId());
					try {
						keyToDataValue = dataRetrievalService.getValueForAProject(submittedDataObject.getSuperAppId(), submittedDataObject.getAppId(), dataFromApp.getProjectId(), ProjectStates.ALL);
					} catch (DataNotFoundException e) {
						logger.error("ProjectSubmissionCountUpdateService :: keyToValue Map for project could not be fetched");
						e.printStackTrace();
					}
					if(keyToDataValue == null || keyToDataValue.isEmpty()) {
						logger.error("ProjectSubmissionCountUpdateService :: keyToValue Map for project is null");
						continue;
					}
				
				String parentEntity = CommonConstants.NA;
				long insertTs = dataFromApp.getTimeStamp();
				int date = DateUtils.getYYYYMMdd(insertTs);
				if(attributeHierarchy == null || attributeHierarchy.isEmpty()) {
					String entity = null;
					entity = CommonConstants.PROJECT_SUBMISSION_ANALYTICS_ALL_PROJECTS;
					logger.info("Get Project Submission Analytics for Primary Key :: "+ superAppId + ":: " + appId +" :: " + parentEntity +" :: " +entity + " :: " + date );
					ProjectSubmissionAnalyticsData projectSubmissionAnalyticsData =  projectSubmissionAnalyticsDataCrudService.findByPrimaryKey(superAppId, appId, parentEntity, date, entity);
					if(projectSubmissionAnalyticsData == null) {
						projectSubmissionAnalyticsData =  createProjectSubmissionAnalyticsDBObject(superAppId, appId, date, parentEntity, entity);
					}
					if(isSubmissionSucessful) {
						updateSuccessfulSubmissionCount(projectSubmissionAnalyticsData, dataFromApp.getProjectId());
					} else {
						updateFailedSubmissionCount(projectSubmissionAnalyticsData, dataFromApp.getProjectId());
					}
					projectSubmissionAnalyticsData.getUserIds().add(submittedDataObject.getUserId());
					projectSubmissionAnalyticsData.setInsertTs(System.currentTimeMillis());
					projectSubmissionAnalyticsDataCrudService. insertHierarchicalData(projectSubmissionAnalyticsData);
					return;
					
				}
				for (String  attribute : attributeHierarchy) {
					String entity = null;
					if(attributeHierarchy.size() == 1 && attribute.equalsIgnoreCase(CommonConstants.PROJECT_SUBMISSION_ANALYTICS_DEFAULT_HIERARCHY_ELEMENT) && keyToDataValue.get(attribute) ==  null) {
						entity = CommonConstants.PROJECT_SUBMISSION_ANALYTICS_ALL_PROJECTS;
					}
					else if(keyToDataValue.get(attribute) ==  null) {
						break;
					}
					else {
						 entity = keyToDataValue.get(attribute);
					}
					if(entity == null || entity.isEmpty()) {
						break;			
					}
					logger.info("Get Project Submission Analytics for Primary Key :: "+ superAppId + ":: " + appId +" :: " + parentEntity +" :: " +entity + " :: " + date );
					ProjectSubmissionAnalyticsData projectSubmissionAnalyticsData =  projectSubmissionAnalyticsDataCrudService.findByPrimaryKey(superAppId, appId, parentEntity, date, entity);
					if(projectSubmissionAnalyticsData == null) {
						projectSubmissionAnalyticsData =  createProjectSubmissionAnalyticsDBObject(superAppId, appId, date, parentEntity, entity);
					}
					if(isSubmissionSucessful) {
						updateSuccessfulSubmissionCount(projectSubmissionAnalyticsData, dataFromApp.getProjectId());
					} else {
						updateFailedSubmissionCount(projectSubmissionAnalyticsData, dataFromApp.getProjectId());
					}
					Set<String> userIds = projectSubmissionAnalyticsData.getUserIds() == null ? new HashSet<String>() : projectSubmissionAnalyticsData.getUserIds();
					userIds.add(submittedDataObject.getUserId());
					projectSubmissionAnalyticsData.setUserIds(userIds);
					projectSubmissionAnalyticsData.setInsertTs(System.currentTimeMillis());
					projectSubmissionAnalyticsDataCrudService. insertHierarchicalData(projectSubmissionAnalyticsData);
					parentEntity = entity;
				}
				
			}
			
		}
	}
	public Map<String, String> metaDataInstanceIdToMap(String metaDataInstanceId) {

		Map<String, String> mdInstanceMap = new HashMap<>();
		String args[] = metaDataInstanceId.split(CommonConstants.DELIMITER);
		mdInstanceMap.put(CommonConstants.SUPER_APP_ID, args[0]);
		mdInstanceMap.put(CommonConstants.APP_ID, args[1]);
		mdInstanceMap.put(CommonConstants.PROJECT_ID, args[2]);
		mdInstanceMap.put(CommonConstants.FORM_TYPE, args[3]);
		mdInstanceMap.put(CommonConstants.META_DATA_VERSION, args[4]);
		return mdInstanceMap;
	}
	public ProjectSubmissionAnalyticsData createProjectSubmissionAnalyticsDBObject(UUID superAppId, UUID appId, int date , String parentEntity, String entity) {
		ProjectSubmissionAnalyticsData projectSubmissionAnalyticsData = new ProjectSubmissionAnalyticsData();
		projectSubmissionAnalyticsData.setSuperAppId(superAppId);
		projectSubmissionAnalyticsData.setApplicationId(appId);
		projectSubmissionAnalyticsData.setDate(date);
		projectSubmissionAnalyticsData.setEntity(entity);
		projectSubmissionAnalyticsData.setParentEntity(parentEntity);
		projectSubmissionAnalyticsData.setFailedSubmissionProjectIds(new HashSet<>());
		projectSubmissionAnalyticsData.setNoOfFailedSubmissions(0);
		projectSubmissionAnalyticsData.setNoOfSuccessfulSubmissions(0);
		projectSubmissionAnalyticsData.setSuccessfulSubmissionProjectIds(new HashSet<>());
		projectSubmissionAnalyticsData.setUserIds(new HashSet<>());
		projectSubmissionAnalyticsData.getInsertTs();
		return projectSubmissionAnalyticsData;
	}
	public void updateSuccessfulSubmissionCount(ProjectSubmissionAnalyticsData projectSubmissionAnalyticsData, UUID projectId) {
		Set<UUID> successfulSubmissionProjectIds  =  projectSubmissionAnalyticsData.getSuccessfulSubmissionProjectIds();
		successfulSubmissionProjectIds = successfulSubmissionProjectIds == null ? new HashSet<>(): new HashSet<>(successfulSubmissionProjectIds);
		successfulSubmissionProjectIds.add(projectId);
		projectSubmissionAnalyticsData.setSuccessfulSubmissionProjectIds(successfulSubmissionProjectIds);
		int count = projectSubmissionAnalyticsData.getNoOfSuccessfulSubmissions() == null ? 1 : projectSubmissionAnalyticsData.getNoOfSuccessfulSubmissions() + 1;
		projectSubmissionAnalyticsData.setNoOfSuccessfulSubmissions(count);
	}
	public void updateFailedSubmissionCount(ProjectSubmissionAnalyticsData projectSubmissionAnalyticsData, UUID projectId) {
		Set<UUID> failedSubmissionProjectIds  =  projectSubmissionAnalyticsData.getFailedSubmissionProjectIds();
		failedSubmissionProjectIds = failedSubmissionProjectIds == null ? new HashSet<>(): new HashSet<>(failedSubmissionProjectIds);
		failedSubmissionProjectIds.add(projectId);
		projectSubmissionAnalyticsData.setFailedSubmissionProjectIds(failedSubmissionProjectIds);
		int count = projectSubmissionAnalyticsData.getNoOfFailedSubmissions() == null ? 1 : projectSubmissionAnalyticsData.getNoOfFailedSubmissions() + 1;
		projectSubmissionAnalyticsData.setNoOfFailedSubmissions(count);
	
	}
}
