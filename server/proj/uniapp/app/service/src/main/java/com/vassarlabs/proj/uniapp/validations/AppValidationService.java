package com.vassarlabs.proj.uniapp.validations;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.prod.spel.common.call.SPELExpressionValidator;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationFormData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.constants.AppFormConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationFormDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.ActiveFlags;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.validations.api.pojo.APICallValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ExpressionValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationObject;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationResult;
/**
 * Validations possible- Field Level Validations, specific Project level validations
 * @author nidhi
 */
@Component
public class AppValidationService {

	@Autowired private DataRetrievalService dataRetrievalService;
	@Autowired private SPELExpressionValidator spelExpressionValidation;
	@Autowired private APICallValidationService apiValidationService;
	@Autowired private ApplicationFormDataCrudService appFormCrudService;

	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	ObjectMapper objectMapper = new ObjectMapper();

	public ValidationResult validateData(UUID superAppId, UUID appId, AppFormData dataFromApp, Map<String, FieldMetaData> keyToFieldDataMap, List<String> mandatoryFields, Map<String, String> dataTypeformatter) throws JsonParseException, JsonMappingException, IOException, 
	InvalidInputException, ValidationException, SpelEvaluationException, DataNotFoundException, InterruptedException, ExecutionException {

		ValidationResult validationResult = null;
		validationResult = validateFormInstanceId(dataFromApp.getFormInstanceId(), dataFromApp.getTimeStamp());
		if(!validationResult.isValid()) {
			return validationResult;
		}

		Map<String, String> keyToDataValue = new HashMap<>();
		Map<String, String> metaDataInstanceMap = metaDataInstanceIdToMap(dataFromApp.getMetaDataInstanceId());
		if (FormTypes.UPDATE.getValue() == Integer.parseInt(metaDataInstanceMap.get(CommonConstants.FORM_TYPE))) {
			keyToDataValue = dataRetrievalService.getValueForAProject(superAppId, appId, dataFromApp.getProjectId(), ProjectStates.ALL);
			if(keyToDataValue == null || keyToDataValue.isEmpty()) {
				throw new DataNotFoundException();
			}
		}
		List<String> keysFromApp = populateKeyToDataValues(dataFromApp.getFormFieldValuesList(), keyToDataValue);
		checkExpirationDateForProject(keyToDataValue, validationResult, dataTypeformatter);
		if(!validationResult.isValid()) {
			return validationResult;
		}

		if(!keysFromApp.containsAll(mandatoryFields)) {
			validationResult.setValid(false);
			mandatoryFields.removeAll(keysFromApp);
			Map<String, List<String>> keyToErrorMessages = validationResult.getKeyToErrorMessages();
			if(!keyToErrorMessages.containsKey(CommonConstants.DEFAULT_KEY)) {
				keyToErrorMessages.put(CommonConstants.DEFAULT_KEY, new ArrayList<>());
			}
			keyToErrorMessages.get(CommonConstants.DEFAULT_KEY).add("Not all mandatory fields found in the submitted data : Missing keys : " + mandatoryFields);
			validationResult.setKeyToErrorMessages(keyToErrorMessages);
			return validationResult;
		}
		keyToDataValue.put(CommonConstants.FORM_SUBMIT_TIMESTAMP, String.valueOf(dataFromApp.getTimeStamp()));
		long form_submit_time = (dataFromApp.getTimeStamp() - DateUtils.getStartOfDay(dataFromApp.getTimeStamp()))/1000;
		keyToDataValue.put(CommonConstants.FORM_SUBMIT_TIME, String.valueOf(form_submit_time));
		validateSPELExpression(validationResult, dataFromApp.getFormFieldValuesList(), keyToFieldDataMap, keyToDataValue, dataTypeformatter, keysFromApp);
		return validationResult;
	}

