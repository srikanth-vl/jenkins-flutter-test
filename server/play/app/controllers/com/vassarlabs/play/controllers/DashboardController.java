package controllers.com.vassarlabs.play.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.ImageSubmittedAndReceivedDashboardData;
import com.vassarlabs.proj.uniapp.api.pojo.MediaSubmissionLogRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.TransactionLogRequestObject;
import com.vassarlabs.proj.uniapp.app.transactionlog.service.TransactionLogGenenarator;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardTokenData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.DashboardTokenDataCrudService;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.BusinessAnalyticsResponse;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectFormSubmissionDataRequestObject;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionCountForEntity;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionData;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.SuperAppAnalyticsData;
import com.vassarlabs.proj.uniapp.dashboard.service.impl.AppInformationAnalytics;
import com.vassarlabs.proj.uniapp.dashboard.service.impl.BusinessAnalyticsService;
import com.vassarlabs.proj.uniapp.dashboard.service.impl.ProjectSubmissionAnalyticService;
import com.vassarlabs.proj.uniapp.dashboard.service.impl.ProjectSubmissionDataFetchService;
import com.vassarlabs.proj.uniapp.data.retrieve.FormSubmittedDataRetreivelService;
import com.vassarlabs.proj.uniapp.imagelog.service.ImageSubmittedAndReceivedDataRetrievalService;

import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.com.vassarlabs.play.services.ApiResponseFactory;



