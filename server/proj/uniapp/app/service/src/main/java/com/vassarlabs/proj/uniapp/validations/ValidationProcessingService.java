package com.vassarlabs.proj.uniapp.validations;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.ErrorObject;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.prod.spel.common.call.SPELExpressionValidator;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.validations.api.pojo.APICallValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ExpressionValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationObject;

@Component
public class ValidationProcessingService {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired private SPELExpressionValidator spelExpEvaluator;
	@Autowired APICallValidationService apiValidationService;

	public String parseAndPerformValidations(String key, Map<String, FieldMetaData> keyToFieldMetaDataMap, Map<String, String> keyToValueMap, String formatter, Properties additionalProperties, List<IErrorObject> errorList) throws IOException {
		Map<String, String> keyToDataType = keyToFieldMetaDataMap.values().stream().collect(Collectors.toMap(FieldMetaData::getKey, FieldMetaData::getDataType));

		String value = keyToValueMap.get(key);
		if(keyToFieldMetaDataMap.get(key).getDataType().equalsIgnoreCase(CommonConstants.DATATYPE_JSON_ARRAY)) {
			
			value = value.replaceAll("\"\"", "\"");
			JsonNode arrayNode = objectMapper.readTree(value);
			for(JsonNode node : arrayNode) {
				Iterator<String> iterator = node.fieldNames();
				while(iterator.hasNext()) {
					String eachKey = iterator.next();
					performValidations(eachKey, keyToFieldMetaDataMap, keyToDataType, keyToValueMap, formatter, additionalProperties, errorList);
				}
			}
		} else {
			performValidations(key, keyToFieldMetaDataMap, keyToDataType, keyToValueMap, formatter, additionalProperties, errorList);
		}
		return value;
	}

	public void performValidations(String key, Map<String, FieldMetaData> keyToFieldMetaDataMap,
			Map<String, String> keyToDataType, Map<String, String> keyToValueMap, String formatter,
			Properties additionalProperties, List<IErrorObject> errorList) throws IOException {

		FieldMetaData metaData = keyToFieldMetaDataMap.get(key);
		// No meta data present for the key: TODO: Verify it later
		if(metaData == null) {
			return;
		}
		String validations = keyToFieldMetaDataMap.get(key).getValidations();
		if(validations != null && !validations.isEmpty()) {
			ValidationObject validationObject = objectMapper.readValue(validations, ValidationObject.class);
			List<ExpressionValidationObject> validationsList = validationObject.getExprValidationObjectList();
			List<APICallValidationObject> apiCallsList = validationObject.getApiCallRequestObjectList();
			if(apiCallsList != null && !apiCallsList.isEmpty()) {
				boolean result = false;
				try {
					result = apiValidationService.validateThroughAPICall(apiCallsList, metaData.getDataType(), keyToValueMap, formatter);
					if(!result) {
						errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "API Validation Failed for key-" + key, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, "API Validation Failed due to Exception for -" + key, IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
				}
			}
			for(ExpressionValidationObject validation : validationsList) {
				if(validation.getExpression() != null && !validation.getExpression().isEmpty()) {
					boolean result = spelExpEvaluator.validateSPELExpression(validation.getExpression(), keyToValueMap, keyToDataType, additionalProperties);
					if(!result) {
						errorList.add(new ErrorObject(IErrorObject.INVALID_INPUT, IErrorObject.ERROR, validation.getErrorMessage(), IErrorObject.ROW_NOT_UPLOADED_SUCCESSFULLY_MESSAGE));
					}
				}
			}
		}
	}
}
