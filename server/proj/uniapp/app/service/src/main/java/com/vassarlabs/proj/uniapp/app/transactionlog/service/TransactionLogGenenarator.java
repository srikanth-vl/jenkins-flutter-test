package com.vassarlabs.proj.uniapp.app.transactionlog.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.FormFieldValues;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.TransactionLogRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTrackingData;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.TransactionLogConstants;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTrackingDataCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
@Component
public class TransactionLogGenenarator {

	@Autowired 
	private UserTrackingDataCrudService userTrackingDataCrudService;

	@Autowired 
	private DataRetrievalService dataRetrievalService ;

	@Autowired 
	private FieldMetaDataCrudService fieldDataCrudService;

	ObjectMapper objectMapper = new ObjectMapper();

	public ServiceOutputObject getTransactionLog(TransactionLogRequestObject transactionLogRequestObject) 
			throws TokenNotFoundException, TokenExpiredException, IOException, DataNotFoundException, InvalidInputException {

		Map<String, Object> output = new HashMap<>();
		if(transactionLogRequestObject == null) {
			return null;
		}
		
		UUID superAppId = transactionLogRequestObject.getSuperAppId();
		UUID appId = transactionLogRequestObject.getAppId();
		String userId = transactionLogRequestObject.getUserId();
		UUID token = UUIDUtils.getDefaultUUID();
		String apiType = APITypes.SUBMIT.getValue();
		long timeStamp;
		int size  = transactionLogRequestObject.getLimit();
		int pageNo= transactionLogRequestObject.getPageNo();
		int noOfTransactions = userTrackingDataCrudService.findCountOfRows(superAppId, appId, userId, apiType);
		int pageCount = noOfTransactions/size + (noOfTransactions%size > 0 ? 1 : 0);
		if(pageNo > 1) {
			List<Long>  timestamps = userTrackingDataCrudService.findMinimumTimestamp(superAppId, appId, userId, apiType, size, pageNo);
		    timeStamp =  timestamps.get(timestamps.size() - 1);
		} else  {
			timeStamp = Long.MAX_VALUE;
		}
		List<UserTrackingData> userTrackingData = userTrackingDataCrudService.findTransactionLogforUserByApiType(superAppId, appId, userId, apiType, size, timeStamp);
		List<Map<String, Object>> responseObject =  new ArrayList<Map<String, Object>>();
		if (userTrackingData == null || userTrackingData.isEmpty()) {

			throw new DataNotFoundException();
		}
		
		List<String> metaDataInstanceIds =  new ArrayList<> ();
		List<UUID> projectIdList =  new ArrayList<> ();
		
		for (UserTrackingData transaction : userTrackingData) { 
			String requestdata = transaction.getRequestObj();
			AppFormData formData = objectMapper.readValue(requestdata, new TypeReference<AppFormData>(){});
			String 	metaDataInstanceId = formData.getMetaDataInstanceId();
			UUID projectId = formData.getProjectId();
			if(!metaDataInstanceIds.contains(metaDataInstanceId)) {
				metaDataInstanceIds.add(metaDataInstanceId);
			}
			if(!projectIdList.contains(projectId)) {
				projectIdList.add(projectId);
			}
			
		}
		
		Map<UUID, List<FieldMetaData>> projectToFieldsData = fieldDataCrudService.getProjectIdToFieldsData(superAppId, appId, metaDataInstanceIds);
		Map<UUID, Map<String, String>> projectIdToData = dataRetrievalService.getValueForAProject(superAppId, appId, projectIdList, ProjectStates.ALL);
		
		for (UserTrackingData transaction : userTrackingData) {
			Map<String, Object> transactionDetail = new HashMap<String, Object>();
			String requestdata = transaction.getRequestObj();
			AppFormData formData = objectMapper.readValue(requestdata, new TypeReference<AppFormData>(){});
			UUID projectId = formData.getProjectId();
			Map<String, String> projectData  = projectIdToData.get(projectId);
			
			for ( String key: transactionLogRequestObject.getFields()) {
				if(projectData.get(key) == null && !(TransactionLogConstants.FORM_SUBMISSION_INPUTS.equals(key) || TransactionLogConstants.INSERT_TIMESTAMP.equals(key) ||  TransactionLogConstants.SYNC_TIMESTAMP.equals(key) || TransactionLogConstants.REQUEST_SUCCESS_STATUS.equals(key))) {
					throw new InvalidInputException("Invalid Project Key Found - " + key);
				}
				transactionDetail.put(key, projectData.get(key));
			}
			transactionDetail.put(TransactionLogConstants.REQUEST_SUCCESS_STATUS, transaction.isRequestSuccessful());
			transactionDetail.put(TransactionLogConstants.INSERT_TIMESTAMP, formData.getTimeStamp());
			transactionDetail.put(TransactionLogConstants.SYNC_TIMESTAMP, System.currentTimeMillis());
			List<FormFieldValues> inputfields =  formData.getFormFieldValuesList();
			List<FieldMetaData> projectFieldsData = new ArrayList<>();
			
			if(projectToFieldsData.containsKey(projectId)) {
				projectFieldsData = projectToFieldsData.get(projectId);
			} else {
				projectFieldsData = projectToFieldsData.get(UUIDUtils.getDefaultUUID());
			}
			
			Map<String, FieldMetaData> keyToLabels = new HashMap<>();
			for (FieldMetaData field : projectFieldsData) {
				keyToLabels.put(field.getKey(),field);
			}
			
			Map<String, Map<String,Object>> input_data = new HashMap<> ();
			
			for (FormFieldValues field : inputfields) {
				Map<String, Object> data = new HashMap<>();
//				data.put(TransactionLogConstants.INPUT_FIELD_KEY, field.getKey());
				data.put(TransactionLogConstants.INPUT_FIELD_VALUE, field.getValue() + 
						(keyToLabels.get(field.getKey()).getUom() != null ? keyToLabels.get(field.getKey()).getUom() : ""));
				data.put(TransactionLogConstants.INPUT_FIELD_LABEL, keyToLabels.get(field.getKey()).getLabelName());
				input_data.put(field.getKey(), data);
			}
			
			transactionDetail.put(TransactionLogConstants.FORM_SUBMISSION_INPUTS, input_data);
			
			responseObject.add(transactionDetail);
		}

		output.put(TransactionLogConstants.PAGE_COUNT,pageCount);
		output.put(TransactionLogConstants.TRANSACTIONLOG, responseObject);
		long trackingTS = System.currentTimeMillis();
		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.TRANSACTION_LOG,
				"Transaction Log Generation", objectMapper.writeValueAsString(transactionLogRequestObject), null, true, trackingTS);
		output.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
		ServiceOutputObject outputObject = new ServiceOutputObject(output, trackingObject, true);
		return outputObject;
	}
}
