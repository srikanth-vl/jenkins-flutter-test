package com.vassarlabs.proj.uniapp.app.config.insert.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.app.insert.service.pojo.FieldData;
import com.vassarlabs.proj.uniapp.app.insert.service.pojo.FieldsDataObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;

@Component
public class FieldDataInsertService {
	
	@Autowired FieldMetaDataCrudService fieldDataCrudService;
	
	@Autowired ProjectExternalToInternalMappingCrudService projectExternalToInternalMapCrudService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public Map<String, Object> insertFieldDataConfigService (FieldsDataObject fieldDataObject) throws JsonProcessingException, InvalidInputException {
		
		Map<String, Object> output = new HashMap<>();
		
		UUID superApp = fieldDataObject.getSuperApp();
		UUID appId = fieldDataObject.getAppId();
		UUID projectId = fieldDataObject.getProjectId();
		String formType = fieldDataObject.getFormType().toUpperCase();
		boolean isMdVersionUpdateReqired = fieldDataObject.isVersionUpdate();	
		String projectExternalId = fieldDataObject.getProjectExternalId();
		if(projectExternalId != null && projectId == null) {
			projectId = projectExternalToInternalMapCrudService.findProjectExternalInternalMapDataForProjectId(superApp, appId, projectExternalId).getProjectId();
		}
		validateProjectForm(superApp, appId, projectId, formType);
		
		long insertTs = System.currentTimeMillis();
		int formTypeNo = FormTypes.valueOf(formType).getValue();
		FieldMetaData data = fieldDataCrudService.findLatestMetaVersion(superApp, appId, projectId, formTypeNo);
		int metaVersion;
	
		if (data == null) {
			metaVersion = 1;
		} else {
			if(isMdVersionUpdateReqired)
				metaVersion = data.getMetaDataVersion() + 1;
			else 
				metaVersion = data.getMetaDataVersion();
		}
		
		String metadataInstanceId = superApp.toString() + CommonConstants.DELIMITER + appId.toString() + CommonConstants.DELIMITER + projectId.toString() 
		+ CommonConstants.DELIMITER + Integer.toString(formTypeNo) + CommonConstants.DELIMITER + Integer.toString(metaVersion);
		
		List<FieldMetaData> fieldMetaData = new ArrayList<>();
		listOfFieldData(metadataInstanceId, KeyTypes.DEFAULT_KEY.getValue(), fieldDataObject.getDefaultFields(), fieldMetaData, insertTs);
		listOfFieldData(metadataInstanceId, KeyTypes.MASTER_DATA_KEY.getValue(), fieldDataObject.getMasterFields(), fieldMetaData, insertTs);
		listOfFieldData(metadataInstanceId, KeyTypes.APP_DATA_KEY.getValue(), fieldDataObject.getAppFields(), fieldMetaData, insertTs);
		fieldDataCrudService.insertFieldMetaData(fieldMetaData);
		
		output.put(CommonConstants.META_DATA_INSTANCE_ID, metadataInstanceId);
		return output;
	}
	
	public void listOfFieldData(String metadataInstanceId, int keyType,	List<FieldData> fieldDataObject, 
			List<FieldMetaData> fieldMetaData, long insertTs) throws JsonProcessingException {
		
		for (FieldData fieldData : fieldDataObject) {
			
			FieldMetaData metaData = new FieldMetaData();
			String[] args = metadataInstanceId.split(CommonConstants.DELIMITER);
			String validations = objectMapper.writeValueAsString(fieldData.getValidations()); 
			
			metaData.setSuperAppId(UUIDUtils.toUUID(args[0]));
			metaData.setApplicationId(UUIDUtils.toUUID(args[1]));
			metaData.setProjectId(UUIDUtils.toUUID(args[2]));
			metaData.setFormType(Integer.parseInt(args[3]));
			metaData.setMetaDataVersion(Integer.parseInt(args[4]));
			metaData.setKeyType(keyType);
			metaData.setKey(fieldData.getKey());
			metaData.setLabelName(fieldData.getLabel());
			metaData.setDataType(fieldData.getDataType());
			metaData.setDefaultValue(fieldData.getDefValue());
			metaData.setUom(fieldData.getUom());
			metaData.setTargetField(fieldData.getTargetKey());
			metaData.setDisplayNames(fieldData.getDisplayLabels());
			metaData.setMetadataInstanceId(metadataInstanceId);
			metaData.setValidations(validations);
			metaData.setComputationType(fieldData.getComputationType());
			metaData.setInsertTs(insertTs);
			fieldMetaData.add(metaData);
		}

	}
	
	public void validateProjectForm (UUID superAppId, UUID appId, UUID projectId, String formType) throws InvalidInputException {
		
		if(superAppId == null || appId == null || projectId == null || formType == null ) {
			throw new InvalidInputException("Invalid Input found : Super App Id - " + superAppId + " App Id - " 
											+ appId + " project Id - " + projectId + " Form Type - " + formType);
		}
		
		if(!FormTypes.getValidFormTypes().contains(formType)) {
			throw new InvalidInputException("Invalid Form Type obtained - " + formType);
		}
		
		if (!projectId.equals(UUIDUtils.getDefaultUUID()) && formType.equals(FormTypes.INSERT.name())) {
			throw new InvalidInputException();
		}
		
	}
	
}
