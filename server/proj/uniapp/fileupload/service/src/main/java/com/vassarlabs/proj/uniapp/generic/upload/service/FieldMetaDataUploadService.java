package com.vassarlabs.proj.uniapp.generic.upload.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
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
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaDataAttributes;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.validations.api.pojo.APICallValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ExpressionValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationObject;

@Component
public class FieldMetaDataUploadService<E>
	extends UniAppFileUpload {
	
	ObjectMapper objectMapper = new ObjectMapper();
	@Autowired protected IVLLogService logFactory;
	protected IVLLogger logger;

	@PostConstruct
	protected void init() {
		logger = logFactory.getLogger(getClass());
	}
	@Autowired FieldMetaDataCrudService fieldMetaDataCrudService;
	@Autowired private ICSVFileReaderService fileReaderService;
	@Autowired private ICSVFileWriterService fileWriterService;
	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;
	@Autowired private FormUploadService formUploadService;
	
	@EventListener
	public void fileUploadEvent(IFileUploadEvent<E> fileUploadEvent) throws IOException, IllegalArgumentException, IllegalAccessException {
		if(fileUploadEvent.getFileUploadDetails().getClassName().contains(CommonConstants.FIELDMETADATA_CLASSNAME)) {
			IFileUploadDetails fileDetails = fileUploadEvent.getFileUploadDetails();
			List<ProjectExternalInternalMapData> externalToInternalIdList = new ArrayList<>();
			Map<E, List<IErrorObject>> rowToErrorListMap = fileUploadEvent.getFileUploadResult().getDataToErrorListMap();
			List<FieldMetaData> fieldMetaDataInsertList = new ArrayList<>();
			String jsonStringNode = fileDetails.getProperties().getProperty(CommonConstants.JSON_CONFIG);
			JsonNode jsonNode = objectMapper.readTree(jsonStringNode);
			UUID superAppId = getSuperAppId(jsonStringNode);
			UUID appId = getAppId(jsonStringNode);
			String formType = getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.FORM_TYPE, jsonNode);
			int formTypeInt = getFormType(formType);
			if(formTypeInt == CommonConstants.NAInteger) {
				logger.error("No form type found for field meta data : Found -" + formType + " Please specify either insert or update");
				return;
			}
			JsonNode columnConfig = jsonNode.findValue(IFileUploadConstants.FieldMetaDataUploadConstants.COLUMN_CONFIG);
			String projectIdColumnName = getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.PROJECT_ID, columnConfig);
			Map<String, Boolean> externalProjIdsToProcessingStatus = getAllProjectExternalIds(projectIdColumnName, fileUploadEvent.getDataList());
			List<String> externalProjIds = new ArrayList<>(externalProjIdsToProcessingStatus.keySet());
			Map<String, ProjectExternalInternalMapData> projExternalToInternalIdDataMap = generateExternalToInternalIdMap(superAppId, appId, externalProjIds, jsonNode, fieldMetaDataInsertList, formTypeInt);
			for(E object : fileUploadEvent.getDataList()) {
				List<IErrorObject> errors = populateFieldMetaData(superAppId, appId, formTypeInt, jsonNode, object, fieldMetaDataInsertList, projExternalToInternalIdDataMap);
				boolean result = errors.stream().anyMatch(x -> x.getRowUploadStatus().equals(IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				if(!result) {
					rowToErrorListMap.put(object, errors);
				} 
			}
			fieldMetaDataCrudService.insertFieldMetaData(fieldMetaDataInsertList);
			for(String externalId : projExternalToInternalIdDataMap.keySet()) {
				if(!externalId.equals(IFileUploadConstants.FieldMetaDataUploadConstants.ALL_NAME))
					externalToInternalIdList.add(projExternalToInternalIdDataMap.get(externalId));
			}
			projectExternalToInternalMappingCrudService.insertListOfProjectExternalInternalMapData(externalToInternalIdList);		
			
			if(!rowToErrorListMap.isEmpty()) {
				IFileWriterDetails fileWriterDetails = createFileWriterObject(fileUploadEvent);
				fileWriterService.writeCSVFile(fileWriterDetails, rowToErrorListMap);
			}
			consolidateCountOfRecords(rowToErrorListMap.size(), fieldMetaDataInsertList.size(), fileUploadEvent.getDataList().size(), fileUploadEvent.getFileUploadDetails(), fileUploadEvent.getFileUploadResult());
		}
	}
	
	private Map<String, ProjectExternalInternalMapData> generateExternalToInternalIdMap(UUID superAppId, UUID appId, List<String> externalProjIds, JsonNode jsonNode, List<FieldMetaData> fieldMetaDataInsertList, int formType) throws JsonParseException, JsonMappingException, IOException {
		
		Map<String, ProjectExternalInternalMapData> projExternalToInternalIdDataMap = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, externalProjIds);
		ProjectExternalInternalMapData projectExternalInternalMapData = null;
		for(String value : externalProjIds) {
			UUID projectId = null;
			if(projExternalToInternalIdDataMap.containsKey(value)) {
				projectExternalInternalMapData = projExternalToInternalIdDataMap.get(value);
				projectId = projectExternalInternalMapData.getProjectId();
			} else {
				if(value.equalsIgnoreCase(IFileUploadConstants.FieldMetaDataUploadConstants.DEFAULT_PROJECT)) {
					projectId = UUIDUtils.getDefaultUUID();
				} else if(value.equalsIgnoreCase(IFileUploadConstants.FieldMetaDataUploadConstants.ALL_NAME)) {
					projectId = IFileUploadConstants.FieldMetaDataUploadConstants.getAllProjectId();
				} 	else {
					projectId = UUIDUtils.getTrueTimeUUID();
				}
				projectExternalInternalMapData = createProjectExternalInternalDataObject(superAppId, appId, value, projectId);
				projExternalToInternalIdDataMap.put(value, projectExternalInternalMapData);
			}
			populateRequiredFields(superAppId, appId, projectId, jsonNode, fieldMetaDataInsertList, formType);
		}
		return projExternalToInternalIdDataMap;
	}
	
	private Map<String, Boolean> getAllProjectExternalIds(String key,  List<E> dataList) throws IllegalArgumentException, IllegalAccessException {
		Map<String, Boolean> externalIdsToProcesingStatusMap = new HashMap<>();
		for(E mappingObject : dataList) {
			for (Field f : mappingObject.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				if(f.getName().equals(key)) {
					String userExtId = String.valueOf(f.get(mappingObject));
					externalIdsToProcesingStatusMap.put(userExtId, false);
				}
			}
		}
		return externalIdsToProcesingStatusMap;
	}
	
	private List<IErrorObject> populateFieldMetaData(UUID superAppId, UUID appId, int formType, JsonNode jsonNode, E object, List<FieldMetaData> fieldMetaDataInsertList, 
			Map<String, ProjectExternalInternalMapData> projExternalToInternalIdDataMap) throws JsonParseException, JsonMappingException, IOException, IllegalArgumentException, IllegalAccessException {
		
		List<IErrorObject> errorList = new ArrayList<>();
		List<String> validationsValues = new ArrayList<>();
		JsonNode columnConfig = jsonNode.findValue(IFileUploadConstants.FieldMetaDataUploadConstants.COLUMN_CONFIG);
		JsonNode validationsConfig = columnConfig.findValue(IFileUploadConstants.FieldMetaDataUploadConstants.VALIDATIONS);
		ValidationObject validationObject = objectMapper.readValue(validationsConfig.toString(), ValidationObject.class);
		for(ExpressionValidationObject config : validationObject.getExprValidationObjectList()) {
			validationsValues.add(config.getExpression());
			validationsValues.add(config.getErrorMessage());
		}
		
		FieldMetaDataAttributes attributes = new FieldMetaDataAttributes();
		FieldMetaData metaData = new FieldMetaData();
		metaData.setSuperAppId(superAppId);
		metaData.setApplicationId(appId);
		for (Field f : object.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			String value = String.valueOf(f.get(object));
			if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.PROJECT_ID, columnConfig))) {
				metaData.setProjectId(projExternalToInternalIdDataMap.get(value).getProjectId());
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.KEYNAME, columnConfig))) {
				metaData.setKey(value);
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.KEYTYPE, columnConfig))) {
				if(value == null || value.isEmpty()) {
					value = getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.DEFAULT_KEYTYPE, columnConfig);
				}
				int keyType = setKeyType(value);
				if(keyType == CommonConstants.NAInteger) {
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.WRONG_ENTRY, "No key Type found for field meta data : Found -" + value, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				} else {
					metaData.setKeyType(keyType);
				}
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.EXTERNAL_KEY, columnConfig))) {
				attributes.setExternalKey(value);
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.LABELNAME, columnConfig))) {
				metaData.setLabelName(value);
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.DATATYPE, columnConfig))) {
				metaData.setDataType(value);
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.DEFAULT, columnConfig))) {
				metaData.setDefaultValue(value);
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.UOM, columnConfig))) {
				metaData.setUom(value);
			} else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.MANDATORY, columnConfig))) {
				metaData.setMandatory(getBooleanValue(value));
			} 
