package com.vassarlabs.proj.uniapp.vadiations.api.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.validations.api.pojo.APICallValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationResult;

public interface IAPICallValidationService {
	/**
	 * Validate data through API calls. Call APIs in asynchronous way and wait for all of them to be completed. Once all API calls are completed, We will access result and perform validation accordingly.
	 * @param apiCallsList
	 * @param validationResult
	 * @param fieldValue
	 * @param keyToDataValue
	 * @throws JsonProcessingException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void validateThroughAPICall(List<APICallValidationObject> apiCallsList, ValidationResult validationResult, FormFieldValues fieldValue, Map<String, String> keyToDataValue, String formatter) 
			throws JsonProcessingException, InterruptedException, ExecutionException ;

}
