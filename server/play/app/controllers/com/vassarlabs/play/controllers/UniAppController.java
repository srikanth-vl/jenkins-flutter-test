package controllers.com.vassarlabs.play.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.vassarlabs.common.utils.err.AppMetaDataNotFoundException;
import com.vassarlabs.common.utils.err.AuthenticationException;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.InvalidInputException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.ApiRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.AppJsonData;
import com.vassarlabs.proj.uniapp.api.pojo.AppRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.CreateMapBBoxRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.DataSubmitField;
import com.vassarlabs.proj.uniapp.api.pojo.EntityMetaConfigInsertObject;
import com.vassarlabs.proj.uniapp.api.pojo.EntityMetadataConfigRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.KeyTransactionLog;
import com.vassarlabs.proj.uniapp.api.pojo.LoginRequestDetails;
import com.vassarlabs.proj.uniapp.api.pojo.ProjectListRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.TransactionLogRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserDataRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserProjectMapData;
import com.vassarlabs.proj.uniapp.app.api.IAppDataInsertService;
import com.vassarlabs.proj.uniapp.app.api.IAppMetaConfigJsonService;
import com.vassarlabs.proj.uniapp.app.api.IEntityMetadataConfigurationService;
import com.vassarlabs.proj.uniapp.app.api.IFormDataJsonService;
import com.vassarlabs.proj.uniapp.app.api.ILoginValidationService;
import com.vassarlabs.proj.uniapp.app.api.ILogoutService;
import com.vassarlabs.proj.uniapp.app.api.IProjectListDataService;
import com.vassarlabs.proj.uniapp.app.api.IRootConfigurationService;
import com.vassarlabs.proj.uniapp.app.api.ISuperAppInsertService;
import com.vassarlabs.proj.uniapp.app.api.ITransactionLogForKey;
import com.vassarlabs.proj.uniapp.app.computation.businessanalytics.BusinessAnalyticsDataUpdateService;
import com.vassarlabs.proj.uniapp.app.config.insert.service.FieldDataInsertService;
import com.vassarlabs.proj.uniapp.app.config.insert.service.FormJsonInsertionService;
import com.vassarlabs.proj.uniapp.app.data.insertion.EntityMetaConfigurationInsertService;
import com.vassarlabs.proj.uniapp.app.data.insertion.LocalizationConfigInsert;
import com.vassarlabs.proj.uniapp.app.insert.service.pojo.FieldsDataObject;
import com.vassarlabs.proj.uniapp.app.insert.service.pojo.FormInsert;
import com.vassarlabs.proj.uniapp.app.kafkaqueue.service.CreateKafkaConsumers;
import com.vassarlabs.proj.uniapp.app.localizationconfig.service.LocalizationConfigurationService;
import com.vassarlabs.proj.uniapp.app.mapconfiguration.service.MapConfigurationService;
import com.vassarlabs.proj.uniapp.app.projectform.insert.service.ProjectFormInsertService;
import com.vassarlabs.proj.uniapp.app.projects.latestdata.ProjectListDataRetreivalService;
import com.vassarlabs.proj.uniapp.app.userprojectmap.service.UserProjectMapService;
import com.vassarlabs.proj.uniapp.app.users.service.UserInfoService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectExternalInternalMapData;
import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.AppMetaConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.crud.service.ProjectExternalToInternalMappingCrudService;
import com.vassarlabs.proj.uniapp.entitymetadataconfig.service.EntityMetadataConfigurationServiceUserSpecific;
import com.vassarlabs.proj.uniapp.usageanalytics.service.UsageAnalyticsData;
import com.vassarlabs.proj.uniapp.utility.BoundingBoxFormation;

import akka.dispatch.sysmsg.Create;
import controllers.com.vassarlabs.play.constants.ResponseConstants;
import models.com.vassarlabs.play.models.ApiResponse;
import models.com.vassarlabs.play.models.UniAppResult;
import play.Logger;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import services.com.vassarlabs.play.services.ApiResponseFactory;