//				else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.TARGET_FIELD, columnConfig))) {
//				metaData.setTargetField(value);
//			}
			else if(f.getName().equals(getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.DISPLAY_LABELS, columnConfig))) {
				metaData.setDisplayNames(value);
			} else if(validationsValues.contains(f.getName())) {
				for(ExpressionValidationObject exprValidationObject : validationObject.getExprValidationObjectList()) {
					if(exprValidationObject.getExpression().equals(f.getName())) {
						exprValidationObject.setExpression(value);
						if(exprValidationObject.getExpression().equals("null")) {
							exprValidationObject.setExpression("");
						}
					} else if(exprValidationObject.getErrorMessage().equals(f.getName())) {
						exprValidationObject.setErrorMessage(value);
						if(exprValidationObject.getErrorMessage().equals("null")) {
							exprValidationObject.setErrorMessage("");
						}
					}
				}
			}
		}
		removeEmptyValidations(validationObject);
		metaData.setMetaDataVersion(IFileUploadConstants.DEFAULT_INTEGER_VALUE);
		metaData.setMetadataInstanceId(IFileUploadConstants.DEFAULT_STRING_VALUE);
		metaData.setFormType(formType);
		metaData.setAttributes(objectMapper.writeValueAsString(attributes));
		metaData.setValidations(objectMapper.writeValueAsString(validationObject));
		metaData.setInsertTs(System.currentTimeMillis());
		if(errorList.isEmpty())
			fieldMetaDataInsertList.add(metaData);
		return errorList;
	}

	private void removeEmptyValidations(ValidationObject validationObject) {
		List<ExpressionValidationObject> validationExpressions = validationObject.getExprValidationObjectList();
		ListIterator<ExpressionValidationObject> iter = validationExpressions.listIterator();
		while(iter.hasNext()) {
			ExpressionValidationObject expr = iter.next();
			if((expr.getErrorMessage().isEmpty() || expr.getErrorMessage() == null)
					&& expr.getExpression() == null || expr.getExpression().isEmpty()) {
				iter.remove();
			}
		}

		List<APICallValidationObject> apiValidationObject = validationObject.getApiCallRequestObjectList();
		ListIterator<APICallValidationObject> iterator = apiValidationObject.listIterator();
		while(iterator.hasNext()) {
			APICallValidationObject expr = iterator.next();
			if((expr.getErrorMessage().isEmpty() || expr.getErrorMessage() == null)
					&& expr.getParams() == null || expr.getParams().isEmpty()
					&& expr.getRoute() == null || expr.getRoute().isEmpty()
					&& expr.getRequestType() == null || expr.getRequestType().isEmpty()) {
				iterator.remove();
			}
		}
	}

	private void populateRequiredFields(UUID superAppId, UUID appId, UUID projectId, JsonNode jsonNode, List<FieldMetaData> fieldMetaDataInsertList, int formType) throws JsonParseException, JsonMappingException, IOException {
		JsonNode columnConfigList = jsonNode.findValue(IFileUploadConstants.FieldMetaDataUploadConstants.REQUIRED_COLUMNS);
		for(JsonNode column : columnConfigList) {
			FieldMetaData metaData = objectMapper.readValue(column.toString(), FieldMetaData.class);
			metaData.setSuperAppId(superAppId);
			metaData.setApplicationId(appId);
			metaData.setProjectId(projectId);
			metaData.setFormType(formType);
			metaData.setMetadataInstanceId(IFileUploadConstants.DEFAULT_STRING_VALUE);
			metaData.setMetaDataVersion(IFileUploadConstants.DEFAULT_INTEGER_VALUE);
			metaData.setInsertTs(System.currentTimeMillis());
			fieldMetaDataInsertList.add(metaData);
		}
	}
	
	private ProjectExternalInternalMapData createProjectExternalInternalDataObject(UUID superAppId, UUID appId,
			String extId, UUID projectId) {
		ProjectExternalInternalMapData projectExternalInternalMapData = new ProjectExternalInternalMapData();
		projectExternalInternalMapData.setSuperAppId(superAppId);
		projectExternalInternalMapData.setAppId(appId);
		projectExternalInternalMapData.setProjectId(projectId);
		projectExternalInternalMapData.setInsertTs(System.currentTimeMillis());
		projectExternalInternalMapData.setProjectExternalId(extId);
		return projectExternalInternalMapData;
	}

	public void uploadFile(IFileUploadDetails fileDetails) throws InvalidInputException, IOException {
		IFileUploadResult<?> fileUploadResult = new FileUploadResult<>();
		logger.debug("Starting Upload for file - " + fileDetails.getFileName());
		long startTS = System.currentTimeMillis();
		fileReaderService.readCSVFile(fileDetails, fileUploadResult);
		String jsonStringNode = fileDetails.getProperties().getProperty(CommonConstants.JSON_CONFIG);
		UUID superAppId = getSuperAppId(jsonStringNode);
		UUID appId = getAppId(jsonStringNode);
		JsonNode jsonNode = objectMapper.readTree(jsonStringNode);
		Boolean isMdVersionUpdateRequired = jsonNode.findPath(IFileUploadConstants.FieldMetaDataUploadConstants.UPDATE_MD_VERSION).asBoolean();
		String formType = getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.FORM_TYPE, jsonNode);
		int formTypeInt = getFormType(formType);
		if(formTypeInt == CommonConstants.NAInteger) {
			logger.error("No form type found for field meta data : Found -" + formType + " Please specify either insert or update");
			return;
		}
		String formJsonPath = getTextValueFromJsonNode(IFileUploadConstants.FieldMetaDataUploadConstants.FORM_JSON_CONFIG, jsonNode);
		List<FieldMetaData> insertedList = modifyMdVersionAndInstanceId(superAppId, appId, isMdVersionUpdateRequired);
		if(formJsonPath != null 
				&& !formJsonPath.isEmpty()) {
			logger.debug("Updating the form for the given meta data");
			formUploadService.insertFormJsonForProjects(insertedList, formJsonPath, formTypeInt, isMdVersionUpdateRequired);
		}
		printResult(fileUploadResult);
		logger.debug("Total Time taken for uploading -" + fileDetails.getFileName() + " : " + (System.currentTimeMillis() - startTS));
	}

	private List<FieldMetaData> modifyMdVersionAndInstanceId(UUID superAppId, UUID appId, Boolean isMdVersionUpdateRequired) {
		List<FieldMetaData> fieldMetaDataList = fieldMetaDataCrudService.findFieldMetaDataByPartitionKey(superAppId, appId);
		List<FieldMetaData> insertionList = new ArrayList<>();
		Map<Integer, List<FieldMetaData>> mdVersionToListMap = fieldMetaDataList.stream().collect(Collectors.groupingBy(FieldMetaData::getMetaDataVersion));
		if(mdVersionToListMap == null) {
			logger.error("Empty meta data version to field meta data list map :: Returning");
			return insertionList;
		}
		List<FieldMetaData> listForUpdation = mdVersionToListMap.get(IFileUploadConstants.DEFAULT_INTEGER_VALUE);
		if(listForUpdation == null || listForUpdation.isEmpty()) {
			logger.error("Empty field meta data list found for updation :: Returning");
			return insertionList;
		}
		UUID allProjectUUID = IFileUploadConstants.FieldMetaDataUploadConstants.getAllProjectId();
		Map<UUID, List<FieldMetaData>> projectIdToMetaDataList = listForUpdation.stream().collect(Collectors.groupingBy(FieldMetaData::getProjectId));
		
		int latestVersion = Collections.max(mdVersionToListMap.keySet());
		if(latestVersion == IFileUploadConstants.DEFAULT_INTEGER_VALUE) {
			latestVersion = 1;
		}
		List<FieldMetaData> allProjectAttributes = projectIdToMetaDataList.get(allProjectUUID);
		if(projectIdToMetaDataList.containsKey(allProjectUUID)) {
			projectIdToMetaDataList.remove(allProjectUUID);
			fieldMetaDataCrudService.deleteFieldMetaDataList(allProjectAttributes);
		}
		for(UUID projectId : projectIdToMetaDataList.keySet()) {
			List<FieldMetaData> metaDataListForProject = projectIdToMetaDataList.get(projectId);
			fieldMetaDataCrudService.deleteFieldMetaDataList(metaDataListForProject);
			if(metaDataListForProject == null
					|| metaDataListForProject.isEmpty()) {
				logger.error("No field meta data found corresponding to project -" + projectId);
				continue;
			}
			String metadataInstanceId = null;
			int mdVersion = -999;
			for (FieldMetaData metaData : metaDataListForProject) {
				if(metadataInstanceId == null && mdVersion == -999) {
					if(isMdVersionUpdateRequired) {
						mdVersion = latestVersion + 1;
					} else {
						mdVersion = latestVersion;
					}
					metadataInstanceId = superAppId.toString() + CommonConstants.DELIMITER + appId.toString() + CommonConstants.DELIMITER + metaData.getProjectId() 
					+ CommonConstants.DELIMITER + Integer.toString(metaData.getFormType()) + CommonConstants.DELIMITER + Integer.toString(mdVersion);
				}
				metaData.setMetadataInstanceId(metadataInstanceId);
				metaData.setMetaDataVersion(mdVersion);
				insertionList.add(metaData);
			}
			if(allProjectAttributes != null) {
				for(FieldMetaData metaData : allProjectAttributes) {
					FieldMetaData newMetaDataObject = new FieldMetaData(metaData.getSuperAppId(), metaData.getApplicationId(), projectId, metaData.getFormType(), mdVersion, metaData.getKeyType(),
							metaData.getKey(), metaData.getAttributes(), metadataInstanceId, metaData.getDataType(), metaData.getTargetField(), metaData.getDisplayNames(), metaData.isMandatory(), metaData.getValidations(),
							metaData.getLabelName(), metaData.getDefaultValue(), metaData.getUom(), metaData.getComputationType(), metaData.getDimension(), metaData.getInsertTs());
					insertionList.add(newMetaDataObject);
				}
			}
		}
		fieldMetaDataCrudService.insertFieldMetaData(insertionList);
		return insertionList;
	}
	
	private boolean getBooleanValue(String value) {
		if(value.equalsIgnoreCase(IFileUploadConstants.TRUE)
				|| value.equals("1")) {
			return true;
		}
		return false;
	}

	private int setKeyType(String value) {
		if(value.equalsIgnoreCase(IFileUploadConstants.FieldMetaDataUploadConstants.MASTER_DATA_KEY)) {
			return KeyTypes.MASTER_DATA_KEY.getValue();
		} else if(value.equalsIgnoreCase(IFileUploadConstants.FieldMetaDataUploadConstants.APP_DATA_KEY)) {
			return KeyTypes.APP_DATA_KEY.getValue();
		} else if(value.equalsIgnoreCase(IFileUploadConstants.FieldMetaDataUploadConstants.DEFAULT_DATA_KEY)) {
			return KeyTypes.DEFAULT_KEY.getValue();
		} 
		return CommonConstants.NAInteger;
	}
	
	private int getFormType(String formType) {
		if(formType.equalsIgnoreCase(FormTypes.UPDATE.name())) {
			return FormTypes.UPDATE.getValue();
		} else if(formType.equalsIgnoreCase(FormTypes.INSERT.name())) {
			return FormTypes.INSERT.getValue();
		}
		return CommonConstants.NAInteger;
	}
}
