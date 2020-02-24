package com.vassarlabs.proj.uniapp.validations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.prod.rest.call.object.RequestObject;
import com.vassarlabs.prod.spel.common.call.SPELExpressionValidator;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.vadiations.api.service.IAPICallValidationService;
import com.vassarlabs.proj.uniapp.validations.api.pojo.APICallValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.AsyncExternalApiCallService;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationResult;

@Component
public class APICallValidationService implements IAPICallValidationService{
	
	@Autowired 
	private AsyncExternalApiCallService externalApiCallService;
	
	@Autowired 
	private SPELExpressionValidator spelExpressionValidator;
	
	
	@SuppressWarnings("unchecked")
	public void validateThroughAPICall(List<APICallValidationObject> apiCallsList, ValidationResult validationResult, FormFieldValues fieldValue, Map<String, String> keyToDataValue, String formatter) throws JsonProcessingException, InterruptedException, ExecutionException {
		
		int length = apiCallsList.size();
		int i = 0;
		CompletableFuture<String> responseList[] = new CompletableFuture[length];
		RequestObject requestList[] = new RequestObject[length];
		Properties additionalProperties = new Properties();
		additionalProperties.setProperty(fieldValue.getDataType(), formatter);
		for(APICallValidationObject validationObject : apiCallsList) {
			RequestObject requestObject = new RequestObject();
			String paramsMapExpression = validationObject.getParams();
			Map<String, Object> params = spelExpressionValidator.generateParamsMap(paramsMapExpression, keyToDataValue, fieldValue.getDataType(), additionalProperties);
			requestObject.setParams(params);
			requestObject.setRequestType(validationObject.getRequestType());
			requestObject.setUrl(validationObject.getRoute());
			
			CompletableFuture<String> response  = externalApiCallService.callAPImethod(requestObject);
			responseList[i] = response;
			requestList[i] = requestObject;
			i++;
			
		}
		
		CompletableFuture.allOf(responseList).join();
		
		for (i = 0; i < length; i++) {
			Boolean result;
			if(responseList[i].get() != null ){
				result = true;
			} else result = false;
			if(!result) {
				validationResult.setValid(false);
				Map<String, List<String>> keyToErrorMessageMap = validationResult.getKeyToErrorMessages();
				if(!keyToErrorMessageMap.containsKey(fieldValue.getKey())) {
					keyToErrorMessageMap.put(fieldValue.getKey(), new ArrayList<>());
				}
				keyToErrorMessageMap.get(fieldValue.getKey()).add("Rest API Call Validation through URL -" + requestList[i].getUrl() + " has failed");
			}
		}	
	}
	
	@SuppressWarnings("unchecked")
	public boolean validateThroughAPICall(List<APICallValidationObject> apiCallsList, String dataType, Map<String, String> keyToDataValue, String formatter) throws JsonProcessingException, InterruptedException, ExecutionException  {	
		int length = apiCallsList.size();
		int i = 0;
		CompletableFuture<String> responseList[] = new CompletableFuture[length];
		RequestObject requestList[] = new RequestObject[length];
		Properties additionalProperties = new Properties();
		additionalProperties.setProperty(dataType, formatter);
		for(APICallValidationObject validationObject : apiCallsList) {
			RequestObject requestObject = new RequestObject();
			String paramsMapExpression = validationObject.getParams();
			Map<String, Object> params = spelExpressionValidator.generateParamsMap(paramsMapExpression, keyToDataValue, dataType, additionalProperties);
			requestObject.setParams(params);
			requestObject.setRequestType(validationObject.getRequestType());
			requestObject.setUrl(validationObject.getRoute());
			
			CompletableFuture<String> response  = externalApiCallService.callAPImethod(requestObject);
			responseList[i] = response;
			requestList[i] = requestObject;
			i++;
			
		}
		CompletableFuture.allOf(responseList).join();
		for (i = 0; i < length; i++) {
			Boolean result;
			if(responseList[i].get() != null ){
				result = true;
			} else result = false;
			if(!result) {
				return false;
			}
		}
		return true;
	}
}