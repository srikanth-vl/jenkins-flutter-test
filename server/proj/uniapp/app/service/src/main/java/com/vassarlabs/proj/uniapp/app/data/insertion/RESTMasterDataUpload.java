package com.vassarlabs.proj.uniapp.app.data.insertion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.exceptions.OperationTimedOutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.dsp.convertor.RawDataToDBObjectConvertor;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.validations.ValidationProcessingService;

@Service
public class RESTMasterDataUpload {

	@Autowired private RawDataToDBObjectConvertor rawDataToDBObjectConvertor;
	@Autowired private FieldMetaDataCrudService fieldMetaDataCrudService;
	@Autowired private ValidationProcessingService validationProcessingService;

	@Autowired private RefreshUserProjectMapping refreshUserProjectMapping;
	@Autowired private DataUploadUtils dataUploadUtils;

	@Autowired
	protected IVLLogService logFactory;

	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;

	ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, List<IErrorObject>> uploadMasterData(AppFormDataSubmittedList appFormSubmittedDataList) throws InvalidInputException, IOException, DSPException, InterruptedException {
		
		UUID superAppId = appFormSubmittedDataList.getSuperAppId();
		UUID appId = appFormSubmittedDataList.getAppId();
		Map<String, List<IErrorObject>> externalIdToErrorList = new HashMap<>();
		int retryCount  = 0;
			try {
			Map<String, String> dataTypeformatter = applicationMetaDataCrudService.getFormmaterList(superAppId, appId);
	
			List<AppFormData> appFormDataList = appFormSubmittedDataList.getAppFormDataList();
	
			List<String> externalProjectIds = getAllExternalProjectIds(appFormDataList);
			
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMap = rawDataToDBObjectConvertor.generateExternalToInternalIdMap(superAppId, appId, externalProjectIds);
	
			List<UUID> internalProjectIdList = externalToInternalIdMap.values().stream().map(ProjectExternalInternalMapData::getProjectId).collect(Collectors.toList());
			internalProjectIdList.add(UUIDUtils.getDefaultUUID());
			
			List<FieldMetaData> fieldMetaDataList = fieldMetaDataCrudService.findLatestFieldMetaDataForProjects(superAppId, appId, internalProjectIdList, FormTypes.UPDATE.getValue());
	
	
			Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataList = fieldMetaDataList.stream().collect(Collectors.groupingBy(FieldMetaData::getProjectId));
			
			Map<String, Map<Integer, UserProjectMapping>> userIdToTypeToProjectsMap = new HashMap<>();
			
			int indexCount = 0;
			List<ProjectMasterData> masterDataList = new ArrayList<>();
			
			for(AppFormData appFormData : appFormDataList) {
				indexCount++;
				List<IErrorObject> errorList = new ArrayList<>(); 
				int date = DateUtils.getYYYYMMdd(System.currentTimeMillis());
				String userType = appFormData.getUserType();
				
				List<String> usersAssigned = null;
				if(userType != null && !userType.isEmpty())
					usersAssigned = objectMapper.readValue(userType, new TypeReference<List<String>>() {});
				List<FormFieldValues> fieldValuesList = appFormData.getFormFieldValuesList();
				String externalProjectId = fieldValuesList.stream().filter(p -> p.getKey().equalsIgnoreCase(MasterDataKeyNames.EXTERNAL_PROJECT_ID)).findFirst().get().getValue();
				Map<String, String> keyToValueData = fieldValuesList.stream().filter((a) -> a.getValue() != null && a.getKey()!=null).collect(Collectors.toMap(FormFieldValues::getKey, FormFieldValues::getValue));
				
				if(externalProjectId == null) {
					logger.error("No external Id found for project");
					createErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No project external Id found!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE, appFormData, indexCount, errorList);
					externalIdToErrorList.put(CommonConstants.DEFAULT_KEY, errorList);
					continue;
				}
				UUID projectId = externalToInternalIdMap.get(externalProjectId).getProjectId();
				if(projectId == null) {
					logger.error("No internal Id found for project" + externalProjectId);
					createErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No project internal Id found!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE, appFormData, indexCount, errorList);
					externalIdToErrorList.put(externalProjectId, errorList);
					continue;
				}
				List<FieldMetaData> metaDataList = projectIdToFieldMetaDataList.get(projectId);
				if(metaDataList == null || metaDataList.isEmpty()) {
					metaDataList = projectIdToFieldMetaDataList.get(UUIDUtils.getDefaultUUID());
				}
				
				Map<String, FieldMetaData> keyToFieldMetaDataMap = validateKeysFromFieldMetaData(keyToValueData.keySet(), superAppId, appId, projectId, metaDataList);
				
				List<ProjectMasterData> masterDataListForProject = getMasterDataList(superAppId, appId, projectId, date, keyToFieldMetaDataMap, keyToValueData, dataTypeformatter, appFormData, errorList, indexCount);
				
				if(usersAssigned != null && !usersAssigned.isEmpty()) {
					dataUploadUtils.populateUserProjectMapping(superAppId, appId, projectId, usersAssigned, null, userIdToTypeToProjectsMap, errorList, null);
				}
				
				if(!errorList.isEmpty()) {
					externalIdToErrorList.put(externalProjectId, errorList);
				} 
				// If project state is not provided, by default add project state as "In Progress"
				if(!keyToValueData.containsKey(MasterDataKeyNames.STATE_KEY)) {
					ProjectMasterData masterData = rawDataToDBObjectConvertor.getMasterDataCommonFields(superAppId, appId, CommonConstants.DEFAULT_DATE, projectId);
					masterData.setKey(MasterDataKeyNames.STATE_KEY);
					masterData.setValue(ProjectStates.getDefaultProjectState().getValue());
					masterDataListForProject.add(masterData);
				}
				boolean result = errorList.stream().anyMatch(x->x.getRowUploadStatus().equals(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				if(!result) {
					masterDataList.addAll(masterDataListForProject);
				}
			}
			dataUploadUtils.insertRecordsIntoDatabase(masterDataList, userIdToTypeToProjectsMap, externalToInternalIdMap);
			//refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId);
		} catch(CassandraReadTimeoutException | CassandraWriteTimeoutException | OperationTimedOutException | CassandraConnectionFailureException e) {
			if(retryCount++ >= CommonConstants.MAX_RETRIES) {
				logger.error("Max retries reached... Could not insert the data");
				throw new DSPException();
			}
			logger.debug("Sleeping for 3 seconds.. before retrying for " + retryCount + " time....");
			Thread.sleep(3000);
		}
		return externalIdToErrorList;
	}

