package com.vassarlabs.proj.uniapp.app.rootconfig.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.AppMetaDataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.ApiRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.api.IRootConfigurationService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
import com.vassarlabs.proj.uniapp.enums.UserStates;

@Component
public class RootConfigurationService 
	implements IRootConfigurationService {
	
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired UserMetaDataCrudService userMetaDataCrudService;
	@Autowired ApplicationMetaDataCrudService appMetaDataCrudService;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	@Override
	public ServiceOutputObject getRootConfigData(ApiRequestObject apiRequestObject) 
			throws IOException, TokenNotFoundException, TokenExpiredException, 
			AppMetaDataNotFoundException ,CassandraConnectionFailureException, 
			CassandraReadTimeoutException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, 
			CassandraQuerySyntaxException, CassandraTypeMismatchException, UserNotFoundException {

		Map<String, Object> rootConfigMap = new HashMap<>();
		if (apiRequestObject == null) {
			logger.error("ApiRequestObject is NULL");
			return null;
		}
		
		UUID superAppId = apiRequestObject.getSuperAppId();
		String userId = apiRequestObject.getUserId();
		UUID token = apiRequestObject.getTokenId();
		UUID appId = superAppId;
		int versionWithApp = apiRequestObject.getVersion();
		
		int maxVersion = -1;
		
		UserDBMetaData userData = userMetaDataCrudService.findUserDataByUserIdKey(superAppId, userId, UserStates.ACTIVE);
		if (userData == null) {
			logger.warn("User not found : " + superAppId + ":" + userId);
			throw new UserNotFoundException();
		} 
		
		List<Map<String, Object>> appDataList = new ArrayList<Map<String, Object>>(); 
		rootConfigMap.put(RootConfigurationConstants.USER_ID, userId);
		
		// Loop through Apps the user has permission on (ACTIVE status)
		if (userData.getAppActions() != null && !userData.getAppActions().isEmpty()) {
			Set<UUID> listOfApps = userData.getAppActions().keySet();
			for (UUID eachApp : listOfApps) {
				ApplicationMetaData appMetaData = appMetaDataCrudService.findLatestAppDataByPartitionKey(superAppId, eachApp);
				if (appMetaData != null) {
					Map<String, Object> appMetaDataMap = new HashMap<>();
					appMetaDataMap.put(RootConfigurationConstants.APPLICATION_ID, eachApp.toString());
					appMetaDataMap.put(RootConfigurationConstants.PARENT_APPLICATION_ID, appMetaData.getParentAppId().toString());
					
					String configData = appMetaData.getConfigData();					
					JsonNode jsonNode = objectMapper.readTree(configData);
					int version = appMetaData.getVersionNumber();
					if(version > maxVersion) {
						maxVersion = version;
					}
					Iterator<String> iterator = jsonNode.fieldNames();
					while(iterator.hasNext()) {
						String key = iterator.next();
						JsonNode value = jsonNode.get(key);
						appMetaDataMap.put(key, value);
					}
					
					Map<String, String> fomatterList = new HashMap<>();
					jsonNode = jsonNode.get(RootConfigurationConstants.FORMATTER);
					if(jsonNode != null) {
						iterator = jsonNode.fieldNames();
						while(iterator.hasNext()) {
							String key = iterator.next();
							JsonNode value = jsonNode.get(key);
							fomatterList.put(key, value.asText());
						}
						appMetaDataMap.put(RootConfigurationConstants.FORMATTER, fomatterList);
					}
					
					
					appDataList.add(appMetaDataMap);
				}
			} 
		}
		rootConfigMap.put(RootConfigurationConstants.VERSION, maxVersion);
		rootConfigMap.put(RootConfigurationConstants.APPLICATION_DATA, appDataList);
		long trackingTS = System.currentTimeMillis();
		rootConfigMap.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
		if(versionWithApp >= maxVersion) {
			rootConfigMap = new HashMap<>(); // If app has latest version, don't send back anything
		} 
		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.ROOT_CONFIG,
				ServiceNamesConstants.ROOT_CONFIG_NAME, objectMapper.writeValueAsString(apiRequestObject), null, true, trackingTS);
		
		ServiceOutputObject output = new ServiceOutputObject(rootConfigMap, trackingObject, true);
		return output;
	}
}
