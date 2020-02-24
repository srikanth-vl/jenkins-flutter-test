package com.vassarlabs.proj.uniapp.app.config.insert.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.app.insert.service.pojo.FormInsert;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationFormData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.AppFormConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationFormDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.enums.ActiveFlags;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.enums.UserStates;
import com.vassarlabs.proj.uniapp.validations.api.pojo.FormValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationObject;

@Component
public class FormJsonInsertionService {

	@Autowired FieldMetaDataCrudService fieldMetaDataCrudService;
	@Autowired ApplicationFormDataCrudService appFormCrudService;
	@Autowired UserMetaDataCrudService userDataCrudService;
	@Autowired ProjectExternalToInternalMappingCrudService projectExternalToInternalMapCrudService;

	ObjectMapper objectMapper = new ObjectMapper();

	public void insertFormJson(FormInsert formInsertData) throws InvalidInputException, IOException {
		
		UUID superAppId = formInsertData.getSuperAppId();
		UUID appId = formInsertData.getAppId();
		UUID projectId = formInsertData.getProjectId();
		int activeFlag = formInsertData.getIsActive();
		String formJson = formInsertData.getFormJson();
		String metaDataInstanceId = formInsertData.getMetadataInstanceId();
		String projectExternalId = formInsertData.getProjectExternalId();
		if(projectExternalId != null && projectId == null) {
			projectId = projectExternalToInternalMapCrudService.findProjectExternalInternalMapDataForProjectId(superAppId, appId, projectExternalId).getProjectId();
		}
				
		
		validateData(superAppId, appId, projectId, formInsertData.getFormType().toUpperCase(), activeFlag, formJson, metaDataInstanceId);
		Integer formType = FormTypes.valueOf(formInsertData.getFormType().toUpperCase()).getValue();
		ApplicationFormData existingAppFormData = appFormCrudService.findLatestVersionAppFormData(superAppId, appId, projectId, formType, ActiveFlags.ALL);
		int version;
		if(existingAppFormData == null) {
			version = 1;
		} else {
			version = existingAppFormData.getFormVersionNumber() + 1;
		}
		if(metaDataInstanceId == null) {
			metaDataInstanceId = existingAppFormData.getMetaDataInstanceId();
		}
		ApplicationFormData formData = createAppFormObject(superAppId, appId, projectId, formType, activeFlag, formJson, metaDataInstanceId, version, null);
		appFormCrudService.insertApplicationFormData(formData);
	}
	
	private void validateData(UUID superAppId, UUID appId, UUID projectId, String formType, int isActive,
			String formJson, String metaDataInstanceId) throws InvalidInputException {
		
		if(superAppId == null || appId == null || projectId == null || formType == null || formJson == null) {
			throw new InvalidInputException("Invalid Input found : Super App Id - " + superAppId + " App Id - " + appId + " project Id - " + projectId + " Form Type - " + formType
					+ " Form Json - " + formJson + " metadata Instance Id- " + metaDataInstanceId);
		}
		if(!FormTypes.getValidFormTypes().contains(formType)) {
			throw new InvalidInputException("Invalid Form Type obtained - " + formType);
		}
		if(metaDataInstanceId != null) {
			String[] metaDataInstances = metaDataInstanceId.split(CommonConstants.DELIMITER);
			if(metaDataInstances.length != 5) {
				throw new InvalidInputException("Invalid meta data instance Id - It should be a superapp##appid##projectid##formtype##md_version");
			}
		}
	}

	public ApplicationFormData createAppFormObject(UUID superAppId, UUID appId, UUID projectId, Integer formType, int activeFlag,
			String formJson, String metaDataInstanceId, int version, List<FieldMetaData> fieldMetaDataList) throws InvalidInputException, IOException {
		if(fieldMetaDataList == null) {
			fieldMetaDataList = fieldMetaDataCrudService.getFieldData(superAppId, appId, metaDataInstanceId);
		}
		Map<String, List<FieldMetaData>> keyToDataMap = fieldMetaDataList.stream().collect(Collectors.groupingBy(FieldMetaData :: getKey));

		if(fieldMetaDataList == null 
				|| fieldMetaDataList.isEmpty()) {
			throw new InvalidInputException("No field Meta data found for super app - " + superAppId + " app id - " + appId + " metadataInstanceId - " + metaDataInstanceId);
		}
		String json = populateFieldData(formJson, keyToDataMap, superAppId);
		
		ApplicationFormData formData = new ApplicationFormData();
		formData.setSuperAppId(superAppId);
		formData.setApplicationId(appId);
		formData.setFormType(formType);
		formData.setFormVersionNumber(version);
		formData.setActiveFlag(activeFlag);
		formData.setMetaDataInstanceId(metaDataInstanceId);
		formData.setProjectId(projectId);
		formData.setFormJson(json);
		formData.setFormInstanceId(generateFormInstanceId(superAppId, appId, projectId, formType, version));
		formData.setInsertTs(System.currentTimeMillis());
		return formData;
	}