	private List<ProjectMasterData> getMasterDataList(UUID superAppId, UUID appId, UUID projectId, int date, Map<String, FieldMetaData> keyToFieldMetaDataMap, Map<String, String> keyToValueData, Map<String, String> dataTypeformatter, 
			AppFormData appFormData, List<IErrorObject> errorList, int indexCount) throws IOException {

		List<ProjectMasterData> masterDataListForAProject = new ArrayList<>();
		for(String key : keyToValueData.keySet()) {
			ProjectMasterData masterData = rawDataToDBObjectConvertor.getMasterDataCommonFields(superAppId, appId, date, projectId);	
			masterData.setKey(key);
			String value = keyToValueData.get(key);
			FieldMetaData metaData = keyToFieldMetaDataMap.get(masterData.getKey());
			if(metaData != null) {

				String formatter = dataTypeformatter.get(metaData.getDataType());
				Properties additionalProperties = new Properties();
				if(formatter != null) {
					additionalProperties.setProperty(metaData.getDataType(), formatter);
				}
				value = validationProcessingService.parseAndPerformValidations(key, keyToFieldMetaDataMap, keyToValueData, formatter, additionalProperties, errorList);
				masterData.setValue(value);
				masterDataListForAProject.add(masterData);
			}
		}
		return masterDataListForAProject;
	}

	private void createErrorObject(String errorCodeStr, int errorCode, String errorMessage,
			String uploadStatus, AppFormData appFormData, int indexCount, List<IErrorObject> errorList) throws JsonProcessingException {
		IErrorObject errorObject = new ErrorObject();
		errorObject.setErrorCode(errorCode);
		errorObject.setErrorMessage(errorMessage);
		errorObject.setErrorType(errorCodeStr);
		errorObject.setRowUploadStatus(uploadStatus);
		errorObject.setRowData(objectMapper.writeValueAsString(appFormData));
		errorObject.setLineNo(indexCount);
		errorList.add(errorObject);
	}

	private Map<String, FieldMetaData> validateKeysFromFieldMetaData(Set<String> keySet, UUID superAppId, UUID appId, UUID projectId, List<FieldMetaData> fieldMetaDataList) throws InvalidInputException {
		if(fieldMetaDataList == null) {
			logger.error("Please upload field meta data first to allow upload of project master data");
			throw new InvalidInputException();
		}
		Map<String, FieldMetaData> keyToFieldMetaDataMap = new HashMap<>();
		List<String> keysFromMetaData = new ArrayList<>();
		// Validate if all master data keys are present in project master data
		fieldMetaDataList.stream().forEach(object -> {
			if (object.getKeyType() == KeyTypes.MASTER_DATA_KEY.getValue()) {
				keysFromMetaData.add(object.getKey());
				keyToFieldMetaDataMap.put(object.getKey(), object);
			}
		});
		Set<String> set = new HashSet<>(keySet);
		set.remove(MasterDataKeyNames.EXTERNAL_PROJECT_ID);
		set.removeAll(keysFromMetaData);
		if(!set.isEmpty()){
			keySet.removeAll(keysFromMetaData);
			logger.error("Not all Field meta data keys found during uploading master data - Missing keys in field_meta_data :: " + keySet);
			throw new InvalidInputException();
			
		}
		return keyToFieldMetaDataMap;
	}

	private List<String> getAllExternalProjectIds(List<AppFormData> appFormDataList) {
		List<String> externalProjectIdList = new ArrayList<>();
		for(AppFormData formData : appFormDataList) {
			String externalProjectId = formData.getFormFieldValuesList().stream().filter(p -> p.getKey().equalsIgnoreCase(MasterDataKeyNames.EXTERNAL_PROJECT_ID)).findFirst().get().getValue();
			externalProjectIdList.add(externalProjectId);
		}
		return externalProjectIdList;
	}
}
