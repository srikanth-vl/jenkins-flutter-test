package com.vassarlabs.proj.uniapp.app.form.load;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.utils.StringUtils;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.IFormDataJsonService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationFormData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserProjectMapping;
import com.vassarlabs.proj.uniapp.constants.AppFormConstants;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationFormDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.ActiveFlags;
import com.vassarlabs.proj.uniapp.enums.FormTypes;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class FormDataJsonService 
	implements IFormDataJsonService {

	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@Autowired private ApplicationFormDataCrudService appFormCrudService;	
	@Autowired private UserProjectMapCrudService userProjectMappingService;
	@Autowired private UserMetaDataCrudService userMetaDataCrudService;
	private ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	public ServiceOutputObject generateFormDataConfigJson(AppFormRequestObject appFormRequest) 
			throws TokenNotFoundException, TokenExpiredException, JsonParseException, 
			JsonMappingException, IOException, DataNotFoundException {

		Map<String, Object> output = new HashMap<>();
		if (appFormRequest == null) {
			logger.error("AppFormRequestObject is NULL");
			return null;
		}

		UUID superAppId = appFormRequest.getSuperAppId();
		UUID appId = appFormRequest.getAppId();
		String userId = appFormRequest.getUserId();
		UUID token = appFormRequest.getTokenId();
		Map<UUID, Map<String, Integer>> projectIdToFormTypeToVersionMap = appFormRequest.getProjectIdToFormTypeToVersionMap();

		Set<UUID> projectIdSet = new HashSet<>();

		UserDBMetaData userMetadataInfo = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);
		if (userMetadataInfo == null) {
			logger.warn("No User found : " + superAppId + ":" + userId);
			throw new DataNotFoundException("No User found : " + superAppId + ":" + userId);
		}

		Map<UUID, String> appActionsList = userMetadataInfo.getAppActions();
		if (appActionsList == null || appActionsList.get(appId) == null || appActionsList.get(appId).isEmpty() ){
			logger.info("User has no associated actions : " + superAppId + ":" + userId);
			throw new DataNotFoundException("User has no associated actions : " + superAppId + ":" + userId);
		}

		List<Integer> actions = StringUtils.getStringListFromDelimitter(CommonConstants.DELIMITER, appActionsList.get(appId)).stream().map(actionType -> Integer.parseInt(actionType)).collect(Collectors.toList());

		List<UserProjectMapping> userProjects = userProjectMappingService.findUserProjectMappingByUserId(superAppId, appId, userId);
		for(UserProjectMapping userProjectMapping : userProjects) {
			projectIdSet.addAll(userProjectMapping.getProjectList());
			projectIdSet.add(UUIDUtils.getDefaultUUID());
		}

		long trackingTS = System.currentTimeMillis();

		output.put(AppFormConstants.USER_ID, userId);
		output.put(AppFormConstants.PROJECT_TYPE, appId);
		output.put(AppFormConstants.DEPT_NAME, superAppId);

		Map<UUID, Object> projectIdToDataMap = new HashMap<>();
		Map<String, Object> formActionToDataMap = new HashMap<>();

		List<ApplicationFormData> appFormLatestDataList = appFormCrudService.findLatestApplicationFormData(superAppId
				, appId, projectIdSet, actions, ActiveFlags.ACTIVE);

		if (appFormLatestDataList != null &&  !appFormLatestDataList.isEmpty()) {
			for(ApplicationFormData appFormData : appFormLatestDataList) {
				UUID projectId = appFormData.getProjectId();
				if (!projectIdToDataMap.containsKey(projectId)) {
					projectIdToDataMap.put(projectId, new HashMap<>());
				}
				Map<String, Integer> formTypeToVersionValue = null;
				if(projectIdToFormTypeToVersionMap != null) {
					formTypeToVersionValue = projectIdToFormTypeToVersionMap.get(projectId);
				}
				formActionToDataMap = (Map<String, Object>) projectIdToDataMap.get(projectId);
				String formType = FormTypes.getFormNameByValue(appFormData.getFormType()).name();
				if(formTypeToVersionValue != null 
						&& formTypeToVersionValue.containsKey(formType)) {
					if(formTypeToVersionValue.get(formType) >= appFormData.getFormVersionNumber()) {
//						formActionToDataMap.put(formType, CommonConstants.EMPTY_STRING);
						continue;
					}
				}
				Map<String, Object> formJson = new HashMap<>();
				String formJsonString = appFormData.getFormJson();
				JsonNode jsonNode = objectMapper.readTree(formJsonString);
				Iterator<String> iterator = jsonNode.fieldNames();
				while(iterator.hasNext()) {
					String key = iterator.next();
					JsonNode value = jsonNode.get(key);
					formJson.put(key, value);
				}
				formJson.put(AppFormConstants.FORM_VERSION, appFormData.getFormVersionNumber());
				formJson.put(AppFormConstants.FORM_INSTANCE_ID, appFormData.getFormInstanceId());
				formJson.put(AppFormConstants.META_DATA_INSTANCE_ID, appFormData.getMetaDataInstanceId());
				formJson.put(AppFormConstants.ACTIVE, appFormData.getActiveFlag());

				formActionToDataMap.put(formType, formJson);
			}
		}
		output.put(AppFormConstants.CONTENT, projectIdToDataMap);
		output.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);

		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.PROJ_TYPE_CONFIG,
				ServiceNamesConstants.PROJECT_TYPE_CONFIG_NAME, objectMapper.writeValueAsString(appFormRequest), null, true, trackingTS);
		ServiceOutputObject outputObject = new ServiceOutputObject(output, trackingObject, true);
		return outputObject;
	}
}