@org.springframework.stereotype.Controller
public class DashboardController 
extends BasicController{

	@Autowired TransactionLogGenenarator transactionLogGenenaratorService;
	@Autowired ImageSubmittedAndReceivedDataRetrievalService imageSubmittedAndReceivedDataRetrievalService;
	@Autowired FormSubmittedDataRetreivelService  formSubmittedDataRetreivelService;
	@Autowired AppInformationAnalytics appInformationAnalytics;
	@Autowired DashboardTokenDataCrudService dashboardTokenCrudService;
	@Autowired ProjectSubmissionAnalyticService projectSubmissionAnalyticService;
	@Autowired ProjectSubmissionDataFetchService projectSubmissionDataFetchService;
	@Autowired BusinessAnalyticsService businessAnalytics;
	
	ObjectMapper objectMapper = new ObjectMapper();

	public Result getTransactionLog(){

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		TransactionLogRequestObject transactionLogRequestObject = null;
		try {
			transactionLogRequestObject = objectMapper.treeToValue(jsonNode, TransactionLogRequestObject.class);
		} catch (JsonProcessingException e1) {
			Logger.error("In Transaction Log Generator - Json processing exception");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e1.printStackTrace();
			return ok(Json.toJson(response));
		}

		try {
			ServiceOutputObject serviceObject;
			serviceObject = transactionLogGenenaratorService.getTransactionLog(transactionLogRequestObject);
			response = createResponse(serviceObject, response);
		} catch (JsonProcessingException e) {
			Logger.error("In Transaction Log Generator- Unable to parse json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PARSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DataNotFoundException e) {
			Logger.error("In Transaction Log Generator- No Logs found for given app and userid");
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In Transaction Log Generator- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In Transaction Log Generator- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error(" Transaction Log Generator- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (InvalidInputException e) {
			Logger.error(" Transaction Log Generator- Invalid Input Exception");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	public Result getImageSubmittedAndReceivedData() throws IOException{

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		MediaSubmissionLogRequestObject appFormRequestObject = null;
		try {
			appFormRequestObject = objectMapper.treeToValue(jsonNode, MediaSubmissionLogRequestObject.class);
		} catch (JsonProcessingException e) {
			Logger.error("In getImageSubmittedAndReceivedData - Json processing exception");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
			return ok(Json.toJson(response));
		}
		List<ImageSubmittedAndReceivedDashboardData> imageSubmittedAndReceivedDashboardData = imageSubmittedAndReceivedDataRetrievalService.getImageSubmittedAndReceivedData(appFormRequestObject);
		response = createResponse (imageSubmittedAndReceivedDashboardData, response);
		return ok(Json.toJson(response));
	}
	
	public Result getFormSubmittedDataList() {
		
		JsonNode jsonNode = request().body().asJson();
		String superapp = jsonNode.get("superapp").asText();
		String appid = jsonNode.get("appid").asText();
		int startDate  = jsonNode.get("startDate").asInt();
		int endDate = jsonNode.get("endDate").asInt();
		
		Map<String, Map<UUID, Map<Long,Map<String,String>>>> keyValueMapOfProject = new HashMap<>();
		Map<Long,Map<String,String>> keyValue = new HashMap<>();
		String filePath = "/home/aitha/work/data_submit_file.csv";
		
		if(superapp == null || appid == null) {
			return ok(Json.toJson(keyValue));
		}
		UUID superappid = UUIDUtils.toUUID(superapp);
		UUID app = UUIDUtils.toUUID(appid);
		
		keyValueMapOfProject = formSubmittedDataRetreivelService.getFormSubmitDataForSuperApp(superappid, app, startDate, endDate);
		
		try {
			String eol = System.getProperty("line.separator");
			
			File file = new File(filePath); 
			Files.deleteIfExists(file.toPath());
			
			try (Writer writer = new FileWriter(filePath)) {
				String headers = "Proj_Ext_Id" 
								+ CommonConstants.KEY_DELIMITER 
								+ "Project_Id" 
								+ CommonConstants.KEY_DELIMITER 
								+ "Timestamp" 
								+ CommonConstants.KEY_DELIMITER 
								+ "Form Submit Data";
				writer.append(headers).append(eol);
				for (Map.Entry<String, Map<UUID, Map<Long,Map<String,String>>>> entry1 : keyValueMapOfProject.entrySet()) {
					for(Map.Entry<UUID, Map<Long,Map<String,String>>> entry2 : entry1.getValue().entrySet()) {
						for(Map.Entry<Long,Map<String,String>> entry3 : entry2.getValue().entrySet()) {
							Map<String, String> keyValues = entry3.getValue();
							String keyValueJson = objectMapper.writeValueAsString(keyValues);
							writer.append(entry1.getKey())
							.append(CommonConstants.KEY_DELIMITER)
							.append(entry2.getKey().toString())
							.append(CommonConstants.KEY_DELIMITER)
							.append(entry3.getKey().toString())
							.append(CommonConstants.KEY_DELIMITER)
							.append(keyValueJson)
							.append(eol);
						}
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
				
		return ok(new java.io.File(filePath));
	}

	public Result getFormSubmittedData() throws IOException {

		JsonNode jsonNode = request().body().asJson();
		String superapp = jsonNode.get("superapp").asText();
		String appid = jsonNode.get("appid").asText();
		String projectid = jsonNode.get("projectid").asText();
		String token = jsonNode.get("token").asText();
		int startDate  = jsonNode.get("startDate").asInt();
		int endDate = jsonNode.get("endDate").asInt();
		Map<Long,Map<String,String>> keyValue = new HashMap<>();
		if(superapp == null || appid == null || projectid == null || token == null) {
			return ok(Json.toJson(keyValue));
		}
		UUID superappid = UUIDUtils.toUUID(superapp);
		UUID app = UUIDUtils.toUUID(appid);
		UUID project = UUIDUtils.toUUID(projectid);
		UUID tokenid = UUIDUtils.toUUID(token);

		keyValue = formSubmittedDataRetreivelService.getUserFormSubmitSingleData(superappid, app, project, tokenid, startDate, endDate);

		return ok(Json.toJson(keyValue));

	}

	public Result getRootHierarchyJson() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId = jsonNode.get("user").asText();

		List<SuperAppAnalyticsData> superAppsData = new ArrayList<SuperAppAnalyticsData>();

		if(userId == null) {
			return ok(Json.toJson(superAppsData));
		}

		try {
			superAppsData = appInformationAnalytics.getSuperAppIdToAppData(userId);
		} catch (IOException e) {
			Logger.error("In getRootHierarchyJson - IO exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
			return ok(Json.toJson(response));
		}
		response = createResponse(superAppsData, response);
		return ok(Json.toJson(response));
	}

	public Result authenticateUser() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId = jsonNode.get("username").asText();
		String password = jsonNode.get("password").asText();

		if (userId != null && password != null) {
			DashboardTokenData tokenData = appInformationAnalytics.authenticate(userId, password);
			if (tokenData != null) {
				response = ApiResponseFactory.createResponse(tokenData);
			}
		}

		return ok(Json.toJson(response));
	}

	public Result tokenExpiry() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId = jsonNode.get("user").asText();
		String token = jsonNode.get("token").asText();

		if (userId != null && token != null) {
			dashboardTokenCrudService.updateTokenExpiry(CommonConstants.TOKEN_EXPIRED, userId, UUIDUtils.toUUID(token));
			response.setResult(true, ResponseConstants.SUCCESS_CODE, ResponseConstants.TOKEN_EXPIRED, ResponseConstants.SUCCESSFUL_MESSAGE);
		}
		return ok(Json.toJson(response));
	}
	public Result getProjectSubmissionAnaltyicsData() {

		ApiResponse response = new ApiResponse();
		List<ProjectSubmissionCountForEntity>  data  =  new ArrayList<>();
		JsonNode jsonNode = request().body().asJson();
		String superapp = jsonNode.get("super_app").asText();
		String app = jsonNode.get("app_id").asText();
		String parentEntityName = jsonNode.get("parent_entity_name").asText();
		String parentEntityValue= jsonNode.get("parent_entity_value").asText();
		if(superapp == null || app == null ) {
			return ok(Json.toJson(data));
		}
		UUID superappid = UUIDUtils.toUUID(superapp);
		UUID appId = UUIDUtils.toUUID(app);
		if(parentEntityValue== null || parentEntityValue.isEmpty() || parentEntityValue.equals("null")) {
			parentEntityValue = null;
		}
		if(parentEntityName== null || parentEntityName.isEmpty() || parentEntityName.equals("null")) {
			parentEntityName = null;
		}
		data = projectSubmissionAnalyticService.getProjectSubmssionAnyticsReport(superappid, appId, parentEntityName,parentEntityValue);
		response = createResponse(data, response);
		return ok(Json.toJson(response));
	}
	public Result getFormSubmittedDataForProjectIds() throws IOException {

		JsonNode jsonNode = request().body().asJson();
		ProjectFormSubmissionDataRequestObject projectFormSubmissionDataRequestObject = null;
		projectFormSubmissionDataRequestObject = objectMapper.treeToValue(jsonNode, ProjectFormSubmissionDataRequestObject.class);
		List<ProjectSubmissionData> data = projectSubmissionDataFetchService.getFormSubmittedData(projectFormSubmissionDataRequestObject.getSuperAppId(), projectFormSubmissionDataRequestObject.getAppId(),projectFormSubmissionDataRequestObject.getProjectIds());

		return ok(Json.toJson(data));

	}
	
	public Result getComputedJson() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String superapp = jsonNode.get("super_app").asText();
		String app = jsonNode.get("app_id").asText();
		String parentEntityName = jsonNode.get("parent_entity_name").asText();
		String parentEntityValue= jsonNode.get("parent_entity_value").asText();

		List<BusinessAnalyticsResponse> businessData = new ArrayList<BusinessAnalyticsResponse>();
		if (superapp!= null && app != null) {
			businessData = businessAnalytics.generateComputedJson(UUIDUtils.toUUID(superapp), UUIDUtils.toUUID(app), parentEntityValue, parentEntityName);
		}
		response = createResponse(businessData, response);
		return ok(Json.toJson(response));
	}
}