	public void validateSPELExpression(ValidationResult validationResult, List<FormFieldValues> formFieldsListFromApp, Map<String, FieldMetaData> keyToFieldDataMap,
			Map<String, String> keyToDataValue, Map<String, String> dataTypeFormatter, List<String> keysFromApp) throws JsonParseException, JsonMappingException, IOException, InterruptedException, ExecutionException, 
	ValidationException, InvalidInputException {
		
		Map<String, String> keyToDataType = new HashMap<>();
		keyToFieldDataMap.forEach((key, value) -> keyToDataType.put(key, value.getDataType()));
		
		for(FormFieldValues fieldValue : formFieldsListFromApp) {	

			if(keyToFieldDataMap.containsKey(fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
					.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()))) {

				FieldMetaData fieldMetaData = keyToFieldDataMap.get(fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
						.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()));

				String validations = fieldMetaData.getValidations();
				String dataType = fieldMetaData.getDataType();
				String formatter = dataTypeFormatter.get(dataType);
				Properties additionalProperties = new Properties();
				if(formatter != null) {
					additionalProperties.setProperty(fieldValue.getDataType(), formatter);
				}
				
				if(fieldMetaData.getKeyType() == KeyTypes.DEFAULT_KEY.getValue() && fieldMetaData.getKey().equals(CommonConstants.MANDATORY_VALIDATION_KEY)) {
					String commaSeparatedList = keysFromApp.stream().
							map(Object::toString).
							collect(Collectors.joining(",")).toString();
					keyToDataValue.put(CommonConstants.APP_DATA_LIST, commaSeparatedList);
				}
				
				if(validations != null && !validations.isEmpty()) {
					ValidationObject validationObject = objectMapper.readValue(validations, ValidationObject.class);
					List<ExpressionValidationObject> expressionLists = validationObject.getExprValidationObjectList();
					List<APICallValidationObject> apiCallsList = validationObject.getApiCallRequestObjectList();

					if(apiCallsList != null && !apiCallsList.isEmpty()) {
						apiValidationService.validateThroughAPICall(apiCallsList, validationResult, fieldValue, keyToDataValue, formatter);
					}
					if(expressionLists == null 
							|| expressionLists.isEmpty()) {
						continue;
					}
					Map<String, List<String>> keyToErrorMessages = validationResult.getKeyToErrorMessages();
					for(int index = 0; index < expressionLists.size() ; index++) {
						String script = expressionLists.get(index).getExpression();
						if(script != null 
								&& !script.isEmpty()) {
							Boolean result = spelExpressionValidation.validateSPELExpression(script, keyToDataValue, keyToDataType, additionalProperties);
							if(!result) {
								validationResult.setValid(false);
								String errorMessage = expressionLists.get(index).getErrorMessage();
						
								if(!keyToErrorMessages.containsKey(fieldValue.getKey())) {
									keyToErrorMessages.put(fieldValue.getKey(), new ArrayList<>());
								}
								if(errorMessage != null && !errorMessage.isEmpty()) {	
									keyToErrorMessages.get(fieldValue.getKey()).add(errorMessage);
								} else  {
									keyToErrorMessages.get(fieldValue.getKey()).add("Validation failed");
								}
							}
						}
					}
				}
			} else {
				logger.error("No meta data found for the key obtained from form - " + fieldValue.getKey().substring(Optional.of(fieldValue.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
						.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()));
				throw new ValidationException();
			}
		}
	}

	private List<String> populateKeyToDataValues(List<FormFieldValues> formFieldsListFromApp, Map<String, String> keyToDataValue) {
		List<String> keysFromApp = new ArrayList<>();
		for(FormFieldValues fieldValues : formFieldsListFromApp) {
			keyToDataValue.put(fieldValues.getKey().substring(Optional.of(fieldValues.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
					.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()), fieldValues.getValue()); 
			keysFromApp.add(fieldValues.getKey().substring(Optional.of(fieldValues.getKey().lastIndexOf(AppFormConstants.KEY_SEPARATOR))
					.map(o -> o.intValue() == -1 ? 0 : (o.intValue() + AppFormConstants.KEY_SEPARATOR.length())).get()));
		}
		return keysFromApp;
	}

	public Map<String, String> metaDataInstanceIdToMap(String metaDataInstanceId) {

		Map<String, String> mdInstanceMap = new HashMap<>();
		String args[] = metaDataInstanceId.split(CommonConstants.DELIMITER);
		mdInstanceMap.put(CommonConstants.SUPER_APP_ID, args[0]);
		mdInstanceMap.put(CommonConstants.APP_ID, args[1]);
		mdInstanceMap.put(CommonConstants.PROJECT_ID, args[2]);
		mdInstanceMap.put(CommonConstants.FORM_TYPE, args[3]);
		mdInstanceMap.put(CommonConstants.META_DATA_VERSION, args[4]);
		return mdInstanceMap;
	}

	private ValidationResult validateFormInstanceId(String formInstanceId, long timestamp) {
		String[] formKeys = formInstanceId.split(CommonConstants.DELIMITER);
		Map<String, List<String>> keyToErrorMessages = new HashMap<>();
		List<String> errorMsg = keyToErrorMessages.get(CommonConstants.DEFAULT_KEY);
		if(errorMsg == null) {
			errorMsg = new ArrayList<>();
		}
		if(formKeys.length != 5) {
			errorMsg.add("Invalid form Instance Id found - " + formInstanceId);
			return ValidationResult.fail(keyToErrorMessages);
		}
		ApplicationFormData appFormData = appFormCrudService.findApplicationFormDataByPrimaryKey(UUIDUtils.toUUID(formKeys[0]), UUIDUtils.toUUID(formKeys[1]), UUIDUtils.toUUID(formKeys[2]), 
				Integer.parseInt(formKeys[3]), Integer.parseInt(formKeys[4]), ActiveFlags.ALL);
		if(appFormData == null) {
			errorMsg.add("No Form data found corresponding to metaData Instance Id -" + formInstanceId);
			return ValidationResult.fail(keyToErrorMessages);
		} else if(appFormData.getActiveFlag() == ActiveFlags.INACTIVE.getValue() && appFormData.getInsertTs() < timestamp) {
			errorMsg.add("Inactive Form data found corresponding to metaData Instance Id -" + formInstanceId);
			return ValidationResult.fail(keyToErrorMessages);
		}
		return ValidationResult.ok();
	}

	/**
	 * If projects data entry date has expired, do not submit the data
	 * @param keyToTargetValue
	 * @param validationResult 
	 * @return
	 */
	private void checkExpirationDateForProject(Map<String, String> keyToTargetValue, ValidationResult validationResult, Map<String, String> dataTypeformatter) {
		String datePattern  = dataTypeformatter.get(CommonConstants.DATATYPE_DATE);
		datePattern = datePattern == null || datePattern.isEmpty() ? CommonConstants.DEFAULT_DATE_FORMAT : datePattern;
		if(keyToTargetValue.containsKey(MasterDataKeyNames.LAST_SUBMISSION_DATE)) {
			String expiryDateStr = keyToTargetValue.get(MasterDataKeyNames.LAST_SUBMISSION_DATE);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);
			LocalDate expiryDate = LocalDate.parse(expiryDateStr, formatter);
			if(expiryDate.isBefore(LocalDate.now())) {
				Map<String, List<String>> keyToErrorMessages = validationResult.getKeyToErrorMessages();
				if(!keyToErrorMessages.containsKey(CommonConstants.DEFAULT_KEY)) {
					keyToErrorMessages.put(CommonConstants.DEFAULT_KEY, new ArrayList<>());
				}
				keyToErrorMessages.get(CommonConstants.DEFAULT_KEY).add("Project Data Insertion Date has expired (was expecting to fill up the details till " + expiryDateStr + " but found"
						+ " modifying on " + LocalDate.now());
				validationResult.setKeyToErrorMessages(keyToErrorMessages);
				validationResult.setValid(false);
			}
		} 
	}

	public boolean isUpdateFormData (String metaDataInstanceId) {

		String[] args = metaDataInstanceId.split("##");
		if (args[3].equals("1")) {
			return true;
		}
		return false;
	}
}
