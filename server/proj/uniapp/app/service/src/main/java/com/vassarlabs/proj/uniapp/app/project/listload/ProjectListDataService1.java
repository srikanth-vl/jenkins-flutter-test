package com.vassarlabs.proj.uniapp.app.project.listload;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.prod.spel.common.call.SPELExpressionValidator;
import com.vassarlabs.proj.uniapp.api.pojo.ProjectIconInfo;
import com.vassarlabs.proj.uniapp.api.pojo.ProjectListConfigObject;
import com.vassarlabs.proj.uniapp.api.pojo.ProjectListRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.IProjectListDataService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FieldMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.FormSubmitData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectMasterData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapDBData;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.MasterDataKeyNames;
import com.vassarlabs.proj.uniapp.constants.ProjectListConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.FieldMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.data.retrieve.DataRetrievalService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.KeyTypes;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserPriorities;
import com.vassarlabs.proj.uniapp.validations.api.pojo.ValidationObject;

@Component("ProjectListDataService1")
public class ProjectListDataService1
	implements IProjectListDataService {

	@Autowired private DataRetrievalService dataRetrievalService;
	@Autowired private UserProjectMapCrudService userProjMappingData;
	@Autowired private FieldMetaDataCrudService fieldDataCrudService;

	@Autowired private SPELExpressionValidator spelExpressionValidator;
	@Autowired private ApplicationMetaDataCrudService applicationMetaDataCrudService;

	@Autowired private IVLLogService logFactory;
	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	private ObjectMapper objectMapper = new ObjectMapper();
	boolean showMap = false;

	@Override
	public ServiceOutputObject getProjectListConfig(ProjectListRequestObject projListRequest)
			throws TokenNotFoundException, TokenExpiredException, IOException, DataNotFoundException,
			ValidationException {

		Map<String, Object> output = new HashMap<>();
		if (projListRequest == null) {
			logger.error("ProjectListRequestObject is NULL");
			return null;
		}

		UUID superAppId = projListRequest.getSuperAppId();
		UUID appId = projListRequest.getAppId();
		String userId = projListRequest.getUserId();
		UUID token = projListRequest.getTokenId();
		List<String> metaDataInstanceIds = projListRequest.getMetadataInstanceList();
		List<UUID> projectIdList = projListRequest.getProjectIdList();
		if (metaDataInstanceIds == null) {
			logger.info("No meta data instance Id List found for the request for ProjectListRequestObject : " + projListRequest);
			return null;
		}

		List<Map<String, Object>> userTypeProjectList = new ArrayList<>();
		output.put(ProjectListConstants.USER_ID, String.valueOf(userId));
		Set<String> userTypeSet = new HashSet<>();
		output.put(ProjectListConstants.PROJECTS, userTypeProjectList);
		output.put(ProjectListConstants.TYPES, userTypeSet);
		output.put(ProjectListConstants.SHOW_MAP, showMap);
		
		List<UserProjectMapDBData> userProjectMappingData = userProjMappingData.findUserProjectMappingDBDataByUserId(superAppId, appId, userId);
		if (userProjectMappingData == null || userProjectMappingData.isEmpty()) {
			logger.info("No project found for user: " + projListRequest.getUserId());
			long trackingTS = System.currentTimeMillis();
			UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.PROJECT_LIST_CONFIG,
					ServiceNamesConstants.PROJECT_LIST_CONFIG_NAME, objectMapper.writeValueAsString(projListRequest), null, true, trackingTS);
			output.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
			ServiceOutputObject outputObject = new ServiceOutputObject(output, trackingObject, true);
			return outputObject;
		}
		
		if(projectIdList == null) {
			// Get all projects assigned to that user
			projectIdList = userProjectMappingData.stream().map(UserProjectMapDBData::getProjectId).collect(Collectors.toList());
		}
		if (projectIdList.size() > 4000) {
			List<UUID> allProjectIds = projectIdList;
			projectIdList =  new ArrayList<>();
			int i = 0;
			for (UUID id : allProjectIds) {
				if(id != null) {
					projectIdList.add(id);
					i++;
					if (i == 4000) {
						break;
					}
				}
			}
		}
		
		Map<UUID, List<FieldMetaData>> projectIdToFieldsMetaDataMap = fieldDataCrudService.getProjectIdToFieldsData(superAppId, appId, metaDataInstanceIds);	
		if(projectIdToFieldsMetaDataMap == null 
				|| projectIdToFieldsMetaDataMap.isEmpty()) {
			logger.error("ProjectIdToFieldsMetaDataMap is empty");
			return null;
		}

		Map<UUID, List<ProjectMasterData>> projectIdToMasterDataMap = dataRetrievalService.getProjectIdToListOfMasterData(superAppId, appId, projectIdList, ProjectStates.getValidProjectStates());

		Map<String, Object> formatterAndAttributes = applicationMetaDataCrudService.getFormatterAndAttributesList(superAppId, appId);
				
		for (UserProjectMapDBData userProjectMapping : userProjectMappingData) {
			
			UUID projectId = userProjectMapping.getProjectId();
			Integer userType = userProjectMapping.getUserType();

			// Filter out only those projects that have to be sent to the app
			if(projectIdList.contains(projectId)) {
				userTypeSet.add(UserPriorities.getAPINameByValue(userType).name());

				Map<String, Object> projectInfoMap = initializeProjectInfoMap();
				projectInfoMap.put(ProjectListConstants.PROJECT_ID, projectId);
				projectInfoMap.put(ProjectListConstants.USER_TYPE, UserPriorities.getAPINameByValue(userType).name());

				List<FieldMetaData> fieldMetaDataList = null;
				if (projectIdToFieldsMetaDataMap.keySet().contains(projectId)) {
					fieldMetaDataList = projectIdToFieldsMetaDataMap.get(projectId);
				} else {
					fieldMetaDataList = projectIdToFieldsMetaDataMap.get(UUIDUtils.getDefaultUUID());
				}
				if(projectIdToMasterDataMap.get(projectId) == null) {
					continue;
				}
				if(fieldMetaDataList == null) {
					continue;
				}
				Map<String, FormSubmitData> keyToDataValues = dataRetrievalService.getValueObjectForAProject(superAppId, appId, projectId, projectIdToMasterDataMap.get(projectId));
				List<Map<String, Object>> fieldsObjectsList = generateFieldsData(fieldMetaDataList, keyToDataValues, projectInfoMap, projectIdToMasterDataMap.get(projectId), formatterAndAttributes);

				projectInfoMap.put(ProjectListConstants.FIELDS, fieldsObjectsList);
				userTypeProjectList.add(projectInfoMap);
			}
			output.put(ProjectListConstants.SHOW_MAP, showMap);
			output.put(ProjectListConstants.TYPES, userTypeSet);
			output.put(ProjectListConstants.PROJECTS, userTypeProjectList);
		}
		
		long trackingTS = System.currentTimeMillis();
		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.PROJECT_LIST_CONFIG,
				ServiceNamesConstants.PROJECT_LIST_CONFIG_NAME, objectMapper.writeValueAsString(projListRequest), null, true, trackingTS);
		output.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
		ServiceOutputObject outputObject = new ServiceOutputObject(output, trackingObject, true);
		return outputObject;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> generateFieldsData(List<FieldMetaData> fieldMetaDataList, 
			Map<String, FormSubmitData> keyToDataValues, Map<String, Object> projectInfoMap, List<ProjectMasterData> list, 
			Map<String, Object> formatterAndAttributes) 
					throws JsonParseException, JsonMappingException, IOException, DataNotFoundException, ValidationException {

		List<Map<String, Object>> fieldsObjectsList = new ArrayList<>();
		Map<String, String> formatter = (Map<String, String>) formatterAndAttributes.get(CommonConstants.DATATYPE_FORMATTER);
		List<String> attributes = (List<String>) formatterAndAttributes.get(CommonConstants.ATTRIBUTES);

		for (FieldMetaData fieldMetaData : fieldMetaDataList) {
			String uom = null;
			String fieldKey = fieldMetaData.getKey();
			String defaultUom = fieldMetaData.getUom();
			Map<String, Object> fieldMap = new LinkedHashMap<>();
			fieldMap.put(ProjectListConstants.IDENTIFIER, fieldKey);
			uom  = keyToDataValues.get(fieldKey) == null ? null : keyToDataValues.get(fieldKey).getUom();
			uom = uom == null || uom.isEmpty() ? defaultUom : uom;
			// Populate master data as part of project Json
			if (ProjectListConstants.MASTER_TO_PROJECT_CONSTANT_MAP.containsKey(fieldKey)
					&& fieldMetaData.getKeyType() == KeyTypes.MASTER_DATA_KEY.getValue()) {
				String value = keyToDataValues.get(fieldKey) == null ? null : keyToDataValues.get(fieldKey).getValue();
				if(value != null) {
					if(fieldKey.equalsIgnoreCase(MasterDataKeyNames.GEO_TAG_KEY)) {
						setGeoTagValues(value, projectInfoMap);
					} else if(fieldKey.equalsIgnoreCase(MasterDataKeyNames.LAST_SUBMISSION_DATE) && value !=null) {
						long ts = DateUtils.getTimestamp(value, formatter.get(CommonConstants.DATATYPE_DATE));
						projectInfoMap.put(ProjectListConstants.MASTER_TO_PROJECT_CONSTANT_MAP.get(fieldKey), ts);
					} else if(fieldKey.equalsIgnoreCase(MasterDataKeyNames.PROJECT_ICON_KEY)){
						projectInfoMap.put(ProjectListConstants.MASTER_TO_PROJECT_CONSTANT_MAP.get(fieldKey), objectMapper.readValue(value, ProjectIconInfo.class));
					} else {
						projectInfoMap.put(ProjectListConstants.MASTER_TO_PROJECT_CONSTANT_MAP.get(fieldKey), value);
					}
				} else if(fieldKey.equalsIgnoreCase(MasterDataKeyNames.PROJECT_ICON_KEY) && value == null) {
					projectInfoMap.put(ProjectListConstants.MASTER_TO_PROJECT_CONSTANT_MAP.get(fieldKey), objectMapper.readValue("{\"static_url\":null,\"dynamic_key_name\":null}", ProjectIconInfo.class));
				//	System.out.println("ProjectInfoValue :: " + projectInfoMap.get(ProjectListConstants.MASTER_TO_PROJECT_CONSTANT_MAP.get(fieldKey)));
				}
				continue;
			}
			// App level validations
			if (fieldMetaData.getKeyType() == KeyTypes.DEFAULT_KEY.getValue()) {
				projectInfoMap.put(ProjectListConstants.VALIDATIONS, objectMapper.readValue(fieldMetaData.getValidations(), ValidationObject.class));
			} else {
				// Filtering attributes map -> key = attribute name(e.g district/mandal), val = attribute value(e.g Srikakulam) 
				if(attributes != null && !attributes.isEmpty() && attributes.contains(fieldKey)) {
					Map<String, String> attributesMap = (Map<String, String>) projectInfoMap.get(ProjectListConstants.ATTRIBUTES);
					if(attributesMap == null) {
						attributesMap = new HashMap<>();
						projectInfoMap.put(ProjectListConstants.ATTRIBUTES, attributesMap);
					}
					attributesMap.put(fieldKey, keyToDataValues.get(fieldKey) == null? null : keyToDataValues.get(fieldKey).getValue());

				}
				ProjectListConfigObject projListObject = setProjectConfigObject(keyToDataValues.get(fieldKey) == null? null : keyToDataValues.get(fieldKey).getValue(), 
						fieldMetaData.getDataType(), formatter.get(fieldMetaData.getDataType()), uom);
				createLabel(projListObject, keyToDataValues, fieldMetaData.getDisplayNames(), fieldMetaData.getTargetField(), fieldKey, formatter);
				fieldMap.put(ProjectListConstants.VALUE, projListObject);
				fieldsObjectsList.add(fieldMap);
			}
		}
		return fieldsObjectsList;
	}

	/**
	 * Parses the display Label expression in field meta data and generates label to be appended to the key name in form key
	 * @param projListObject
	 * @param keyToDataValues
	 * @param displayLabelName
	 * @param targetKey
	 * @param fieldKey
	 * @param formmater
	 * @throws DataNotFoundException
	 * @throws ValidationException
	 */
	private void createLabel(ProjectListConfigObject projListObject, Map<String, FormSubmitData> keyToDataValues,
			String displayLabelName, String targetKey, String fieldKey, Map<String, String> formmater)
					throws DataNotFoundException, ValidationException {

		if(displayLabelName == null
				|| displayLabelName.equals("")) {
			return;
		}
		String dateFormat = formmater.get(CommonConstants.DATATYPE_DATE);
		String timeFormat = formmater.get(CommonConstants.DATATYPE_TIME);
		Map<String, Map<String, String>> fieldToValuesMap = new HashMap<String,Map<String, String>> ();
		FormSubmitData currentFieldFData;
		Map<String, String> formSubmissionkeyToValuesMap ;
		ExpressionParser expressionParser = new SpelExpressionParser();
		SpelExpression expression = (SpelExpression) expressionParser.parseExpression(displayLabelName);
		Set<String> variablesForEvaluation = spelExpressionValidator.getVars(expression.getAST());
		String script = "";
		for (String scriptKey : variablesForEvaluation) {
			List<String> keys = Arrays.asList(scriptKey.split(CommonConstants.KEY_DELIMITER_REGEX));
			script = "";
			String field = keys.get(keys.size()-1);
			String emptyValue = ProjectListConstants.NA_STRING;
			if(keys.size() >= 3) {
				emptyValue = "";
			}
			if(keys.size() == 1) {
				script = script + "' '+ #"+ field+ "['value'] + ";
			} else {
				for (int i=0; i < keys.size()-1; i++) {
					script = script + "' '+ #"+ field+ "['"+ keys.get(i) +"'] + ";
				} 
			}
			script = script + "' '";
			displayLabelName = displayLabelName.replace("#"+scriptKey, script );
			formSubmissionkeyToValuesMap	= new HashMap<String, String> ();
			currentFieldFData = keyToDataValues.get(field);
			if(currentFieldFData == null ) {
				String value = ProjectListConstants.NA_STRING;
				formSubmissionkeyToValuesMap.put(ProjectListConstants.VALUE, value);
				formSubmissionkeyToValuesMap.put(ProjectListConstants.DATE, emptyValue);
				formSubmissionkeyToValuesMap.put(ProjectListConstants.TIME, emptyValue);
				formSubmissionkeyToValuesMap.put(ProjectListConstants.USER_ID, emptyValue);
				formSubmissionkeyToValuesMap.put(ProjectListConstants.USER_TYPE, emptyValue);
				formSubmissionkeyToValuesMap.put(ProjectListConstants.UOM, emptyValue);
			} else {
				formSubmissionkeyToValuesMap.put(ProjectListConstants.VALUE, currentFieldFData.getValue());
				String date = currentFieldFData.getDate() ==  CommonConstants.DEFAULT_INT_VALUE ? emptyValue : DateUtils.getDateFromModelDate(currentFieldFData.getDate(), dateFormat);
				String time = currentFieldFData.getTimestamp() == CommonConstants.DEFAULT_LONG_VALUE ? emptyValue : DateUtils.getDateInFormat(timeFormat,  currentFieldFData.getTimestamp());
				formSubmissionkeyToValuesMap.put(ProjectListConstants.DATE, date);
				formSubmissionkeyToValuesMap.put(ProjectListConstants.TIME, time);
				formSubmissionkeyToValuesMap.put(ProjectListConstants.USER_ID, currentFieldFData.getUserId());
				formSubmissionkeyToValuesMap.put(ProjectListConstants.USER_TYPE, String.valueOf(currentFieldFData.getUserType()));
				formSubmissionkeyToValuesMap.put(ProjectListConstants.UOM, currentFieldFData.getUom());
			}
			fieldToValuesMap.put(field,formSubmissionkeyToValuesMap);

		}
		if(projListObject.getLabel() == null) {
			projListObject.setLabel(ProjectListConstants.EMPTY_STRING);
		}
		String label = ""; 
		label = spelExpressionValidator.evaluateSpelExpression(displayLabelName, fieldToValuesMap);

		projListObject.setLabel(label);
	}

	private ProjectListConfigObject setProjectConfigObject(String value, String dataType, String format, String uom) throws JsonParseException, JsonMappingException, IOException {
		ProjectListConfigObject projectListConfigObject = new ProjectListConfigObject();
		if(value == null) {
			projectListConfigObject.setValue(value);
		} else {
			projectListConfigObject.setValue(getValueInGivenFormat(value, dataType, format));
		}
		projectListConfigObject.setUom(uom);
		return projectListConfigObject;
	}

	private void setGeoTagValues(String value, Map<String, Object> projectInfoMap) {
		String[] geotag = value.split(",");
		if(geotag.length == 2) {
			projectInfoMap.put(ProjectListConstants.GEO_TAG_LAT, geotag[0]);
			projectInfoMap.put(ProjectListConstants.GEO_TAG_LONG, geotag[1]);
			showMap = validateLatLong(geotag[0], geotag[1]);
		}
	}

	private boolean validateLatLong(String latitudeStr, String longitudeStr) {
		try {
			Double latitude = Double.parseDouble(latitudeStr);
			Double longitude = Double.parseDouble(longitudeStr);
			if((latitude > -90 && latitude < 90)
					&& (longitude > -180 && longitude < 180)) {
				return true;
			}
			return false;
		} catch(NumberFormatException e) {
			return false;
		}
	}

	private Map<String, Object> initializeProjectInfoMap() {

		Map<String, Object> projectInfoMap = new HashMap<>();
		projectInfoMap.put(ProjectListConstants.PROJECT_ID, ProjectListConstants.EMPTY_STRING);
		projectInfoMap.put(ProjectListConstants.PROJECT_NAME, ProjectListConstants.NA_STRING);
		projectInfoMap.put(ProjectListConstants.STATE, ProjectListConstants.NA_STRING);
		projectInfoMap.put(ProjectListConstants.LAST_SYNC_TS, 0);
		projectInfoMap.put(ProjectListConstants.PRIORITY, ProjectListConstants.EMPTY_STRING);
		projectInfoMap.put(ProjectListConstants.EXTERNAL_PROJECT_ID, ProjectListConstants.EMPTY_STRING);
		projectInfoMap.put(ProjectListConstants.GEO_TAG_LAT, ProjectListConstants.EMPTY_STRING);
		projectInfoMap.put(ProjectListConstants.VALIDATIONS, new ValidationObject());
		projectInfoMap.put(ProjectListConstants.GEO_TAG_LONG, ProjectListConstants.EMPTY_STRING);
		projectInfoMap.put(ProjectListConstants.LAST_SUBMISSION_DATE, null);
		projectInfoMap.put(ProjectListConstants.ATTRIBUTES, null);
		return projectInfoMap;
	}

	public String getValueInGivenFormat(String value, String dataType, String format) {
		String formatedValue = "" ;
		if (value == null) {
			return formatedValue;
		} else if(dataType.equalsIgnoreCase(CommonConstants.DATATYPE_DOUBLE)) {
			DecimalFormat df = new DecimalFormat(format);
			df.setRoundingMode(RoundingMode.CEILING);
			Double dbvalue = Double.parseDouble(value);
			formatedValue = df.format(dbvalue);
		} else if(dataType.equalsIgnoreCase(CommonConstants.DATATYPE_TIMESTAMP)) {
			formatedValue  = DateUtils.getDateInFormat(format, Long.parseLong(value));

		} else {
			formatedValue = value;
		}
		return formatedValue;	
	}
}
