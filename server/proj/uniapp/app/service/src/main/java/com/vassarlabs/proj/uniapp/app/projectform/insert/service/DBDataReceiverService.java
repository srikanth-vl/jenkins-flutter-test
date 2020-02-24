package com.vassarlabs.proj.uniapp.app.projectform.insert.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.AppVersionMismatchException;
import com.vassarlabs.common.utils.err.DataDeletionException;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.FormSubmissionSyncPeriodExceedException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.insert.service.IDataReceiverService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.AppFormConstants;
import com.vassarlabs.proj.uniapp.constants.AppMetaConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.ErrorConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FormSubmittedDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;
import com.vassarlabs.proj.uniapp.validations.AppValidationService;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationResult;

@Component("DBDataReceiverService")
public class DBDataReceiverService 
implements IDataReceiverService {

	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@Autowired private FormSubmittedDataCrudService formInsertCrudService;
	@Autowired private ProjectMasterDataCrudService masterDataCrudService;
	@Autowired private FieldMetaDataCrudService fieldDataCrudService;
	@Autowired private AppValidationService validationService;
	@Autowired private UserProjectMapCrudService userProjectMapCrudService;
	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;
	@Autowired private ArrayInputFormatter arrayInputFormatter;
	@Autowired private SuperAppDataCrudService superAppDataCrudService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	public List<ServiceOutputObject> execute(AppFormDataSubmittedList submittedDataObject)
			throws TokenNotFoundException, TokenExpiredException, JsonParseException, JsonMappingException
			, IOException, SpelEvaluationException, InvalidInputException, ValidationException
			, DataNotFoundException, InterruptedException, ExecutionException, DataDeletionException, AppVersionMismatchException , FormSubmissionSyncPeriodExceedException{

		if (submittedDataObject == null) {
			logger.warn("Submitted Appform data list is NULL");
			return null;
		}

		if (submittedDataObject.isEmpty()) {
			logger.warn("Submitted Appform data list is EMPTY");
			return null;
		}

		List<AppFormData> formDataList = submittedDataObject.getAppFormDataList();
		List<ServiceOutputObject> serviceOutputList = new ArrayList<>();
		List<ProjectMasterData> masterDataList = new ArrayList<>();
		ServiceOutputObject outputObject = null;
		List<String> metaDataInstanceIds = new ArrayList<>();

		for (AppFormData data : formDataList) {
			metaDataInstanceIds.add(data.getMetaDataInstanceId());
		}
		Map<String, Object> superAppConfig = superAppDataCrudService.getSuperAppConfiguration(submittedDataObject.getSuperAppId());
		String currentAppVersion = null;
		if(superAppConfig.get(AppMetaConfigurationConstants.CURRENT_PLAYSTORE_APP_VERSION) != null) {
			currentAppVersion = superAppConfig.get(AppMetaConfigurationConstants.CURRENT_PLAYSTORE_APP_VERSION).toString();
		}
		Boolean acceptDataFromOlderApp = true;
		if(superAppConfig.get(AppMetaConfigurationConstants.ACCEPT_DATA_FROM_OLDER_APP) != null) {
			 acceptDataFromOlderApp=Boolean.parseBoolean(superAppConfig.get(AppMetaConfigurationConstants.ACCEPT_DATA_FROM_OLDER_APP).toString());
		}
		// Map of project ID to state of the project (master data)
		Map<UUID, ProjectMasterData> projectIdToProjectStateMasterData = masterDataCrudService.findDataForProjectsState(
				submittedDataObject.getSuperAppId(), submittedDataObject.getAppId()
				, formDataList.stream().filter(Objects::nonNull).map(AppFormData::getProjectId).collect(Collectors.toList()));
		// Map of project ID to list of fields properties or MD
		Map<UUID, List<FieldMetaData>> projectIdToFieldsData = fieldDataCrudService.getProjectIdToFieldsData(
				submittedDataObject.getSuperAppId(), submittedDataObject.getAppId(), metaDataInstanceIds);
		// Map of data type to formatters (formatter string)
		Map<String, String> dataTypeformatter = applicationMetaDataCrudService.getFormmaterList(submittedDataObject.getSuperAppId(), submittedDataObject.getAppId());
		boolean isAnyProjectDeleted = false;
		for (AppFormData formData : formDataList) {
			Map<String, String> other_properties = formData.getOtherParams();
			if(other_properties!= null && other_properties.get("app_version") != null &&  currentAppVersion!= null && !currentAppVersion.isEmpty() &&
					!other_properties.get("app_version").equals(currentAppVersion) && !acceptDataFromOlderApp ) {
				
				throw new AppVersionMismatchException(ErrorConstants.APP_VERSION_MISMATCH_EXCEPTION);
				
			}
			ProjectMasterData projectStateMasterData = projectIdToProjectStateMasterData.get(formData.getProjectId());

			if (projectStateMasterData != null && projectStateMasterData.getValue().equals(ProjectStates.DELETED.getValue()) 
					&& projectStateMasterData.getInsertTs() < formData.getTimeStamp()) {
				isAnyProjectDeleted = true;
				continue;
			}
			
			List<FieldMetaData> appFieldsData = null;
			
			if (projectIdToFieldsData.containsKey(formData.getProjectId())) {
				appFieldsData = projectIdToFieldsData.get(formData.getProjectId());
			} else if (projectIdToFieldsData.containsKey(UUIDUtils.getDefaultUUID())) {
				appFieldsData = projectIdToFieldsData.get(UUIDUtils.getDefaultUUID());
			}

			if (appFieldsData == null) {
				throw new ValidationException("No field meta data found for project");
			}

			List<String> mandatoryFields = new ArrayList<>();
			List<String> arrayKeys = new ArrayList<>();
			
			
			int formType = Integer.parseInt(formData.getMetaDataInstanceId().split(CommonConstants.DELIMITER)[3]);
			Map<String, FieldMetaData> keyToFieldDataMap = getFieldMetaDataBasedOnFormType(appFieldsData, formType, formData, mandatoryFields, arrayKeys);
		
			ValidationResult validationResult;
			if(!arrayKeys.isEmpty()) {
				validationResult = arrayInputFormatter.processAndValidateArrayValues(submittedDataObject.getSuperAppId(), submittedDataObject.getAppId(), formData, keyToFieldDataMap, mandatoryFields, dataTypeformatter);
			} else {
				validationResult = validationService.validateData(submittedDataObject.getSuperAppId(), submittedDataObject.getAppId(), formData, keyToFieldDataMap, mandatoryFields, dataTypeformatter);
			}
			
			APITypes apiType = APITypes.SUBMIT; 
			if (validationResult.isValid()) {
				if (formType == FormTypes.INSERT.getValue()) {
					long insertTs = formData.getTimeStamp();
					int date = DateUtils.getYYYYMMdd(insertTs);
					List<ProjectMasterData> masterList = createAndInsertProjectMasterData(submittedDataObject, formData, insertTs, date);
					updateUserProjectMapping(submittedDataObject, formData, insertTs);
					if (masterList != null && !masterList.isEmpty()) {
//						ProjectMasterData masterData = new ProjectMasterData();
//						masterData.setApplicationId(submittedDataObject.getAppId());
//						masterData.setSuperAppId(submittedDataObject.getSuperAppId());
//						masterData.setKey(MasterDataKeyNames.STATE_KEY);
//						masterData.setValue(ProjectStates.NEW.getValue());
//						masterData.setDate(date);
//						masterData.setInsertTs(insertTs);
//						masterData.setProjectId(formData.getProjectId());
//						masterList.add(masterData);
						masterDataCrudService.insertListOfProjectMasterData(masterList);
					}
					apiType = APITypes.INSERT ;
				}
				createandInsertFormData(formData, submittedDataObject.getSuperAppId(),
						submittedDataObject.getUserId(), submittedDataObject.getAppId(),
						submittedDataObject.getTokenId(), keyToFieldDataMap, dataTypeformatter);
				masterDataList.add(updateProjectSyncTS(formData, submittedDataObject.getSuperAppId(),
						submittedDataObject.getAppId()));
			}

			formData.getFormFieldValuesList().stream().forEach(data -> {
				data.setKey(data.getKey().substring(Optional.of(data.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
						.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()));
			});
			List<String> errorMessages = null;
			if(validationResult.getKeyToErrorMessages() != null) {
				errorMessages = new ArrayList<>();
				for(String key : validationResult.getKeyToErrorMessages().keySet())
					errorMessages.addAll(validationResult.getKeyToErrorMessages().get(key));
			}
			UserTrackingObject trackingObject = new UserTrackingObject(submittedDataObject.getSuperAppId(), submittedDataObject.getAppId(), submittedDataObject.getUserId(), submittedDataObject.getTokenId(),
					apiType, "Data submit for project " + formData.getProjectId(), objectMapper.writeValueAsString(formData), errorMessages, validationResult.isValid(), formData.getTimeStamp());
			outputObject = new ServiceOutputObject();
			outputObject.setTrackingObject(trackingObject);
			outputObject.setSuccessful(validationResult.isValid());
			outputObject.setKeyToErrorMessages(validationResult.getKeyToErrorMessages());
			serviceOutputList.add(outputObject);
		}
		if(isAnyProjectDeleted) {
			throw new DataDeletionException(ErrorConstants.PROJECT_DELETION_ERROR_MESSAGE);
		}
		// Update Last Sync Time Stamp of each of the projects in master data table
		masterDataCrudService.insertListOfProjectMasterData(masterDataList);
		return serviceOutputList;
	}

	private Map<String, FieldMetaData> getFieldMetaDataBasedOnFormType(List<FieldMetaData> appFieldsData, int formType, AppFormData formData, List<String> mandatoryFields, List<String> arrayKeys) {
		Map<String, FieldMetaData> keyToFieldDataMap = new HashMap<>();
		appFieldsData.stream().forEach(data -> {
			if (data.isMandatory()) {
				mandatoryFields.add(data.getKey());
			}
			if (data.getKeyType() == KeyTypes.DEFAULT_KEY.getValue()) {
				FormFieldValues projectValidationObject = new FormFieldValues();
				projectValidationObject.setKey(data.getKey());
				projectValidationObject.setDataType(data.getDataType());
				formData.getFormFieldValuesList().add(projectValidationObject);
			}
			if(formType == FormTypes.UPDATE.getValue()) {
				if (data.getKeyType() == KeyTypes.APP_DATA_KEY.getValue()
						|| data.getKeyType() == KeyTypes.DEFAULT_KEY.getValue()) {
					keyToFieldDataMap.put(data.getKey(), data);
				}
			} else if(formType == FormTypes.INSERT.getValue()) {
				if (data.getKeyType() == KeyTypes.MASTER_DATA_KEY.getValue()
						|| data.getKeyType() == KeyTypes.DEFAULT_KEY.getValue()) {
					keyToFieldDataMap.put(data.getKey(), data);
				}
			}
			if(data.getDataType().equals(CommonConstants.DATATYPE_JSON_ARRAY)) {
				arrayKeys.add(data.getKey());
			}
		});
		
		
		return keyToFieldDataMap;
	}

	private void updateUserProjectMapping(AppFormDataSubmittedList submittedDataObject, AppFormData formData, long insertTs) {

		UUID superAppId = submittedDataObject.getSuperAppId();
		UUID appId = submittedDataObject.getAppId();
		String userId = submittedDataObject.getUserId();
		UserProjectMapping userProjectMap = userProjectMapCrudService.findUserProjectMappingByPrimaryKey(superAppId, appId, userId, UserPriorities.Default.getValue());
		List<UUID> projectIds = new ArrayList<>();
		if (userProjectMap != null) {
			projectIds = userProjectMap.getProjectList();
		}
		projectIds.add(formData.getProjectId());
		UserProjectMapping userProjMap = new UserProjectMapping(superAppId, appId, userId, UserPriorities.Default.getValue(), projectIds, insertTs);
		userProjectMapCrudService.insertUserProjectMappingData(userProjMap);
	}

	private List<ProjectMasterData> createAndInsertProjectMasterData(AppFormDataSubmittedList submittedDataObject, AppFormData formData, Long insertTs, int date) {

		List<FieldMetaData> fieldDataList = fieldDataCrudService.getFieldData(submittedDataObject.getSuperAppId(), submittedDataObject.getAppId(), formData.getMetaDataInstanceId());
		List<ProjectMasterData> masterDataList = new ArrayList<>();
		List<FormFieldValues> fieldValues = formData.getFormFieldValuesList();
		for (FieldMetaData fieldData : fieldDataList) {
			if (fieldData.getKeyType() == KeyTypes.MASTER_DATA_KEY.getValue()) {
				String key = fieldData.getKey();
				for (FormFieldValues fieldValue : fieldValues) {
					if (fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
							.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()).equalsIgnoreCase(key)) {
						ProjectMasterData masterData = getProjectMasterData(fieldValue, formData, submittedDataObject, insertTs, date);
						masterDataList.add(masterData);
					}
				}
			}
		}
		return masterDataList;
	}

	private ProjectMasterData getProjectMasterData(FormFieldValues fieldValue, AppFormData formData, AppFormDataSubmittedList submittedDataObject, Long insertTs, int date) {
		ProjectMasterData masterData = new ProjectMasterData();
		masterData.setApplicationId(submittedDataObject.getAppId());
		masterData.setSuperAppId(submittedDataObject.getSuperAppId());
		masterData.setKey(fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
				.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()));
		if(fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
				.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()).equals(MasterDataKeyNames.STATE_KEY)) {
			masterData.setDate(IFileUploadConstants.DEFAULT_INTEGER_VALUE);
		} else {
			masterData.setDate(date);
		}
		
		masterData.setValue(fieldValue.getValue());
		
		masterData.setInsertTs(insertTs);
		masterData.setProjectId(formData.getProjectId());
		return masterData;
	}

	private void createandInsertFormData(AppFormData formData, UUID superAppId, String userId, UUID appId, UUID tokenId, Map<String, FieldMetaData> keyToFieldDataMap, Map<String, String> dataTypeformatter) throws InterruptedException, ExecutionException, IOException {

		List<FormSubmitData> projectDataList = new ArrayList<>();
		List<FormFieldValues> fieldValuesList = formData.getFormFieldValuesList();
		Map<String, FormFieldValues> keyToFormInsertData = fieldValuesList.stream().collect(Collectors.toMap(fieldValues -> fieldValues.getKey().substring(Optional.of(fieldValues.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
				.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()), Function.identity()));

		for(FormFieldValues fieldValue : fieldValuesList) {

			FieldMetaData metaData = keyToFieldDataMap.get(fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
					.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()));

			if(metaData.getKeyType() == KeyTypes.APP_DATA_KEY.getValue()) {
				FormSubmitData projectData = getFormSubmitData(fieldValue, formData, keyToFormInsertData, keyToFieldDataMap, superAppId, appId, userId, tokenId, dataTypeformatter);
				projectDataList.add(projectData);
			}
		}

		formInsertCrudService.insertListOfFormSubmittedData(projectDataList);
	}

	private FormSubmitData getFormSubmitData(FormFieldValues fieldValue, AppFormData formData, Map<String, FormFieldValues> keyToFormInsertData, Map<String, FieldMetaData> keyToFieldDataMap, UUID superAppId,
			UUID appId, String userId, UUID tokenId, Map<String, String> dataTypeformatter) {

		FormSubmitData projectData = new FormSubmitData();
		projectData.setApplicationId(appId);
		projectData.setSuperAppId(superAppId);
		projectData.setProjectId(formData.getProjectId());
		projectData.setFormInstanceId(formData.getFormInstanceId());
		projectData.setUserId(userId);
		projectData.setTokenId(tokenId);
		projectData.setUserType(UserPriorities.valueOf(formData.getUserType()).getValue());

		if(fieldValue.getKey().indexOf(AppFormConstants.KEY_SEPARATOR) == -1)
			projectData.setDate(DateUtils.getYYYYMMdd(formData.getTimeStamp()));
		else {
			String datePattern  = dataTypeformatter.get(CommonConstants.DATATYPE_DATE);
			datePattern = datePattern == null || datePattern.isEmpty() ? AppFormConstants.DEFAULT_DATE_FORMAT : datePattern;
			projectData.setDate(DateUtils.getYYYYMMdd(
					keyToFormInsertData.get(fieldValue.getKey().substring(0, fieldValue.getKey().indexOf(AppFormConstants.KEY_SEPARATOR))).getValue(), 
					datePattern));
		}
		if(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR) == fieldValue.getKey().indexOf(AppFormConstants.KEY_SEPARATOR))
			projectData.setTimestamp(formData.getTimeStamp());
		else {
			String datePattern  = dataTypeformatter.get(CommonConstants.DATATYPE_DATE);
			datePattern = datePattern == null || datePattern.isEmpty() ? AppFormConstants.DEFAULT_DATE_FORMAT : datePattern;
			long timeInSeconds = DateUtils.changeHHMMSSIntoSecond(keyToFormInsertData.get(fieldValue.getKey().substring(fieldValue.getKey().indexOf(AppFormConstants.KEY_SEPARATOR) + AppFormConstants.KEY_SEPARATOR.length(), fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))).getValue());
			projectData.setTimestamp(DateUtils.getTimestamp(
					keyToFormInsertData.get(fieldValue.getKey().substring(0, fieldValue.getKey().indexOf(AppFormConstants.KEY_SEPARATOR))).getValue(), 
					String.valueOf(timeInSeconds), datePattern));
		}

		projectData.setKey(fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
				.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()));
		projectData.setMetaDataInstanceId(formData.getMetaDataInstanceId());
		projectData.setDataType(keyToFieldDataMap.get(fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
				.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get())).getDataType());
		projectData.setDbInsertTs(System.currentTimeMillis());
		projectData.setValue(fieldValue.getValue());
		String uom = fieldValue.getUom();
		String defaultUom = keyToFieldDataMap.get(projectData.getKey()) == null ? null : keyToFieldDataMap.get(projectData.getKey()).getUom();
		uom = (uom == null || uom.isEmpty()) ? defaultUom : uom;
		String dataType =  projectData.getDataType();
		if(uom != null && !uom.isEmpty() && dataType != null && (dataType.equalsIgnoreCase(CommonConstants.DATATYPE_DOUBLE)|| dataType.equalsIgnoreCase(CommonConstants.DATATYPE_LONG) || dataType.equalsIgnoreCase(CommonConstants.DATATYPE_INTEGER))) {
			projectData.setUom(uom);
		}
		return projectData;
	}

	private ProjectMasterData updateProjectSyncTS(AppFormData formData, UUID superAppId, UUID appId) throws InvalidInputException {

		ProjectMasterData masterData = new ProjectMasterData();
		masterData.setSuperAppId(superAppId);
		masterData.setApplicationId(appId);
		masterData.setProjectId(formData.getProjectId());
		masterData.setDate(CommonConstants.DEFAULT_DATE);
		masterData.setKey(MasterDataKeyNames.LAST_SYNC_TS);
		masterData.setValue(String.valueOf(formData.getTimeStamp()));
		masterData.setInsertTs(System.currentTimeMillis());
		return masterData;
	}
}
