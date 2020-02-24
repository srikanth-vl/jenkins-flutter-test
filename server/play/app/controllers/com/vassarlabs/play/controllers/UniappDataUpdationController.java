package controllers.com.vassarlabs.play.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DSPException;
import com.vassarlabs.common.utils.err.DataDeletionException;
import com.vassarlabs.common.utils.err.IErrorObject;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.app.data.deletion.ProjectDeletionService;
import com.vassarlabs.proj.uniapp.app.data.deletion.UserDeletionService;
import com.vassarlabs.proj.uniapp.app.data.deletion.UserProjectMappingDeletionService;
import com.vassarlabs.proj.uniapp.app.data.insertion.RESTMasterDataUpload;
import com.vassarlabs.proj.uniapp.app.data.insertion.RESTUserMetaDataUpload;
import com.vassarlabs.proj.uniapp.app.data.insertion.RESTUserProjectMappingUpload;
import com.vassarlabs.proj.uniapp.upload.pojo.UserMetaDataRequest;
import com.vassarlabs.proj.uniapp.upload.pojo.UserProjectMappingInput;

import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

@org.springframework.stereotype.Controller
public class UniappDataUpdationController 
	extends BasicController  {

	
	@Autowired private ProjectDeletionService projectDeletionService;
	
	@Autowired private UserProjectMappingDeletionService userProjectMappingDeletionService;
	
	@Autowired private RESTMasterDataUpload masterDataUploadService;
	
	@Autowired private RESTUserProjectMappingUpload userProjectMappingInsertService;
	
	@Autowired private RESTUserMetaDataUpload userMetaDataUploadService;
	
	@Autowired private UserDeletionService userDeletionService;
			
	ObjectMapper objectMapper = new ObjectMapper();
	
	public Result uploadUserMetaData() {
		
		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();
		String formDataJson = jsonNode.get("data").toString();
		Map<String, List<IErrorObject>> outputObject = new HashMap<>();
		try {
			UserMetaDataRequest userMetaDataRequest = objectMapper.readValue(formDataJson, UserMetaDataRequest.class);
			outputObject = userMetaDataUploadService.insertUserMetaData(userMetaDataRequest);
		} catch (InvalidInputException e) {
			Logger.error("In uploadUserMetaData() - Invalid input ");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(DSPException e) {
			Logger.error("In uploadUserMetaData() - Problem uploading data ");
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			Logger.error("In uploadUserMetaData() : Error parsing Json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(outputObject));
	}
	
	public Result uploadMasterData() {
		
		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();
		String formDataJson = jsonNode.get("data").toString();
		Map<String, List<IErrorObject>> outputObject = new HashMap<>();
		try {
			AppFormDataSubmittedList dataList = objectMapper.readValue(formDataJson, AppFormDataSubmittedList.class);
			outputObject = masterDataUploadService.uploadMasterData(dataList);
			
		} catch (InvalidInputException e) {
			Logger.error("In uploadMasterData() - Invalid input ");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(DSPException e) {
			Logger.error("In uploadMasterData() - Problem uploading data ");
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException | JsonParseException e) {
			Logger.error("In uploadMasterData() : Error parsing Json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(outputObject));
	}
	
	public Result uploadUserProjectMappingData() {
		
		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();
		String formDataJson = jsonNode.get("data").toString();
		Map<String, List<IErrorObject>> outputObject = new HashMap<>();
		try {
			UserProjectMappingInput userProjectMappingInput = objectMapper.readValue(formDataJson, UserProjectMappingInput.class);
			outputObject = userProjectMappingInsertService.insertUserProjectMapping(userProjectMappingInput);
		} catch (InvalidInputException e) {
			Logger.error("In uploadUserProjectMappingData() - Invalid input ");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(DSPException e) {
			Logger.error("In uploadUserProjectMappingData() - Problem uploading data ");
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException | JsonParseException e) {
			Logger.error("In uploadUserProjectMappingData() : Error parsing Json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(outputObject));
	}
	
	public Result deleteUserMetaData() {
		
		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();
		String formDataJson = jsonNode.get("data").toString();
		Map<String, List<IErrorObject>> outputObject = new HashMap<>();
		try {
			UserMetaDataRequest userMetaDataRequest = objectMapper.readValue(formDataJson, UserMetaDataRequest.class);
			outputObject = userDeletionService.deleteUserMetaData(userMetaDataRequest);
			
		} catch (InvalidInputException e) {
			Logger.error("In deleteUserMetaData() - Invalid input");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(DataDeletionException e) {
			Logger.error("In deleteUserMetaData() - Problem deleting data ");
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException | JsonParseException e) {
			Logger.error("In deleteUserMetaData() : Error parsing Json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(outputObject));
	}
	public Result deleteProjects() {
		
		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();
		String formDataJson = jsonNode.get("data").toString();
		Map<String, List<IErrorObject>> outputObject = new HashMap<>();
		try {
			AppFormDataSubmittedList dataList = objectMapper.readValue(formDataJson, AppFormDataSubmittedList.class);
			outputObject = projectDeletionService.execute(dataList);
			
		} catch (InvalidInputException e) {
			Logger.error("In deleteProjects() - Invalid input");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(DataDeletionException e) {
			Logger.error("In deleteMasterMetaData() - Problem deleting data ");
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException | JsonParseException e) {
			Logger.error("In deleteProjects() : Error parsing Json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(outputObject));
	}
	
	public Result deleteUserProjectMappingData() {
		
		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();
		String formDataJson = jsonNode.get("data").toString();
		Map<String, List<IErrorObject>> outputObject = new HashMap<>();
		try {
			UserProjectMappingInput userProjectMappingInput = objectMapper.readValue(formDataJson, UserProjectMappingInput.class);
			outputObject = userProjectMappingDeletionService.deleteUserProjectMapping(userProjectMappingInput);
		} catch (InvalidInputException e) {
			Logger.error("In deleteUserProjectMappingData() - Invalid input ");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(DataDeletionException e) {
			Logger.error("In deleteUserProjectMappingData() - Problem deleting data ");
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException | JsonParseException e) {
			Logger.error("In deleteUserProjectMappingData() : Error parsing Json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ok(Json.toJson(outputObject));
	}	
}