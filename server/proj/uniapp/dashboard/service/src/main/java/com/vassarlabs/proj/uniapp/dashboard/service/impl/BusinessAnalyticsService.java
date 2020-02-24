package com.vassarlabs.proj.uniapp.dashboard.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.BusinessAnalyticsData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.ProjectListConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.BusinessAnalyticsDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.BusinessAnalyticsResponse;
import com.vassarlabs.proj.uniapp.dashboard.service.api.IBusinessAnalyticsService;

@Service
public class BusinessAnalyticsService implements IBusinessAnalyticsService{
	
	@Autowired SuperAppDataCrudService superAppCrudService;
	@Autowired ApplicationMetaDataCrudService appMetaDataCrudService;
	@Autowired BusinessAnalyticsDataCrudService businessDataCrudService;
	@Autowired FieldMetaDataCrudService fieldMetaDataCrudService;

	@Autowired 
	private IVLLogService logFactory;
	private IVLLogger logger;
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Override
	public List<BusinessAnalyticsResponse> generateComputedJson(UUID superAppId, UUID appId, String parentEntityValue, String parentEntityName) {
		List<BusinessAnalyticsResponse> businessResponse = new ArrayList<BusinessAnalyticsResponse>();
		if (parentEntityValue == null || parentEntityValue.isEmpty() || parentEntityValue.equalsIgnoreCase("null")) {
			parentEntityValue = ProjectListConstants.NA_STRING;
		}
		List<FieldMetaData> fieldsList = fieldMetaDataCrudService.findFieldMetaDataByPartitionKey(superAppId, appId);
		Map<String, FieldMetaData> keyToMetaDataMap = new HashMap<String, FieldMetaData>();
		
		for (FieldMetaData fieldData : fieldsList) {
			if (fieldData.getComputationType() != null && !fieldData.getComputationType().isEmpty()) {
				if (!keyToMetaDataMap.containsKey(fieldData.getKey())) {
					keyToMetaDataMap.put(fieldData.getKey(), fieldData);
				} else {
					if (fieldData.getMetaDataVersion() > keyToMetaDataMap.get(fieldData.getKey()).getMetaDataVersion()) {
						keyToMetaDataMap.put(fieldData.getKey(), fieldData);
					}
				}
			}
		}
		BusinessAnalyticsResponse data = new BusinessAnalyticsResponse();
		if(keyToMetaDataMap == null || keyToMetaDataMap.isEmpty()) {
			logger.info(" generateComputedJson() -  " + "Computation fields not found for Business analytics data for SuperApp :: "  + superAppId + " App :: " + appId);
			return new ArrayList<>();
		} else {
			Map<String, String> labels =  new HashMap<>();
			Map<String, String> computationType =  new HashMap<>();
			Map<String, String> uom =  new HashMap<>();
			for (String  key : keyToMetaDataMap.keySet()) {
				String labelName = keyToMetaDataMap.get(key) != null && keyToMetaDataMap.get(key).getLabelName() != null ? keyToMetaDataMap.get(key).getLabelName() : "";
				if(keyToMetaDataMap.get(key) != null && keyToMetaDataMap.get(key).getComputationType() != null && keyToMetaDataMap.get(key).getComputationType().equalsIgnoreCase("count")) {
					labelName = labelName + "*";
				}
				labels.put(key, labelName);
				computationType.put(key, keyToMetaDataMap.get(key).getComputationType());
				uom.put(key, keyToMetaDataMap.get(key).getUom());
			}
			
			data.setComputationType(computationType);
			data.setLabels(labels);
			data.setFields(new ArrayList<>(keyToMetaDataMap.keySet()));
			data.setUom(uom);
			
		}
		List<BusinessAnalyticsData> childsBusinessData = businessDataCrudService.findBusinessAnalyticsDataByPartitionKey(superAppId, appId, parentEntityValue);
		
		List<String>  attributes = appMetaDataCrudService.getEntityHeirarchyForApp(superAppId, appId);
		String entityName = null;
		if(parentEntityName !=  null  && attributes != null && !attributes.isEmpty()  && !parentEntityName.equalsIgnoreCase("null")) {
			int indexOfParentEntityName = attributes.indexOf(parentEntityName);

			if(indexOfParentEntityName>= 0 && indexOfParentEntityName < attributes.size()-1) {
				entityName = attributes.get(indexOfParentEntityName+1);
			} else {
				return businessResponse;
			} 
		} else {
			entityName = attributes== null || attributes.isEmpty() ? CommonConstants.PROJECT_SUBMISSION_ANALYTICS_DEFAULT_HIERARCHY_ELEMENT : attributes.get(0);
		}
		
		data.setEntityName(entityName);
		businessResponse.add(data);
		if (childsBusinessData != null) {
			for (BusinessAnalyticsData businessAnalyticsData : childsBusinessData) {
				BusinessAnalyticsResponse businessOutput = new BusinessAnalyticsResponse();
				businessOutput.setParentEntity(businessAnalyticsData.getParentEntity());
				businessOutput.setEntity(businessAnalyticsData.getChildEntity());
				businessOutput.setEntityName(entityName);
				String computedJson = businessAnalyticsData.getComputedValues();
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					businessOutput.setComputedValues(objectMapper.readValue(computedJson, new TypeReference<Map<String, Object>>() {}));
				} catch (IOException e) {
					businessOutput.setComputedValues(new HashMap<>());
					e.printStackTrace();
				}
				businessOutput.setInsertTs(businessAnalyticsData.getInsertTs());
				businessResponse.add(businessOutput);
			}
		}
		
		return businessResponse;
	}
	
	
		
	
}
