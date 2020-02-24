package controllers.com.vassarlabs.play.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.AppVersionMismatchException;
import com.vassarlabs.common.utils.err.DataDeletionException;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.FormSubmissionSyncPeriodExceedException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormData;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.insert.service.IDataReceiverService;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;

import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

@org.springframework.stereotype.Controller
public class DataReceiverServiceController  
extends BasicController {

	@Autowired ApplicationContext applicationCtxt;
	@Autowired private ApplicationProperties applicationProperties;
	@Qualifier("QueueFailedSubmissionDataReceiverService")
	@Autowired private IDataReceiverService submitFormDataToFailedSubmissionQueueService;

	public Result receiveAndSubmitData() {
		Logger.info("In DataReceiverServiceController :: receiveAndSubmitData() - Receiving data");
		JsonNode jsonNode = request().body().asJson();
		String formDataJson =  jsonNode.findPath("formdata").asText();
		ApiResponse response = new ApiResponse();
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			AppFormDataSubmittedList dataList = objectMapper.readValue(formDataJson, AppFormDataSubmittedList.class);
			List<ServiceOutputObject> outputObjectList;
			List<String> dataReceiverClass = applicationProperties.getListProperty(CommonConstants.FORM_SUBMIT_RECEIVER_CLASS);
			List<IDataReceiverService> dataRecievers = new ArrayList<IDataReceiverService>();

			/* 
			 * Initialize List of beans read from configuration files
			 */
			for (String classname : dataReceiverClass) {
				dataRecievers.add((IDataReceiverService)applicationCtxt.getBean(classname));
			}
			Map<UUID, AppFormData> projectIdtoAppFormData = new HashMap<UUID, AppFormData>();
			projectIdtoAppFormData = dataList.getAppFormDataList().stream().collect(Collectors.toMap(AppFormData::getProjectId, Function.identity()));
			//TODO validate Project independent of Receiver.execute Method
			/*
			 * First bean by specified in configuration file should be to Insert in DB
			 * Invoke IDataReceiverService.execute() method to insert in DB.
			 */
			outputObjectList = dataRecievers.get(0).execute(dataList); 

			List<AppFormData> successfullySubmittedFormData = new ArrayList<AppFormData>();
			List<AppFormData> failedSubmissionFormData = new ArrayList<AppFormData>();
			for (ServiceOutputObject output : outputObjectList) {
				AppFormData formData = objectMapper.readValue(output.getTrackingObject().getRequestObj(), new TypeReference<AppFormData>(){});
				if(output.isSuccessful()) {
					successfullySubmittedFormData.add(projectIdtoAppFormData.get(formData.getProjectId()));
				} else {
					failedSubmissionFormData.add(projectIdtoAppFormData.get(formData.getProjectId()));
				}

			}
			if(!successfullySubmittedFormData.isEmpty()) {
				dataList.setAppFormDataList(successfullySubmittedFormData);
				Logger.info("In DataReceiverServiceController :: receiveAndSubmitData() - Submitted data for: " + successfullySubmittedFormData.size() + " projects");
				// Iterate through all other data receiver services and call their respective execute methods
				for (int i = 1; i < dataRecievers.size() ; i++) {
					IDataReceiverService dtReceiever  = dataRecievers.get(i);
					dtReceiever.execute(dataList);
				}
			}
			// add failed submission to Queue for analytics
			if(failedSubmissionFormData != null && !failedSubmissionFormData.isEmpty()) {
				dataList.setAppFormDataList(failedSubmissionFormData);
				submitFormDataToFailedSubmissionQueueService.execute(dataList);
			}
			response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.PROJECT_SYNC_SUCCESS);
			for(ServiceOutputObject output : outputObjectList) {
				if(!output.isSuccessful()) {
					Map<String, List<String>> keyToErrorMessages = output.getKeyToErrorMessages();
					String errorMessage = null;
					if(keyToErrorMessages != null) {
						errorMessage = objectMapper.writeValueAsString(keyToErrorMessages);
					}
					response.setResult(false, ResponseConstants.VALIDATION_FAILURE, null, errorMessage);
				}
			}
		} catch (InvalidInputException e) {
			Logger.error("In submitFormData() - Invalid input ");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (ValidationException e) {
			Logger.error("In submitFormData() - Validation Exception ");
			response.setResult(false, ResponseConstants.VALIDATION_EXCEPTION_ERROR_CODE, null, ResponseConstants.VALIDATION_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DataNotFoundException e) {
			Logger.error("In submitFormData() - Data Not found ");
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In submitFormData() - Data Submission Error - Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In submitFormData() - Data Submission Error - Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException | JsonParseException e) {
			Logger.error("In submitFormData() - Data Submission Error : Error parsing Json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		}catch (InterruptedException e) {
			Logger.error("In submitFormData - Thread Interrupted Exception ");
			response.setResult(false, ResponseConstants.FAILURE, null, "");
			e.printStackTrace();
		} catch(DataDeletionException e) {
			Logger.error("In submitFormData - Data has been deleted ");
			response.setResult(false, ResponseConstants.DATA_DELETION_ERROR	, null, e.getMessage());
			e.printStackTrace();
		} catch (AppVersionMismatchException e) {
			Logger.error("In submitFormData - Data was submitted from older app");
			response.setResult(false, ResponseConstants.APP_VERSION_MISTMATCH_ERROR_CODE, null, e.getMessage());
			e.printStackTrace();
		} catch (FormSubmissionSyncPeriodExceedException e) {
			Logger.error("In submitFormData() - Data Submission Error - Form Submisson Exceeded Sync Period ");
			response.setResult(false, ResponseConstants.SYNC_PERIOD_EXCEEDED, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (IOException e) {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
			Logger.error("In submitFormData - Got an IO exception in Parsing AppFormDataSubmittedList ");
			e.printStackTrace();
		} 
		catch (ExecutionException e) {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
			Logger.error("In submitFormData - Got an ExecutionException ");
			e.printStackTrace();
		} 
		return ok(Json.toJson(response));
	}

}