@org.springframework.stereotype.Controller
public class UniAppController 
extends BasicController  
implements InitializingBean {

	@Autowired private IAppMetaConfigJsonService metaConfig;

	@Autowired private ILoginValidationService loginValidationService;

	@Autowired private IRootConfigurationService rootConfigGenService;
	
	@Autowired private LocalizationConfigurationService locazationConfigService;
	
	@Autowired private MapConfigurationService mapConfigurationService;

	@Autowired private ILogoutService logoutService;

	@Autowired private IFormDataJsonService appFormDataService;

	@Autowired
	@Qualifier("ProjectListWithTimeService") private IProjectListDataService projListWithTSService;

	@Autowired
	@Qualifier("ProjectListDataService1") private IProjectListDataService projListDataService;

	@Autowired private ProjectFormInsertService formDataInsertService;

	@Autowired private FormJsonInsertionService formJsonInsertionService;

	@Autowired private ISuperAppInsertService superAppInsertService;

	@Autowired private IAppDataInsertService appDataInsertService;

	@Autowired private FieldDataInsertService fieldDataInsertService;

	@Autowired private ProjectListDataRetreivalService projListDataRetreivalService;

	@Autowired private UserInfoService userInfoService;

	@Autowired private ProjectExternalToInternalMappingCrudService projectExternalToInternalMappingCrudService;

	@Autowired private UserProjectMapService userProjectMapService;

	@Autowired private UsageAnalyticsData usageAnalyticsData;

	@Autowired private CreateKafkaConsumers createKafkaConsumers ;

	@Autowired private ApplicationProperties properties;
	
	@Autowired private ITransactionLogForKey transactionLogForKey;
	@Autowired private BusinessAnalyticsDataUpdateService businessAnalyticsDataUpdateService;
	@Autowired private BoundingBoxFormation bboxGenrationUtil;
	@Autowired private LocalizationConfigInsert localizationConfigInsert;
	@Autowired private EntityMetadataConfigurationServiceUserSpecific entityMetadataConfigurationService;
	
	@Autowired private EntityMetaConfigurationInsertService entityMetaConfigInsertService;
	ObjectMapper objectMapper = new ObjectMapper();

	public Result getAppMetaConfigJSON(){

		JsonNode jsonNode = request().body().asJson();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		String versionStr =  jsonNode.findPath("versionId").asText();
		ApiResponse response = new ApiResponse();

		if ( !(validateInput(UUID.class, superAppStr) 
				&& validateInput(Integer.class, versionStr))) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		int version = Integer.parseInt(versionStr);

		AppRequestObject appRequestObject = new AppRequestObject(superAppId, version);
		try {
			Map<String, Object> jsonData;
			jsonData = metaConfig.getLatestAppMetaConfigJson(appRequestObject);
			response = ApiResponseFactory.createResponse(jsonData);
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException
				| CassandraInvalidQueryException | CassandraTypeMismatchException | CassandraReadTimeoutException 
				| CassandraQuerySyntaxException | CassandraInternalException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DataNotFoundException e) {
			Logger.error("In appMetaConfigJson()- No data for given superapp");
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Logger.error("In appMetaConfigJson()- Error Mapping json");
			response.setResult(false, ResponseConstants.JSON_MAPPING_ERROR_CODE, null, ResponseConstants.JSON_MAPPING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(JsonParseException e) {
			Logger.error("In appMetaConfigJson()- Error parsing json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PARSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In appMetaConfigJson()- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} 
		return ok(Json.toJson(response));
	}

	public Result authenticateUser() {

		JsonNode jsonNode = request().body().asJson();
		String mobileNumber =  jsonNode.findPath("mobile").asText();
		String password =  jsonNode.findPath("password").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		ApiResponse response = new ApiResponse();

		if ( !validateInput(UUID.class, superAppStr) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		LoginRequestDetails loginRequestObject = new LoginRequestDetails(mobileNumber, password);
		loginRequestObject.setSuperAppId(superAppId);

		try {
			ServiceOutputObject serviceObject = loginValidationService.validateUser(loginRequestObject);
			response = createResponse(serviceObject, response);
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			Logger.error("In validateUser()- Error processing json data");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In validateUser()- Error processing IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (UserNotFoundException e) {
			Logger.error("In validateUser()- User Not Found");
			response.setResult(false, ResponseConstants.USER_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (AuthenticationException e) {
			Logger.error("In validateUser()- UserId and Password do not match");
			response.setResult(false, ResponseConstants.AUTHENTICATION_ERROR_CODE, null, ResponseConstants.AUTHENTICATION_ERROR_MESSAGE);
			e.printStackTrace();
		} 
		return ok(Json.toJson(response));
	}

	public Result getRootConfigJSON() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId =  jsonNode.findPath("userid").asText();
		String tokenStr =  jsonNode.findPath("token").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		String versionStr =  jsonNode.findPath("version").asText();
		int version = versionStr == null || versionStr.isEmpty() ? 0 : Integer.parseInt(versionStr);
		UUID token = null;
		if (tokenStr != null) {
			token = UUID.fromString(tokenStr);
		} else {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		if ( !validateInput(UUID.class, superAppStr) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		ApiRequestObject apiRequestObject = new ApiRequestObject(userId, token);
		apiRequestObject.setSuperAppId(superAppId);
		apiRequestObject.setVersion(version);

		try {
			ServiceOutputObject serviceObject;
			serviceObject = rootConfigGenService.getRootConfigData(apiRequestObject);
			response = createResponse(serviceObject, response);
		} 
		catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (UserNotFoundException e) {
			Logger.error("In Root Configuration- User Not Found");
			response.setResult(false, ResponseConstants.USER_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In Root Configuration- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			//e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In Root Configuration- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			Logger.error("In Root Configuration- Error processing json data");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In Root Configuration- Error reading properties file");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (AppMetaDataNotFoundException e) {
			Logger.error("In Root Configuration- Error fetching app meta data");
			response.setResult(false, ResponseConstants.APP_META_DATA_NOT_FOUND_ERROR_CODE, null, ResponseConstants.APP_META_DATA_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	public Result logout() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId =  jsonNode.findPath("userid").asText();
		String tokenStr =  jsonNode.findPath("token").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();

		UUID tokenId = null;
		if (tokenStr != null) {
			tokenId = UUID.fromString(tokenStr);
		} else {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		if ( !validateInput(UUID.class, superAppStr) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		ApiRequestObject requestObject = new ApiRequestObject(userId, tokenId);
		requestObject.setSuperAppId(superAppId);

		try {
			ServiceOutputObject outputObject;
			outputObject = logoutService.logout(requestObject);
			response = createResponse(outputObject, response);
			if(response.getResult().getSuccess()
					&& outputObject.isSuccessful()) {
				response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.LOGOUT_SUCCESS);
			} else {
				response.setResult(false, ResponseConstants.LOGOUT_UNSUCCESSFUL_ERROR_CODE, null, ResponseConstants.LOGOUT_UNSUCCESSFUL);
			}
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException | CassandraInvalidQueryException
				| CassandraTypeMismatchException | CassandraReadTimeoutException | CassandraQuerySyntaxException 
				| CassandraInternalException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			//e.printStackTrace();
		} catch (TokenExpiredException e) {
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	public Result getProjectTypeJSON() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId =  jsonNode.findPath("userid").asText();
		String tokenStr =  jsonNode.findPath("token").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		String appIdStr = jsonNode.findPath("appid").asText();
		String versionMapStr = jsonNode.findPath("versionmap").toString();

		UUID token = null;
		if (tokenStr != null) {
			token = UUID.fromString(tokenStr);
		} else {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		if ( !(validateInput(UUID.class, superAppStr) 
				&& validateInput(UUID.class, appIdStr))) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		UUID appId = UUIDUtils.toUUID(appIdStr);
		
		try {
			Map<UUID, Map<String, Integer>> projectIdToFormTypeToVer = null;

			if(versionMapStr != null
					&& !versionMapStr.isEmpty())
				projectIdToFormTypeToVer = objectMapper.readValue(versionMapStr, new TypeReference<Map<UUID, Map<String, Integer>>>() {});

			AppFormRequestObject appFormRequestObject = createAppFormRequestObject(appId, userId, token, superAppId, projectIdToFormTypeToVer);

			ServiceOutputObject serviceObject = appFormDataService.generateFormDataConfigJson(appFormRequestObject);

			response = createResponse(serviceObject, response);
		} catch (DataNotFoundException e) {
			Logger.error("In Project Type Configuration- Form data not found for given app");
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In Project Type Configuration- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			//e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In Project Type Configuration- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			Logger.error("In Project Type Configuration- Error processing json data");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In Project Type Configuration - Error reading properties file");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	public Result getProjectIdToLastSyncTs() {
		ProjectListRequestObject requestObject = new ProjectListRequestObject();
		ApiResponse response = getProjectListRequestObject(requestObject);
		if(!response.getResult().getSuccess()) {
			return ok(Json.toJson(response));
		}
		try {
			ServiceOutputObject serviceOutputObject = projListWithTSService.getProjectListConfig(requestObject);
			response = createResponse(serviceOutputObject, response);
		} catch (DataNotFoundException e) {
			Logger.error("In Project List Configuration- No projects found for given app and userid");
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In Project List Configuration- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			//e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In Project List Configuration- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In Project List Configuration- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (ValidationException e) {
			Logger.error("In Project List Configuration- Validation Exception");
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ok(Json.toJson(response));

	}

	public Result getProjectListJSON() {

		ProjectListRequestObject requestObject = new ProjectListRequestObject();
		ApiResponse response = getProjectListRequestObject(requestObject);
		if(!response.getResult().getSuccess()) {
			return ok(Json.toJson(response));
		}	
		try {
			ServiceOutputObject serviceObject = projListDataService.getProjectListConfig(requestObject);
			response = createResponse(serviceObject, response);
		} catch (DataNotFoundException e) {
			Logger.error("In Project List Configuration- No projects found for given app and userid");
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In Project List Configuration- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			//e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In Project List Configuration- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In Project List Configuration- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (ValidationException e) {
			Logger.error("In Project List Configuration- Validation Exception");
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}
	
	public Result getTransactionLogForKey() {
		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();
		try {
			KeyTransactionLog keyTransactionLog = objectMapper.readValue(jsonNode.toString(), KeyTransactionLog.class);
			List<String> result = transactionLogForKey.getLastNValuesForKey(keyTransactionLog);
			response.setResult(true, ResponseConstants.SUCCESS_CODE, result, ResponseConstants.SUCCESSFUL_MESSAGE);
		} catch (IOException e) {
			Logger.error("In getTransactionLogForKey- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	private ApiResponse getProjectListRequestObject(ProjectListRequestObject projListRequestObject) {

		UniAppResult result = new UniAppResult(true);
		ApiResponse response = new ApiResponse();
		response.setResult(result);

		JsonNode jsonNode = request().body().asJson();
		String userId =  jsonNode.findPath("userid").asText();
		String tokenStr =  jsonNode.findPath("token").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		String appIdStr = jsonNode.findPath("appid").asText();
		JsonNode metaDataListStr = jsonNode.findPath("md_instance_id");
		JsonNode projectIdListNode = jsonNode.findPath("project_list");
		String projectInstanceKey = jsonNode.findPath("instance_key").asText();
		UUID token = null;
		if (tokenStr != null) {
			token = UUID.fromString(tokenStr);
		} else {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			return response;
		}
		if ( !(validateInput(UUID.class, superAppStr) 
				&& validateInput(UUID.class, appIdStr)) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return response;
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		UUID appId = UUIDUtils.toUUID(appIdStr);
		List<String> metaDataList = null;
		List<UUID> projectIdList = null;
		try {
			if(!projectIdListNode.isMissingNode()
					&& projectIdListNode != null) {
				ObjectReader reader = objectMapper.readerFor(new TypeReference<List<UUID>>() {});

				projectIdList = reader.readValue(projectIdListNode);
			}
			if(!metaDataListStr.isMissingNode() 
					&& metaDataListStr != null) {
				ObjectReader reader = objectMapper.readerFor(new TypeReference<List<String>>() {});
				metaDataList = reader.readValue(metaDataListStr);
			}
			projListRequestObject.setSuperAppId(superAppId);
			projListRequestObject.setAppId(appId);
			projListRequestObject.setMetadataInstanceList(metaDataList);
			projListRequestObject.setProjectInstanceKey(projectInstanceKey);
			projListRequestObject.setProjectIdList(projectIdList);
			projListRequestObject.setTokenId(token);
			projListRequestObject.setUserId(userId);
		}  catch (IOException e) {
			Logger.error("In Project List Configuration- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return response;
	}
	
	public Result insertFormJson() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String projectIdStr =  jsonNode.findPath("projectid").asText();
		String formType =  jsonNode.findPath("formType").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		String projectExtId =  jsonNode.findPath("proj_ext_id").asText();
		String appIdStr = jsonNode.findPath("appid").asText();
		String metadataInstanceId = jsonNode.findPath("md_instance_id").asText();
		int isActive = jsonNode.findPath("isactive").asInt();
		String formJson = jsonNode.findPath("formjson").toString();

		if ( !(validateInput(UUID.class, superAppStr) 
				&& validateInput(UUID.class, appIdStr)) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}
		try {
			UUID superAppId =  UUIDUtils.toUUID(superAppStr);
			UUID appId = UUIDUtils.toUUID(appIdStr);
			UUID projectId = null;
			if(projectIdStr != null && !projectIdStr.isEmpty() && !projectIdStr.equals("null")) {
				projectId = UUIDUtils.toUUID(projectIdStr);
			}
			FormInsert formInsertObject = new FormInsert(superAppId, appId, formType, projectId, metadataInstanceId, isActive, formJson, projectExtId);
			formJsonInsertionService.insertFormJson(formInsertObject);
			response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.SUCCESSFUL_MESSAGE);
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		}  catch (InvalidInputException e) {
			Logger.error("In submitFormData() - Invalid input ");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In Project List Configuration- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	private AppFormRequestObject createAppFormRequestObject(UUID appId, String userId, UUID token, UUID superAppId, Map<UUID, Map<String, Integer>> projectIdToFormTypeToVersion) {

		AppFormRequestObject appFormRequestObject = new AppFormRequestObject(appId, projectIdToFormTypeToVersion);
		appFormRequestObject.setUserId(userId);
		appFormRequestObject.setTokenId(token);
		appFormRequestObject.setSuperAppId(superAppId);
		return appFormRequestObject;
	}

	public Result insertSuperAppFromJson() {

		JsonNode jsonNode = request().body().asJson();
		String superApp =  jsonNode.findPath("superappname").asText();
		String title =  jsonNode.findPath("title").asText();
		String subTitle =  jsonNode.findPath("subtitle").asText();
		String userNameType =  jsonNode.findPath("usernametype").asText();
		Object syncVisible =  jsonNode.findPath("syncvisible").asBoolean();
		String syncInterval =  jsonNode.findPath("syncInterval").asText();
		String gridColumns =  jsonNode.findPath("gridcolumns").asText();
		String colorAccent =  jsonNode.findPath("coloraccent").asText();
		String colorPrimary = jsonNode.findPath("colorprimary").asText();
		String colorPrimaryDark =  jsonNode.findPath("colorprimarydark").asText();
		String splashIcon =  jsonNode.findPath("splashicon").asText();
		String splashDuration =  jsonNode.findPath("splashduration").asText();
		String splashBackground = jsonNode.findPath("splashbackground").asText();
		String loginIcon = jsonNode.findPath("loginicon").asText();
		String superAppId = jsonNode.findPath("superappid").asText();
		String currentAppversion = jsonNode.findPath("currentplaystoreappversion").asText();
		Boolean acceptolderappdata = jsonNode.findPath("acceptolderappdata").asBoolean();
		JsonNode  serviceFrequencyOnApp = jsonNode.findPath("servicefrequency");
		String formSubmissionUplaodRetriesStr = jsonNode.findPath(AppMetaConfigurationConstants.FORM_SUBMISSION_UPLOAD_RETRIES).asText();
		String mediaUplaodRetriesStr = jsonNode.findPath(AppMetaConfigurationConstants.MEDIA_UPLOAD_RETRIES).asText();
		Integer formSubmissionUplaodRetries = null;
		if(formSubmissionUplaodRetriesStr != null && !formSubmissionUplaodRetriesStr.isEmpty()) {
			formSubmissionUplaodRetries = Integer.parseInt(formSubmissionUplaodRetriesStr);
		}
		Integer mediaUplaodRetries = null;
		if(mediaUplaodRetriesStr!= null && !mediaUplaodRetriesStr.isEmpty()) {
			mediaUplaodRetries = Integer.parseInt(mediaUplaodRetriesStr);
		}
        String awsConfiguarations = jsonNode.findPath(AppMetaConfigurationConstants.AWS_BUCKET_PROPERTIES).toString();
		ApiResponse response = new ApiResponse();

		if (argumentIsNull(superApp) || argumentIsNull(title) || argumentIsNull(subTitle) || argumentIsNull(userNameType)) {
			Logger.error("In insertSuperAppFromJson() - Found NULL parameters : superApp : " + superApp + " title : " + title + " subTitle : "
					+ subTitle + " userNameType : " + userNameType );
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
			return ok(Json.toJson(response));
		}
		Logger.error("SuperAppId :: " +superAppId);
		try {
			if(superAppId == null || superAppId.isEmpty()) {
				Map<String, Object> output = superAppInsertService.insertSuperAppConfigService(superApp, title, subTitle, userNameType, 
						(boolean) syncVisible, syncInterval, gridColumns, colorAccent, colorPrimary, colorPrimaryDark, 
						splashIcon, splashDuration, splashBackground, loginIcon, serviceFrequencyOnApp, formSubmissionUplaodRetries, mediaUplaodRetries,
						awsConfiguarations, currentAppversion, acceptolderappdata );
				response = ApiResponseFactory.createResponse(output);
			} else {
				Map<String, Object> output = superAppInsertService.updateSuperAppConfigService(superAppId, superApp, title, subTitle, userNameType, 
						(boolean) syncVisible, syncInterval, gridColumns, colorAccent, colorPrimary, colorPrimaryDark, 
						splashIcon, splashDuration, splashBackground, loginIcon, serviceFrequencyOnApp, formSubmissionUplaodRetries
						, mediaUplaodRetries, awsConfiguarations, currentAppversion, acceptolderappdata );
				response = ApiResponseFactory.createResponse(output);
			}
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		}  catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	public Result insertAppMetaDataJson() {

		JsonNode jsonNode = request().body().asJson();
		String superAppStr = jsonNode.findPath("superapp").asText();
		int noOfApps = jsonNode.findPath("noofapps").asInt();
		List<AppJsonData> listOfAppData = new ArrayList<>();
		ApiResponse response = new ApiResponse();

		Iterator<JsonNode> iter = jsonNode.findPath("apps").iterator();
		while(iter.hasNext()){
			JsonNode appNode = iter.next();
			ObjectMapper mapper = new ObjectMapper();
			try {
				AppJsonData jsonNodeAppData = mapper.readValue(appNode.toString(), new TypeReference<AppJsonData>(){});
				listOfAppData.add(jsonNodeAppData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			if ( argumentIsNull(superAppStr)) {
				Logger.error("In insertAppMetaDataJson() - Found NULL parameters : superApp : " + superAppStr);
				response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
				return ok(Json.toJson(response));
			}
			Map<String,Object> output = appDataInsertService.insertAppDataConfigService(superAppStr, noOfApps, listOfAppData);
			response = ApiResponseFactory.createResponse(output);
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ok(Json.toJson(response));

	}

	public boolean argumentIsNull (String argument) {
		if (argument.equals("null")) {
			return true;
		}
		return false;	
	}

	public Result insertFieldMetaDataJson() {

		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();

		ObjectMapper mapper = new ObjectMapper();
		try {
			FieldsDataObject jsonNodeFieldData = mapper.readValue(jsonNode.toString(), new TypeReference<FieldsDataObject>(){});
			Map<String, Object> output = fieldDataInsertService.insertFieldDataConfigService(jsonNodeFieldData);
			response = ApiResponseFactory.createResponse(output);
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		}  catch (InvalidInputException e) {
			Logger.error("In insertFieldMetaDataJson() - Invalid input");
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In insertFieldMetaDataJson - IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Result fetchLatestDataOfProjectWithUsers() {

		JsonNode jsonNode = request().body().asJson();
		ApiResponse response = new ApiResponse();

		String superAppStr =  jsonNode.findPath("superapp").asText();
		String appIdStr = jsonNode.findPath("appid").asText();
		JsonNode listJson = jsonNode.findPath("external_ids");
		JsonNode userDataJson = jsonNode.findPath("user_data");
		JsonNode projectDataJson = jsonNode.findPath("project_data");
		JsonNode timestampJson = jsonNode.findPath("timestamp"); 

		Map<String, Object> output = new HashMap<>();

		if (!(validateInput(UUID.class, superAppStr) 
				&& validateInput(UUID.class, appIdStr))) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		try {
			List<String> externalIds = null;
			List<String> usersList = null;
			List<UserProjectMapData> userMapData = null;
			Map<String, Map<String, Object>> userProjectsData = new HashMap<>();
			Set<String> projectDataKeys;
			Map<String, UUID> extToInternalIdMap = new HashMap<>();
			Map<String, Map<String, Object>> projectsData;
			Map<String, List<UserProjectMapData>> userProjectMapData = new HashMap<>();
			Map<String, Map<String, Object>> usersData = new HashMap<>();
			Map<String, Object> projectData;
			boolean userProfileNeeded = true;
			boolean isTimestampNull;

			ObjectReader listReader = objectMapper.readerFor(new TypeReference<List<String>>() {});

			isTimestampNull = timestampJson == null || timestampJson.isMissingNode() || timestampJson.isNull() || !timestampJson.booleanValue();
			externalIds = (listJson == null || listJson.isMissingNode() || listJson.isNull()) ? null : listReader.readValue(listJson);
			userProfileNeeded = (userDataJson == null || userDataJson.isMissingNode() || userDataJson.isNull()) ? true : userDataJson.asBoolean(true);
			projectDataKeys = (projectDataJson == null || projectDataJson.isMissingNode() || projectDataJson.isNull()) ? null : new HashSet(listReader.readValue(projectDataJson));

			UUID superAppId =  UUIDUtils.toUUID(superAppStr);
			UUID appId = UUIDUtils.toUUID(appIdStr);
			usersList = new ArrayList<>();

			Map<String, ProjectExternalInternalMapData> extToInternalProjectObjectMap = projectExternalToInternalMappingCrudService.findProjectExternalInternalMapDataForProjectExternalIds(superAppId, appId, externalIds);
			for (String externalId: extToInternalProjectObjectMap.keySet()) {
				extToInternalIdMap.put(externalId, extToInternalProjectObjectMap.get(externalId).getProjectId());
			}

			UserDataRequestObject userDataRequestObject = new UserDataRequestObject();
			userDataRequestObject.setSuperAppId(superAppId);

			if(isTimestampNull)
				projectsData = new HashMap(projListDataRetreivalService.getLatestDataFromProjExtIds(superAppId, appId, extToInternalIdMap));
			else
				projectsData = new HashMap(projListDataRetreivalService.getLatestDataFromProjExtIdsWithDataSubmitField(superAppId, appId, extToInternalIdMap));

			if(userProfileNeeded)
			{
				userProjectMapData = userProjectMapService.getUserProjectMapDataList(superAppId, appId, extToInternalIdMap);
				if(isTimestampNull)
					usersList = new ArrayList(projectsData.entrySet().parallelStream().flatMap(e -> Stream.of(e.getValue().get(CommonConstants.LAST_SYNC_USER_ID))).collect(Collectors.toList()));
				else
					usersList = new ArrayList(projectsData.entrySet().parallelStream().flatMap(e -> Stream.of(((DataSubmitField)(e.getValue().get(CommonConstants.LAST_SYNC_USER_ID))).getValue())).collect(Collectors.toList()));
				usersList.addAll(userProjectMapData.entrySet().parallelStream().flatMap(e -> e.getValue().stream().map(UserProjectMapData::getUserId)).collect(Collectors.toList()));
				usersList.removeIf(Objects::isNull);
				usersData = userInfoService.getUserInfoForListOfUsers(superAppId, usersList);
			}

			for(Entry<String, Map<String, Object>> entry : projectsData.entrySet()) {

				projectData = (Map)projectsData.get(entry.getKey());

				if(projectDataKeys != null && !projectDataKeys.isEmpty())
					projectData = projectData.entrySet().parallelStream().filter(i -> projectDataKeys.contains(i.getKey())).collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue()));

				userProjectsData.put(entry.getKey(), projectData);

				if(userProfileNeeded)
				{
					userMapData = userProjectMapData.get(entry.getKey());
					userProjectsData.get(entry.getKey()).put(ResponseConstants.USER_MAP, userMapData);
				}
			}

			output.put(ResponseConstants.PROJECT_DATA, userProjectsData);

			if(userProfileNeeded)
				output.put(ResponseConstants.USER_DATA, usersData);

			response = ApiResponseFactory.createResponse(output);

		} catch (IOException e) {
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (DataNotFoundException e) {
			response.setResult(false, ResponseConstants.NO_DATA_FOUND_ERROR_CODE, null, ResponseConstants.NO_DATA_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		}

		return ok(Json.toJson(response));
	}

	private void initialize() {
		int schedulerInitialDelayValue = (!checkForNullOrEmpty(properties.getProperty("scheduler_intial_delay"))) ?
				Integer.parseInt(properties.getProperty("scheduler_intial_delay")) : 6;
				int schedulerIntervalValue = (!checkForNullOrEmpty(properties.getProperty("scheduler_interval"))) ?
						Integer.parseInt(properties.getProperty("scheduler_interval")) : 1;
						TimeUnit schedulerInitialDelayTimeUnit = (!checkForNullOrEmpty(properties.getProperty("scheduler_intial_delay_time_unit"))) ?
								TimeUnit.valueOf(properties.getProperty("scheduler_intial_delay_time_unit")) : TimeUnit.HOURS;
								TimeUnit schedulerIntervalTimeUnit = (!checkForNullOrEmpty(properties.getProperty("scheduler_interval_time_unit"))) ?
										TimeUnit.valueOf(properties.getProperty("scheduler_interval_time_unit")) : TimeUnit.DAYS;
										Akka.system().scheduler().schedule(
												Duration.create(schedulerInitialDelayValue, schedulerInitialDelayTimeUnit), //Initial Delay
												Duration.create(schedulerIntervalValue, schedulerIntervalTimeUnit), //interval
												new Runnable() {
													@Override
													public void run() {
														System.out.println("Running scheduled job -----> "+ System.currentTimeMillis());
														try {
															usageAnalyticsData.getUsageAnalyticsExcelData();
														} catch (IOException e) {
															e.printStackTrace();
														}
													}
												},
												Akka.system().dispatcher()
												);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Logger.debug("This is printing from afterpropertieesSet");
		// initialize();
		runKafkaQueueConsumer();
		runKafkaQueueConsumerForFailedMessages();
		runBusinessAnalyticsComputation();
	}

	public void runKafkaQueueConsumer() {
		Akka.system().scheduler().scheduleOnce(
				Duration.create(1, TimeUnit.SECONDS), //interval
				new Runnable() {
					@Override
					public void run() {
						System.out.println("Running scheduled job -----> "+ System.currentTimeMillis());
						try {
							createKafkaConsumers.createConsumersForPrimaryQueue();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				},
				Akka.system().dispatcher()
				);
	}

	public boolean checkForNullOrEmpty(String propertyValue) {
		if(propertyValue == null || propertyValue.equalsIgnoreCase("")) {
			return true;
		}
		return false;
	}

	public void runKafkaQueueConsumerForFailedMessages() {
		Akka.system().scheduler().schedule(
				Duration.create(6, TimeUnit.HOURS), //Initial Delay
				Duration.create(24, TimeUnit.HOURS), //interval
				new Runnable() {
					@Override
					public void run() {
						System.out.println("Running scheduled job -----> "+ System.currentTimeMillis());
						try {
							createKafkaConsumers.createConsumersForFailedQueue();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				},
				Akka.system().dispatcher()
				);
	}
	public void runBusinessAnalyticsComputation() {
		Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.SECONDS), //Initial Delay
				Duration.create(24, TimeUnit.HOURS), //interval
				new Runnable() {
					@Override
					public void run() {
						System.out.println("Running scheduled job :: BusinessAnalyticsComputation -----> "+ System.currentTimeMillis());
						businessAnalyticsDataUpdateService.execute();
					}
				},
				Akka.system().dispatcher()
				);
	}
	public Result getLocalizationConfigJSON() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId =  jsonNode.findPath("userid").asText();
		String tokenStr =  jsonNode.findPath("token").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		String versionStr =  jsonNode.findPath("version").asText();
		int version = versionStr == null || versionStr.isEmpty() ? 0 : Integer.parseInt(versionStr);
		UUID token = null;
		if (tokenStr != null) {
			token = UUID.fromString(tokenStr);
		} else {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		if ( !validateInput(UUID.class, superAppStr) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		ApiRequestObject apiRequestObject = new ApiRequestObject(userId, token);
		apiRequestObject.setSuperAppId(superAppId);
		apiRequestObject.setVersion(version);

		try {
			ServiceOutputObject serviceObject;
			serviceObject = locazationConfigService.getLocalizationConfigData(apiRequestObject);
			response = createResponse(serviceObject, response);
		} 
		catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (UserNotFoundException e) {
			Logger.error("In Root Configuration- User Not Found");
			response.setResult(false, ResponseConstants.USER_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In Root Configuration- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			//e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In Root Configuration- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			Logger.error("In Root Configuration- Error processing json data");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In Root Configuration- Error reading properties file");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (AppMetaDataNotFoundException e) {
			Logger.error("In Root Configuration- Error fetching app meta data");
			response.setResult(false, ResponseConstants.APP_META_DATA_NOT_FOUND_ERROR_CODE, null, ResponseConstants.APP_META_DATA_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}
	public Result getMapConfigJSON() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String userId =  jsonNode.findPath("userid").asText();
		String tokenStr =  jsonNode.findPath("token").asText();
		String superAppStr =  jsonNode.findPath("superapp").asText();
		String versionStr =  jsonNode.findPath("version").asText();
		int version = versionStr == null || versionStr.isEmpty() ? 0 : Integer.parseInt(versionStr);
		UUID token = null;
		if (tokenStr != null) {
			token = UUID.fromString(tokenStr);
		} else {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		if ( !validateInput(UUID.class, superAppStr) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		ApiRequestObject apiRequestObject = new ApiRequestObject(userId, token);
		apiRequestObject.setSuperAppId(superAppId);
		apiRequestObject.setVersion(version);

		try {
			ServiceOutputObject serviceObject;
			serviceObject = mapConfigurationService.getMapConfiguration(apiRequestObject);
			response = createResponse(serviceObject, response);
		} 
		catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (UserNotFoundException e) {
			Logger.error("In Map Configuration- User Not Found");
			response.setResult(false, ResponseConstants.USER_NOT_FOUND_ERROR_CODE, null, ResponseConstants.USER_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenNotFoundException e) {
			Logger.error("In Map Configuration- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			//e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In Map Configuration- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			Logger.error("In Map Configuration- Error processing json data");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In Map Configuration- Error reading properties file");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (AppMetaDataNotFoundException e) {
			Logger.error("In Map Configuration- Error fetching app meta data");
			response.setResult(false, ResponseConstants.APP_META_DATA_NOT_FOUND_ERROR_CODE, null, ResponseConstants.APP_META_DATA_NOT_FOUND_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}
	public Result downloadMapFiles() {

		JsonNode jsonNode = request().body().asJson();
		String superAppStr =  jsonNode.findPath("super_app").asText();
		ApiResponse response = new ApiResponse();

		if ( !(validateInput(UUID.class, superAppStr) )) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superAppId =  UUIDUtils.toUUID(superAppStr);
		
		try {
			CreateMapBBoxRequestObject createMapBBoxRequestObject = objectMapper.treeToValue(jsonNode, CreateMapBBoxRequestObject.class);
		
			Boolean bboxCreated = bboxGenrationUtil.bboxFormationForSuperApp(createMapBBoxRequestObject);
			if(bboxCreated) {
				response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.SUCCESSFUL_MESSAGE);
			} else {
				response.setResult(true, ResponseConstants.FAILURE, null, ResponseConstants.UNSUCCESSFUL_OPERATION);
			}
			
		} catch (CassandraConnectionFailureException | CassandraWriteTimeoutException
				| CassandraInvalidQueryException | CassandraTypeMismatchException | CassandraReadTimeoutException 
				| CassandraQuerySyntaxException | CassandraInternalException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Logger.error("In appMetaConfigJson()- Error Mapping json");
			response.setResult(false, ResponseConstants.JSON_MAPPING_ERROR_CODE, null, ResponseConstants.JSON_MAPPING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch(JsonParseException e) {
			Logger.error("In appMetaConfigJson()- Error parsing json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PARSING_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error("In appMetaConfigJson()- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} 
		return ok(Json.toJson(response));
	}
	public Result insertLocalizationConfigJSON() {

		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		String superAppStr =  jsonNode.findPath("super_app").asText();
		String versionStr =  jsonNode.findPath("version").asText();
		String config = jsonNode.findPath("config").toString();
		int version = versionStr == null || versionStr.isEmpty() ? 0 : Integer.parseInt(versionStr);
		UUID token = null;
		if (config == null) {
			response.setResult(false, ResponseConstants.FAILURE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		if ( !validateInput(UUID.class, superAppStr) ) {
			response.setResult(false, ResponseConstants.INVALID_INPUT_ERROR_CODE, null, ResponseConstants.INVALID_INPUT_ERROR_MESSAGE);
			return ok(Json.toJson(response));
		}

		UUID superId = UUIDUtils.toUUID(superAppStr);
		try {
			ServiceOutputObject serviceObject;
			serviceObject = localizationConfigInsert.insertLocalizationConfig(superId, config);
			response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.SUCCESSFUL_MESSAGE);
			return ok(Json.toJson(response));
		} 
		catch (CassandraConnectionFailureException | CassandraWriteTimeoutException |CassandraTypeMismatchException 
				| CassandraReadTimeoutException | CassandraQuerySyntaxException | CassandraInternalException
				| CassandraInvalidQueryException e) {
			response.setResult(false, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_CODE, null, ResponseConstants.CASSANDRA_EXCEPTION_ERROR_MESSAGE);
			e.printStackTrace();
		}   
		return ok(Json.toJson(response));
	}
	public Result getEntityMetaConfig() {
		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		EntityMetadataConfigRequestObject entityMetadataConfigRequestObject = null;
		try {
			entityMetadataConfigRequestObject = objectMapper.treeToValue(jsonNode, EntityMetadataConfigRequestObject.class);
		} catch (JsonProcessingException e1) {
			Logger.error("In EntityMetadataConfig  Generator - Json processing exception");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e1.printStackTrace();
			return ok(Json.toJson(response));
		}

		try {
			ServiceOutputObject serviceObject;
			serviceObject = entityMetadataConfigurationService.getEntityConfigData(entityMetadataConfigRequestObject);
			response = createResponse(serviceObject, response);
		} catch (JsonProcessingException e) {
			Logger.error("In EntityMetadataConfig Generator- Unable to parse json");
			response.setResult(false, ResponseConstants.JSON_PARSING_ERROR_CODE, null, ResponseConstants.JSON_PARSING_ERROR_MESSAGE);
			e.printStackTrace();
		}  catch (TokenNotFoundException e) {
			Logger.error("In EntityMetadataConfig Generator- Error processing token : Token Not Found");
			response.setResult(false, ResponseConstants.TOKEN_NULL_ERROR_CODE, null, ResponseConstants.TOKEN_NULL_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (TokenExpiredException e) {
			Logger.error("In EntityMetadataConfig Generator- Error processing token : Token Expired");
			response.setResult(false, ResponseConstants.TOKEN_EXPIRED_ERROR_CODE, null, ResponseConstants.TOKEN_EXPIRED_ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.error(" EntityMetadataConfig Generator- IO Exception");
			response.setResult(false, ResponseConstants.IO_ERROR_CODE, null, ResponseConstants.IO_ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return ok(Json.toJson(response));
	}
	public Result insertEntityMetaConfigJSON() {
		
		ApiResponse response = new ApiResponse();
		JsonNode jsonNode = request().body().asJson();
		EntityMetaConfigInsertObject entityMetaConfigRequestObject = null;
		try {
			entityMetaConfigRequestObject = objectMapper.treeToValue(jsonNode, EntityMetaConfigInsertObject.class);
			entityMetaConfigInsertService.insertEntityMetaConfig(entityMetaConfigRequestObject);
			response.setResult(true, ResponseConstants.SUCCESS_CODE, null, ResponseConstants.SUCCESSFUL_MESSAGE);
			return ok(Json.toJson(response));
		} catch (JsonProcessingException e1) {
			Logger.error("In Entity Meta Config - Json processing exception");
			response.setResult(false, ResponseConstants.JSON_PROCESSING_ERROR_CODE, null, ResponseConstants.JSON_PROCESSING_ERROR_MESSAGE);
			e1.printStackTrace();
			return ok(Json.toJson(response));
		}
		
	}
}