package com.vassarlabs.proj.uniapp.app.projectform.insert.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.validations.AppValidationService;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationResult;

@Component
public class ArrayInputFormatter {

	ObjectMapper objectMapper = new ObjectMapper();
	@Autowired 
	private AppValidationService validationService;
	
	public ValidationResult processAndValidateArrayValues(UUID superAppId, UUID appId, AppFormData formData, Map<String, FieldMetaData> keyToFieldDataMap, List<String> mandatoryFields, Map<String, String> dataTypeformatter) 
			throws IOException, SpelEvaluationException, InvalidInputException, ValidationException, DataNotFoundException, InterruptedException, ExecutionException {

		ValidationResult validationResult = ValidationResult.ok();
		List<FormFieldValues> singleValidationKeys = new ArrayList<>();
		for(FormFieldValues formValues : formData.getFormFieldValuesList()) {
			
			List<FormFieldValues> individualValList = new ArrayList<>();
			//individualValList.addAll(formData.getFormFieldValuesList());
			
			String key = formValues.getKey();
			if(keyToFieldDataMap.containsKey(key)
					&& keyToFieldDataMap.get(key).getDataType().equalsIgnoreCase(CommonConstants.DATATYPE_JSON_ARRAY)) {
				
				String jsonArray = formValues.getValue();
				JsonNode arrayNode = objectMapper.readTree(jsonArray);

				for(JsonNode node : arrayNode) {
					Iterator<String> iterator = node.fieldNames();
					while(iterator.hasNext()) {
						String eachKey = iterator.next();
						FormFieldValues jsonArrayKeyData = new FormFieldValues();
						jsonArrayKeyData.setKey(eachKey);
						jsonArrayKeyData.setDataType(keyToFieldDataMap.get(eachKey).getDataType());
						jsonArrayKeyData.setValue(node.get(eachKey).asText());
						individualValList.add(jsonArrayKeyData);
					}
					AppFormData newAppFormData = objectMapper.readValue(objectMapper.writeValueAsString(formData), AppFormData.class);
					newAppFormData.setFormFieldValuesList(individualValList);
					ValidationResult eachValResult = validationService.validateData(superAppId, appId, newAppFormData, keyToFieldDataMap, mandatoryFields, dataTypeformatter);
					consolidateValidationResult(eachValResult, validationResult);
				}	
				
			} else {
				singleValidationKeys.add(formValues);
			}
		}	
		if(!singleValidationKeys.isEmpty()) {
			AppFormData newAppFormData = objectMapper.readValue(objectMapper.writeValueAsString(formData), AppFormData.class);
			newAppFormData.setFormFieldValuesList(singleValidationKeys);
			ValidationResult validationResultSingleKeys = validationService.validateData(superAppId, appId, formData, keyToFieldDataMap, mandatoryFields, dataTypeformatter);
			consolidateValidationResult(validationResultSingleKeys, validationResult);
		}
		return validationResult;
	}

	private void consolidateValidationResult(ValidationResult eachValResult, ValidationResult validationResult) {
		
		if(!eachValResult.isValid()) {
			validationResult.setValid(false);
		}
		Map<String, List<String>> keyToErrorMessages = validationResult.getKeyToErrorMessages();
		if(keyToErrorMessages == null) {
			keyToErrorMessages = new HashMap<>();
		}
		Map<String, List<String>> keyToErrorMessagesforArray = eachValResult.getKeyToErrorMessages();
		for(String key : keyToErrorMessagesforArray.keySet()) {
			if(!keyToErrorMessages.containsKey(key)) {
				keyToErrorMessages.put(key, new ArrayList<>());
			}
			List<String> errorMsgs = keyToErrorMessages.get(key);
			errorMsgs.addAll(keyToErrorMessagesforArray.get(key));
			keyToErrorMessages.put(key, errorMsgs);
		}
	}
}
