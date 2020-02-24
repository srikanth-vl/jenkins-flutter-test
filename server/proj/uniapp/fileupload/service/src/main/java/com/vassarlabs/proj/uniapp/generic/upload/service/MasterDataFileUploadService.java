package com.vassarlabs.proj.uniapp.generic.upload.service;

import java.io.IOException;
import java.lang.reflect.Field;
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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.filereader.service.api.ICSVFileReaderService;
import com.vassarlabs.common.fileupload.event.api.IFileUploadEvent;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadDetails;
import com.vassarlabs.common.fileupload.pojo.api.IFileUploadResult;
import com.vassarlabs.common.fileupload.pojo.impl.FileUploadResult;
import com.vassarlabs.common.filewriter.pojo.api.IFileWriterDetails;
import com.vassarlabs.common.filewriter.service.api.ICSVFileWriterService;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.ObjectNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.app.update.projects.status.service.ProjectDisableService;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.dsp.convertor.RawDataToDBObjectConvertor;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.fileupload.object.KeyFields;
import com.vassarlabs.proj.uniapp.validations.ValidationProcessingService;


@Component
public class MasterDataFileUploadService 
	extends UniAppFileUpload {

	@Autowired private ICSVFileReaderService fileReaderService;
	@Autowired private ICSVFileWriterService fileWriterService;

	@Autowired private ProjectMasterDataCrudService masterDataCrudService;
	@Autowired private UserProjectMapCrudService userProjectMappingCrudService;
	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;	
	@Autowired private FieldMetaDataCrudService fieldMetaDataCrudService;

	@Autowired private UserProjectMapUtils userProjMappingUtils;
	@Autowired private ProjectDisableService projectDisableService;
	@Autowired private RefreshUserProjectMapping refreshUserProjectMapping;
	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;
	@Autowired private RawDataToDBObjectConvertor rawDataToDBObjectConvertor;
	@Autowired private ValidationProcessingService validationProcessingService;

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	protected IVLLogService logFactory;

	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}

	@EventListener
	public <E> void fileUploadEvent(IFileUploadEvent<E> fileUploadEvent) throws DSPException, ObjectNotFoundException, JsonProcessingException, InvalidInputException, ValidationException {
		if(fileUploadEvent.getFileUploadDetails().getClassName().contains(CommonConstants.MASTER_DATA_CLASSNAME)) {
			try {
				logger.debug("Starting master data file upload");
				Map<E, List<IErrorObject>> rowToErrorListMap = fileUploadEvent.getFileUploadResult().getDataToErrorListMap();
				List<ProjectMasterData> masterDataList = new ArrayList<>();
				Map<String, Map<Integer, UserProjectMapping>> userProjectMappingMap = new HashMap<>();
				Map<String, String> masterDataNameToKeyNameMap = new HashMap<>();
				Map<String, List<KeyFields>> keyNameToKeyObject = new HashMap<>();
				Properties properties = fileUploadEvent.getFileUploadDetails().getProperties();
				String JsonConfiguration = properties.getProperty(CommonConstants.JSON_CONFIG);
				String date = properties.getProperty(IFileUploadConstants.MasterDataUploadConstants.DATE);
				JsonNode node = objectMapper.readTree(JsonConfiguration);
				UUID superAppId = getSuperAppId(JsonConfiguration);
				UUID appId = getAppId(JsonConfiguration);
				JsonNode keyArray = node.findValue(IFileUploadConstants.MasterDataUploadConstants.KEYS);
				if(keyArray == null) {
					throw new ValidationException(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No Keys information found in Json!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				}
				readJsonNode(keyArray, masterDataNameToKeyNameMap, keyNameToKeyObject);
				JsonNode userConfigNode = node.get(IFileUploadConstants.MasterDataUploadConstants.USER_MAPPING).get(IFileUploadConstants.MasterDataUploadConstants.MAPPING);
				List<String> externalProjIds = getAllExternalProjectIds(fileUploadEvent.getDataList(), masterDataNameToKeyNameMap);
				Map<String, ProjectExternalInternalMapData> externalToInternalIdMap = rawDataToDBObjectConvertor.generateExternalToInternalIdMap(superAppId, appId, externalProjIds);
				Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataMap = getProjectIdToFieldMetaDataListMap(superAppId, appId, externalToInternalIdMap);
				Map<String, String> dataTypeformatter = applicationMetaDataCrudService.getFormmaterList(superAppId, appId);
				for(E object : fileUploadEvent.getDataList()) {
					List<ProjectMasterData> masterDataListForProject = new ArrayList<>();
					List<IErrorObject> errors = populateMasterData(object, masterDataListForProject, keyArray, date, superAppId, appId, externalToInternalIdMap, masterDataNameToKeyNameMap, keyNameToKeyObject, projectIdToFieldMetaDataMap, dataTypeformatter);
					if(!errors.isEmpty()) {
						rowToErrorListMap.put(object, errors);
					}
					boolean result = errors.stream().anyMatch(x->x.getRowUploadStatus().equals(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					if(!result) {
						masterDataList.addAll(masterDataListForProject);
						if(userConfigNode != null) {
							UUID projectId = masterDataListForProject.get(masterDataListForProject.size()-1).getProjectId(); // Process last update project id
							List<IErrorObject> errorInUserMapping = userProjMappingUtils.populateUserProjectMappingData(object, userProjectMappingMap, userConfigNode, superAppId, appId, externalToInternalIdMap, projectId);
							if(errorInUserMapping != null && !errorInUserMapping.isEmpty()) {
								masterDataList.removeAll(masterDataListForProject);
								rowToErrorListMap.put(object, errorInUserMapping);
							} 
						}
					}
					masterDataListForProject = null;
				}
				insertRecordsIntoDatabase(masterDataList, userProjectMappingMap, externalToInternalIdMap);
				if(!rowToErrorListMap.isEmpty()) {
					IFileWriterDetails fileWriterDetails = createFileWriterObject(fileUploadEvent);
					fileWriterService.writeCSVFile(fileWriterDetails, rowToErrorListMap);
				}
				consolidateCountOfRecords(rowToErrorListMap.size(),masterDataList.size(), fileUploadEvent.getDataList().size(), fileUploadEvent.getFileUploadDetails(), fileUploadEvent.getFileUploadResult());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Map<UUID, List<FieldMetaData>> getProjectIdToFieldMetaDataListMap(UUID superAppId, UUID appId, Map<String, ProjectExternalInternalMapData> externalToInternalIdMap) {
		List<UUID> projectInternalIds = new ArrayList<>();
		for(String extId : externalToInternalIdMap.keySet()) {
			projectInternalIds.add(externalToInternalIdMap.get(extId).getProjectId());
		}
		projectInternalIds.add(UUIDUtils.getDefaultUUID());
		List<FieldMetaData> fieldMetaDataList = fieldMetaDataCrudService.findLatestFieldMetaDataForProjects(superAppId, appId, projectInternalIds, FormTypes.UPDATE.getValue());
		Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataList = fieldMetaDataList.stream().collect(Collectors.groupingBy(FieldMetaData::getProjectId));
		return projectIdToFieldMetaDataList;
	}

	private List<IErrorObject> populateMasterData(Object masterDataInfo, List<ProjectMasterData> masterDataListForProject, JsonNode keyArray, String date, UUID superAppId, UUID appId, 
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMapData, Map<String, String> masterDataNameToKeyNameMap, Map<String, List<KeyFields>> keyNameToKeyObject, Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataMap, Map<String, String> dataTypeformatter) throws JsonParseException, JsonMappingException, 
	IOException, IllegalArgumentException, IllegalAccessException, InvalidInputException {

		List<IErrorObject> errorList = new ArrayList<>();
		String extId = getExternalIdForObject(masterDataInfo, masterDataNameToKeyNameMap);
		if(extId == null) {
			logger.error("No external Id found for project");
			errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No project external Id found!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
			return errorList;
		}
		UUID projectId = externalToInternalIdMapData.get(extId).getProjectId();
		if(projectId == null) {
			logger.error("No internal Id found for project" + extId);
			errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No project internal Id found!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
			return errorList;
		}
		List<FieldMetaData> metaDataList = projectIdToFieldMetaDataMap.get(projectId);
		if(metaDataList == null || metaDataList.isEmpty()) {
			metaDataList = projectIdToFieldMetaDataMap.get(UUIDUtils.getDefaultUUID());
		}
		Map<String, FieldMetaData> keyToFieldMetaDataMap = validateKeysFromFieldMetaData(masterDataNameToKeyNameMap.keySet(), superAppId, appId, projectId, metaDataList);
		Map<String, String> keyToValueMap = getValuesFromFile(masterDataInfo, keyNameToKeyObject, errorList, keyToFieldMetaDataMap);

		for(String key : keyToValueMap.keySet()) {
			ProjectMasterData masterData = rawDataToDBObjectConvertor.getMasterDataCommonFields(superAppId, appId, Integer.parseInt(date), projectId);	
			masterData.setKey(key);
			String value = keyToValueMap.get(key);
			FieldMetaData metaData = keyToFieldMetaDataMap.get(key);
			if(metaData != null) {
				String formatter = dataTypeformatter.get(metaData.getDataType());
				Properties additionalProperties = new Properties();
				if( formatter != null)
					additionalProperties.setProperty(metaData.getDataType(), formatter);
				value = validationProcessingService.parseAndPerformValidations(key, keyToFieldMetaDataMap, keyToValueMap, formatter, additionalProperties, errorList);
				masterData.setValue(value);
				masterDataListForProject.add(masterData);
			}
		}
		if(!masterDataNameToKeyNameMap.containsKey(MasterDataKeyNames.STATE_KEY)) {
			ProjectMasterData masterData = rawDataToDBObjectConvertor.getMasterDataCommonFields(superAppId, appId, CommonConstants.DEFAULT_DATE, projectId);
			masterData.setKey(MasterDataKeyNames.STATE_KEY);
			masterData.setValue(ProjectStates.getDefaultProjectState().getValue());
			masterDataListForProject.add(masterData);
		}
		return errorList;
	}


	private Map<String, String> getValuesFromFile(Object masterDataInfo, Map<String, List<KeyFields>> keyNameToKeyObject, List<IErrorObject> errorList, Map<String, FieldMetaData> keyToFieldMetaDataMap) throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> keyToValueMap = new HashMap<>();
		for (Field f : masterDataInfo.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if(keyNameToKeyObject.containsKey(f.getName())) {
				List<KeyFields> infoObjectList = keyNameToKeyObject.get(f.getName());
				for(KeyFields infoObject : infoObjectList) {
					Object value = f.get(masterDataInfo);
					if(value == null 
							|| String.valueOf(value).isEmpty() || value.equals("null")) {
						if(infoObject.getDefaultValue() != null && !infoObject.getDefaultValue().isEmpty()|| infoObject.getDefaultValue().equals("null")) {
							value  = infoObject.getDefaultValue();
//							keyToValueMap.put(infoObject.getMasterDataName(), infoObject.getDefaultValue());
						}
					}
					if(value == null 
							|| String.valueOf(value).isEmpty() || value.equals("null")) {
						if(infoObject.getDefaultValue() != null && !infoObject.getDefaultValue().isEmpty()|| infoObject.getDefaultValue().equals("null")) {
							keyToValueMap.put(infoObject.getMasterDataName(), infoObject.getDefaultValue());
						}
						logger.error("No value found for key - " + f.getName());
						if(keyToFieldMetaDataMap.containsKey(infoObject.getMasterDataName())) {
							FieldMetaData metaData = keyToFieldMetaDataMap.get(infoObject.getMasterDataName());
							if(metaData.isMandatory()) {
								errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.WRONG_ENTRY, "Field is mandatory but no value is provided", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
								continue;
							}
						}
						errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.NO_DATA, "No value found for key - " + f.getName(), IErrorObject.ROW_PARTIAL_UPLOADED_MESSAGE));
						continue;
					}
					if(infoObject.getMasterDataName().equals(CommonConstants.DATATYPE_GEOTAG)
							&& keyToValueMap.containsKey(CommonConstants.DATATYPE_GEOTAG)) {
						value = keyToValueMap.get(CommonConstants.DATATYPE_GEOTAG) + "," + String.valueOf(value);
					} if(infoObject.getMasterDataName().equals(CommonConstants.DATATYPE_BBOX)
							&& keyToValueMap.containsKey(CommonConstants.DATATYPE_BBOX)) {
						value = keyToValueMap.get(CommonConstants.DATATYPE_BBOX) + "," + String.valueOf(value);
					} if(infoObject.getMasterDataName().equals(CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE)
							&& keyToValueMap.containsKey(CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE)) {
						value = keyToValueMap.get(CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE) + "," + String.valueOf(value);
					} 
					keyToValueMap.put(infoObject.getMasterDataName(), String.valueOf(value));
				}
			}
		}
		validateVariableKeyLength(errorList, keyToValueMap, CommonConstants.DATATYPE_GEOTAG, 2 ,"Latitude and Longitude both should be present for geotag field");
		validateVariableKeyLength(errorList, keyToValueMap, CommonConstants.DATATYPE_BBOX, 4 ,"Min x, Min y , Max x, Max y should be present for bbox field");
		validateVariableKeyLength(errorList, keyToValueMap, CommonConstants.DATATYPE_CENTER_RADIUS_ENVELOPE, 3 ,"Center x,y Coordinates and radius should be present for bbox field");
		return keyToValueMap;
	}

	private void validateVariableKeyLength(List<IErrorObject> errorList, Map<String, String> keyToValueMap, String key, int length, String errorMsg) {
		if(keyToValueMap.containsKey(key)) {
			int lengthFromFile = keyToValueMap.get(key).split(",").length;
			if(lengthFromFile != length) {
				logger.error(errorMsg);
				errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.NO_DATA, errorMsg, IErrorObject.ROW_PARTIAL_UPLOADED_MESSAGE));
				keyToValueMap.remove(key);
			}
		}
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

	public void uploadFile(IFileUploadDetails fileDetails)
			throws  InvalidInputException, IOException {
		IFileUploadResult<?> fileUploadResult = new FileUploadResult<>();
		logger.debug("Starting Upload for file - " + fileDetails.getFileName());
		long startTS = System.currentTimeMillis();
		String jsonStringNode = fileDetails.getProperties().getProperty(CommonConstants.JSON_CONFIG);
		String uploadType = getUploadType(jsonStringNode);
		UUID superAppId = getSuperAppId(jsonStringNode);
		UUID appId = getAppId(jsonStringNode);
		processQueryForUploadType(uploadType, superAppId, appId);
		fileReaderService.readCSVFile(fileDetails, fileUploadResult);
		printResult(fileUploadResult);
		refreshUserProjectMapping.refreshProjectIdsAssignedToDeafaultUser(superAppId, appId);
		logger.debug("Total Time taken for uploading -" + fileDetails.getFileName() + " : " + (System.currentTimeMillis() - startTS));
	}

	private void processQueryForUploadType(String uploadType, UUID superAppId, UUID appId) throws InvalidInputException, IOException {
		if(uploadType == null) {
			uploadType = CommonConstants.DEFAULT_UPLOAD_TYPE;
		}
		if(!CommonConstants.getAllUploadTypes().contains(uploadType)) {
			logger.error("Invalid File Upload Type found");
			throw new InvalidInputException();
		}
		if(uploadType.equals(CommonConstants.DELETE_INSERT_UPLOAD)) {
			projectDisableService.disableAllProjectForApp(superAppId, appId);
		}		
	}

	private void readJsonNode(JsonNode keyArray, Map<String, String> masterDataNameToKeyNameMap, Map<String, List<KeyFields>> keyNameToKeyObject) {
		for(JsonNode objectNode : keyArray) {
			String keyName = getTextValueFromJsonNode(IFileUploadConstants.MasterDataUploadConstants.KEY, objectNode);
			KeyFields object = new KeyFields();
			object.setKey(keyName);
			object.setDefaultValue(getTextValueFromJsonNode(IFileUploadConstants.MasterDataUploadConstants.DEFAULT, objectNode));
			object.setMasterDataName(getTextValueFromJsonNode(IFileUploadConstants.MasterDataUploadConstants.MASTER_DATA_KEY_NAME, objectNode));
			if(keyNameToKeyObject.containsKey(keyName)) {
				List<KeyFields> keyFieldsList = keyNameToKeyObject.get(keyName);
				keyFieldsList.add(object);
			} else {
				keyNameToKeyObject.put(keyName, new ArrayList<>());
				keyNameToKeyObject.get(keyName).add(object);
			}
			String masterDataName = getTextValueFromJsonNode(IFileUploadConstants.MasterDataUploadConstants.MASTER_DATA_KEY_NAME, objectNode);
			if(masterDataNameToKeyNameMap.containsKey(masterDataName)) {
				keyName = masterDataNameToKeyNameMap.get(masterDataName) + CommonConstants.DELIMITER + keyName;
			}
			masterDataNameToKeyNameMap.put(masterDataName, keyName);	
		}
	}

	private <E> List<String> getAllExternalProjectIds(List<E> dataList, Map<String, String> masterDataNameToKeyNameMap) throws IllegalArgumentException, IllegalAccessException {
		List<String> externalIds = new ArrayList<>();
		for(E masterDataInfo : dataList) {
			externalIds.add(getExternalIdForObject(masterDataInfo, masterDataNameToKeyNameMap));
		}
		return externalIds;
	}

	private <E> String getExternalIdForObject(E masterDataInfo, Map<String, String> masterDataNameToKeyNameMap) throws IllegalArgumentException, IllegalAccessException {
		for (Field f : masterDataInfo.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if(f.getName().equals(masterDataNameToKeyNameMap.get(MasterDataKeyNames.EXTERNAL_PROJECT_ID))) {
				String extId = String.valueOf(f.get(masterDataInfo));
				return extId;
			}
		}
		return null;
	}

	private void insertRecordsIntoDatabase(List<ProjectMasterData> masterDataList, Map<String, Map<Integer, UserProjectMapping>> userProjectMappingMap,
			Map<String, ProjectExternalInternalMapData> externalToInternalIdMap) {

		List<ProjectExternalInternalMapData> externalToInternalIdList = new ArrayList<>();
		List<UserProjectMapping> userProjMappingInsertList = new ArrayList<>();

		masterDataCrudService.insertListOfProjectMasterData(masterDataList);
		for(String userId : userProjectMappingMap.keySet()) {
			for(Integer userType : userProjectMappingMap.get(userId).keySet()) {
				userProjMappingInsertList.add(userProjectMappingMap.get(userId).get(userType));
			}
		}
		userProjectMappingCrudService.insertUserProjectMappingData(userProjMappingInsertList);
		for(String externalId : externalToInternalIdMap.keySet()) {
			externalToInternalIdList.add(externalToInternalIdMap.get(externalId));
		}
		projectExternalToInternalMappingCrudService.insertListOfProjectExternalInternalMapData(externalToInternalIdList);		
	}
}
