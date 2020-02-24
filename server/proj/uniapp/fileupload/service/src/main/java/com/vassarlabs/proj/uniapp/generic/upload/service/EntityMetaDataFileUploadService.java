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
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.Entity;
import com.vassarlabs.proj.uniapp.api.pojo.EntityElement;
import com.vassarlabs.proj.uniapp.api.pojo.EntityGroup;
import com.vassarlabs.proj.uniapp.app.update.projects.status.service.ProjectDisableService;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.RefreshUserProjectMapping;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.EntityMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.IFileUploadConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.EntityMetadataCrudService;
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
public class EntityMetaDataFileUploadService 
extends UniAppFileUpload {

	@Autowired private ICSVFileReaderService fileReaderService;
	@Autowired private ICSVFileWriterService fileWriterService;

	@Autowired private EntityMetadataCrudService entityMetadataCrudService;

	@Autowired private RefreshUserProjectMapping refreshUserProjectMapping;
	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;


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
		if(fileUploadEvent.getFileUploadDetails().getClassName().contains(CommonConstants.ENTITY_META_DATA_CONFIG_CLASSNAME)) {
			try {
				logger.debug("Starting entity data file upload");

				Map<String, Map<String, Set<String>>> configMap = new HashMap<>();
				
				Map<String, String> keyTypeToKey = new HashMap<>();
				Map<String, String> keyToParent = new HashMap<>();
				Map<String, String> keyToKeyType = new HashMap<>();
				Properties properties = fileUploadEvent.getFileUploadDetails().getProperties();
				String JsonConfiguration = properties.getProperty(CommonConstants.JSON_CONFIG);

				JsonNode node = objectMapper.readTree(JsonConfiguration);
				
				UUID superAppId = getSuperAppId(JsonConfiguration);
				UUID appId = getAppId(JsonConfiguration);
				configMap =  getExistingEntity(superAppId);
				JsonNode keyArray = node.findValue(IFileUploadConstants.MasterDataUploadConstants.KEYS);
				if(keyArray == null) {
					throw new ValidationException(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "No Keys information found in Json!", IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				}
				readJsonNode(keyArray, keyTypeToKey, keyToParent,keyToKeyType);
				for(E object : fileUploadEvent.getDataList()) {

					populateMasterData(object, configMap, keyTypeToKey, keyToParent,keyToKeyType);

				}
				uploadToDB(configMap, superAppId, appId);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
public Map<String, Map<String, Set<String>>> getExistingEntity(UUID superapp) {
	Map<String, Map<String, Set<String>>> configMap =  new HashMap<>();
	try {
		List<EntityMetaData> data = entityMetadataCrudService.getLatestEntityMetadataForApp(superapp, UUIDUtils.getDefaultUUID());
		for (EntityMetaData entityData : data) {
			String type = entityData.getEntityName();
			String parent = entityData.getParentEntity();
			List<EntityElement> elements = entityData.getElements() != null && !entityData.getElements().isEmpty() ? 
					objectMapper.readValue(entityData.getElements(), new TypeReference<List<EntityElement>>() {}) :new ArrayList<>();
//				List<String> values = 	elements.stream().map(entity -> entity.getName()).collect(Collectors.toList());
				for (EntityElement ele  : elements) {
					updateEntityConfigMap(configMap,ele.getName(),parent, type);
				}
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return configMap;
}

	private void populateMasterData(Object masterDataInfo, Map<String, Map<String,Set<String>>> entityConfigMap,  Map<String, String> keyTypeToKey, Map<String, String> keyToParent, Map<String, String> keyToKeyTYpe) throws IllegalArgumentException, IllegalAccessException  
	{
		Map<String, String> keyToValueMap = getValuesFromFile(masterDataInfo, keyToParent);
		for(String key : keyToValueMap.keySet()) {
			String parent =  getParent(keyToValueMap, keyToParent.get(key),keyTypeToKey);
			if(parent == null)
			{continue;}
			updateEntityConfigMap(entityConfigMap, keyToValueMap.get(key), parent,keyToKeyTYpe.get(key));		
		}

	}
	private String getParent(Map<String, String> keyToValueMap, String parentExpression, Map<String, String> keyTypeToKey) {
		String parent = "DEFAULT";
	if(parentExpression== null || parentExpression.equalsIgnoreCase("DEFAULT")) {
	return parent;
	}
		List<String> parentDimensions   =  StringUtils.getStringListFromDelimitter("##", parentExpression);
		for (String dimension : parentDimensions) {
			String key   = keyTypeToKey.get(dimension);
			String value = keyToValueMap.get(key);
			if(value ==  null || value.isEmpty()) {
				return null;
			}
			value = value.trim();
			parent  = parent + "##" + dimension + "$$"+ value;

		}
		return parent;
	}

	public void updateEntityConfigMap(Map<String, Map<String,Set<String>>> entityConfigMap, String value, String parent, String type) {
		Map<String,Set<String>> typeToElementsMap = entityConfigMap.get(parent);
		Set<String> elements = new HashSet<>();
		if(typeToElementsMap == null) {
			typeToElementsMap =  new HashMap<String, Set<String>>();
		}

		elements = typeToElementsMap.get(type);
		if(elements == null) {
			elements = new HashSet<>();
		} 
		elements.add(value);
		typeToElementsMap.put(type, elements);
		entityConfigMap.put(parent, typeToElementsMap);
	}

	private Map<String, String> getValuesFromFile(Object masterDataInfo, Map<String, String> keyToParent) throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> keyToValueMap = new HashMap<>();
		for (Field f : masterDataInfo.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if(keyToParent.containsKey(f.getName())) {
				
				Object value = f.get(masterDataInfo);
				String valueString = value!= null ?  String.valueOf(value).trim() : null;
				if(valueString != null && !valueString.isEmpty() && !valueString.equalsIgnoreCase("null")) {
					keyToValueMap.put(f.getName(), valueString.toUpperCase());
				}
				
			}
		}
		return keyToValueMap;
	}

	public void uploadToDB(Map<String, Map<String,Set<String>>> configMap, UUID superApp, UUID app) {
		List<EntityMetaData> entityDbData = new ArrayList<>();
		for(String parent : configMap.keySet()) {
			Map<String, Set<String>> typetoElementsMap= configMap.get(parent);
			for(String type : typetoElementsMap.keySet()) {
				EntityMetaData data = new  EntityMetaData();
				data.setApplicationId(app);
				data.setSuperAppId(superApp);
				data.setEntityName(type);
				data.setParentEntity(parent);
				data.setUserId(CommonConstants.DEFAULT_USER_ID);
				data.setInsertTs(System.currentTimeMillis());
				data.setProjectId(UUIDUtils.getDefaultUUID());
				List<EntityElement> elements = getElements(typetoElementsMap.get(type));
				try {
					String typeToElementsStr = objectMapper.writeValueAsString(elements);
					data.setElements(typeToElementsStr);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entityDbData.add(data);
			}	
		}
		if(entityDbData != null && !entityDbData.isEmpty()) {
			entityMetadataCrudService.insertApplicationDBData(entityDbData); }
	}
	public List<EntityElement> getElements(Set<String> values) {
		List<EntityElement> elements = new ArrayList<>();
		for (String value : values) {
			EntityElement element=  new EntityElement();
			element.setName(value);
			elements.add(element);
		}
		return elements;
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



	private void readJsonNode(JsonNode keyArray, Map<String, String> keyTypeToKey, Map<String, String> keyToParent, Map<String, String> keyToKeyTYpe) {
		for(JsonNode objectNode : keyArray) {
			String keyName = getTextValueFromJsonNode(IFileUploadConstants.EntityMetaDataUploadConstants.KEY, objectNode);
			String parent = getTextValueFromJsonNode(IFileUploadConstants.EntityMetaDataUploadConstants.PARENT, objectNode);
			String keyType = getTextValueFromJsonNode(IFileUploadConstants.EntityMetaDataUploadConstants.KEY_TYPE, objectNode);
			keyTypeToKey.put(keyType, keyName);
			keyToParent.put(keyName, parent);
			keyToKeyTYpe.put(keyName, keyType);
		}
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
		}		
	}
}