	private String generateFormInstanceId(UUID superAppId, UUID appId, UUID projectId, Integer formType, int version) {
		return superAppId + CommonConstants.DELIMITER + appId + CommonConstants.DELIMITER + projectId + CommonConstants.DELIMITER + formType + CommonConstants.DELIMITER + version;
	}

	@SuppressWarnings("unchecked")
	private String populateFieldData(String formJson, Map<String, List<FieldMetaData>> keyToDataMap, UUID superAppId) throws IOException, InvalidInputException {
		JsonNode jsonNode = objectMapper.readTree(formJson);
		Map<String, Object> jsonNodeMap = objectMapper.readValue(jsonNode.toString(), new TypeReference<Map<String, Object>>(){});
		Map<String, Object> jsonMap = (Map<String, Object>) jsonNodeMap.get("forms");
		
		if (jsonMap.containsKey("assigneeapp") && jsonMap.containsKey("assigneeuser")) {
			if (jsonMap.get("assigneeapp")!=null && jsonMap.get("assigneeuser")!=null) {
				UserDBMetaData userData = userDataCrudService.findUserDataByUserIdKey(superAppId, (String) jsonMap.get("assigneeuser"), UserStates.ACTIVE);
				if (!userData.getAppActions().containsKey(UUIDUtils.toUUID((String) jsonMap.get("assigneeapp")))) {
					throw new InvalidInputException("User - " + jsonMap.get("assigneeuser") + 
							"is not assigned to given app - " + jsonMap.get("assigneeapp"));
				}
			}
		}
		Iterator<JsonNode> formsIterator = jsonNode.findPath("form").iterator();
		List<Object> list = new ArrayList<>();
		while(formsIterator.hasNext()) {
			Map<String, Object> formElementsMap = objectMapper.readValue(formsIterator.next().toString(), new TypeReference<Map<String, Object>>(){});
			List<Map<String, Object>> newJsonList = new ArrayList<>();
			List<Map<String, Object>> jsonList = null;
			Set<String> allKeysRequired = null;
			boolean isPreviewForm = false;
			if(!formElementsMap.containsKey("fields")) {
				jsonList = (List<Map<String, Object>>) formElementsMap.get("header");
				isPreviewForm = true;
			} else {
				jsonList = (List<Map<String, Object>>) formElementsMap.get("fields");
			}
			if(jsonList == null) {
				list.add(formElementsMap);
				continue;
			}
			for(Map<String, Object> keysToFormPropMap : jsonList) {
				String appendedkey = String.valueOf(keysToFormPropMap.get("key"));
				String[] keyList = appendedkey.split(CommonConstants.KEY_DELIMITER_REGEX);
				String key = keyList[keyList.length - 1];
				if(isPreviewForm) {
					allKeysRequired = AppFormConstants.getRequiredKeysForHeaders();
				} else {
					allKeysRequired = AppFormConstants.getRequiredKeys();
				}
				allKeysRequired.removeAll(keysToFormPropMap.keySet());
				for(String keyName : allKeysRequired) {
					if(allKeysRequired.contains(keyName)) {
						List<FieldMetaData> metaDataList = keyToDataMap.get(key);
						if(metaDataList == null) {
							continue;
						}
						for(FieldMetaData fieldMetaData : metaDataList) {
							if(fieldMetaData.getKeyType() != KeyTypes.APP_DATA_KEY.getValue()) {
								continue;
							} 
							switch(keyName) {
							case AppFormConstants.LABEL :
								keysToFormPropMap.put(keyName, fieldMetaData.getLabelName());
								break;
							case AppFormConstants.UOM :
								keysToFormPropMap.put(keyName, fieldMetaData.getUom()); 
								break;
							case AppFormConstants.DEFAULT:
								keysToFormPropMap.put(keyName, fieldMetaData.getDefaultValue()); 
								break;
							case AppFormConstants.VALIDATIONS :
								if(!fieldMetaData.getValidations().isEmpty()) {
									ValidationObject validationObject = objectMapper.readValue(fieldMetaData.getValidations(), ValidationObject.class);
									FormValidationObject formValidationObject = new FormValidationObject();
									formValidationObject.setApiCallRequestObjectList(validationObject.getApiCallRequestObjectList());
									formValidationObject.setExprValidationObjectList(validationObject.getExprValidationObjectList());
									formValidationObject.setMandatory(fieldMetaData.isMandatory());
									keysToFormPropMap.put(keyName, formValidationObject); 
								}
								break;
							case AppFormConstants.DATATYPE:
								keysToFormPropMap.put(keyName, fieldMetaData.getDataType()); 
								break;
							}
						}
					}
				}
				newJsonList.add(keysToFormPropMap);
			}
			if(isPreviewForm) {
				formElementsMap.put("header", newJsonList);
			} else {
				formElementsMap.put("fields", newJsonList);
			}
			list.add(formElementsMap);
		}
		jsonMap.put("form", list);
		return objectMapper.writeValueAsString(jsonNodeMap);
	}
}
