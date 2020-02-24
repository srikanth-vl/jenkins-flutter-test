package com.vassarlabs.proj.uniapp.generic.upload.service;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.proj.uniapp.app.config.insert.service.FormJsonInsertionService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationFormData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationFormDataCrudService;
import com.vassarlabs.proj.uniapp.enums.ActiveFlags;

@Component
public class FormUploadService {

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired FormJsonInsertionService formJsonInsertionService;
	@Autowired ApplicationFormDataCrudService appFormDataCrudService;

	public void insertFormJsonForProjects(List<FieldMetaData> insertionList, String formJsonPath, Integer formType, Boolean isMdVersionUpdateRequired) throws JsonProcessingException, IOException, InvalidInputException {

		JsonNode jsonNode = objectMapper.readTree(new File(formJsonPath));
		String genericFormJson = jsonNode.findPath("jsonConfig").toString();
		if(insertionList == null || insertionList.isEmpty()) {
			return ;
		}
		FieldMetaData metaDataFirstElement = insertionList.get(0);
		Map<UUID, List<FieldMetaData>> projectIdToFieldMetaDataList = insertionList.stream().collect(Collectors.groupingBy(FieldMetaData:: getProjectId));
		for(UUID projectId : projectIdToFieldMetaDataList.keySet()) {
			List<FieldMetaData> metaDataList = projectIdToFieldMetaDataList.get(projectId);
			if(metaDataList == null || metaDataList.isEmpty()) {
				continue;
			} else {
				String metadataInstanceId = metaDataList.get(0).getMetadataInstanceId();
				int version = -1;
				ApplicationFormData existingAppFormData = appFormDataCrudService.findLatestVersionAppFormData(metaDataFirstElement.getSuperAppId(), metaDataFirstElement.getApplicationId(), projectId, formType, ActiveFlags.ALL);
				if(existingAppFormData == null) {
					version = 1;
				} else {
					version = existingAppFormData.getFormVersionNumber();
				}
				if(isMdVersionUpdateRequired) {
					version++;
				}
				ApplicationFormData formData = formJsonInsertionService.createAppFormObject(metaDataFirstElement.getSuperAppId(), metaDataFirstElement.getApplicationId(), projectId, formType, ActiveFlags.ACTIVE.getValue(),
						genericFormJson, metadataInstanceId, version, metaDataList);
				removeAdditionalFieldsFromFormJson(formData, metaDataList);
				appFormDataCrudService.insertApplicationFormData(formData);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void removeAdditionalFieldsFromFormJson(ApplicationFormData formData, List<FieldMetaData> fieldMetaDataList) throws IOException {
		JsonNode formNode = objectMapper.readTree(formData.getFormJson());
		Map<String, List<FieldMetaData>> keyToFieldDataMap = fieldMetaDataList.stream().collect(Collectors.groupingBy(FieldMetaData:: getKey));
		Set<String> keysFromMetaData = keyToFieldDataMap.keySet();
		JsonNode jsonNode = formNode.findPath("form");
		for(JsonNode node : jsonNode) {
			JsonNode headersNode = node.findPath("header");
			JsonNode fieldsNode = node.findPath("fields");
			fieldsNode = removeFields(fieldsNode, keysFromMetaData);
			headersNode = removeFields(headersNode, keysFromMetaData);
		}
		Map<String, Object> jsonNodeMap = objectMapper.readValue(formNode.toString(), new TypeReference<Map<String, Object>>(){});
		Map<String, Object> jsonMap = (Map<String, Object>) jsonNodeMap.get("forms");
		jsonMap.remove("form");
		jsonMap.put("form", jsonNode);
		
		formData.setFormJson(objectMapper.writeValueAsString(jsonNodeMap));
	}

	/**
	 * If the field in generic form is not present in field meta data for that object, then remove that field from the form
	 * @param fieldsNode
	 * @param keysFromMetaData
	 * @return
	 */
	private JsonNode removeFields(JsonNode fieldsNode, Set<String> keysFromMetaData) {
		Iterator<JsonNode> itr = fieldsNode.iterator();
		while(itr.hasNext()) {
			JsonNode fieldNode = itr.next();
			String key = fieldNode.findPath("key").asText();
			if(key.contains(CommonConstants.KEY_DELIMITER)) {
				String[] keyList = key.split(CommonConstants.KEY_DELIMITER_REGEX);
				key = keyList[keyList.length-1];
			}
			if(!keysFromMetaData.contains(key)) {
				itr.remove();
			}   
		};
		return fieldsNode;
	}
}
