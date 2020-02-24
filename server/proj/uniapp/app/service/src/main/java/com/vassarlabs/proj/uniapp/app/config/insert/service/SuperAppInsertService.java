	package com.vassarlabs.proj.uniapp.app.config.insert.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.app.api.ISuperAppInsertService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.constants.AppMetaConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;

@Component
public class SuperAppInsertService 
	implements ISuperAppInsertService {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired private SuperAppDataCrudService superAppCrudService;
	
	@Override
	public Map<String, Object> insertSuperAppConfigService(String superApp, String title, String subTitle, String userNameType, boolean syncVisible, 
			String syncInterval, String gridColumns, String colorAccent, String colorPrimary, String colorPrimaryDark, String splashIcon, 
			String splashDuration, String splashBackground, String loginIcon, JsonNode serviceFrequencyOnApp, Integer formSubmissionUploadRetries, 
			Integer mediaUplaodRetries, String awsBucketConfiguration, String currentPlaystoreAppVersion, boolean acceptDataFromOlderApp) throws JsonProcessingException, CassandraConnectionFailureException, CassandraTypeMismatchException, 
	CassandraReadTimeoutException, CassandraInvalidQueryException,  CassandraQuerySyntaxException, CassandraInternalException {
		
		Map<String, Object> output = new HashMap<>();
		Map<String, Object> appMetaConfigMap = generateParams(splashDuration, splashIcon, splashBackground, loginIcon, colorPrimary, colorPrimaryDark, colorAccent, gridColumns, title, subTitle, 
				userNameType, syncVisible, syncInterval, serviceFrequencyOnApp, formSubmissionUploadRetries, mediaUplaodRetries, currentPlaystoreAppVersion, acceptDataFromOlderApp);
		String configFile = objectMapper.writeValueAsString(appMetaConfigMap);
		UUID superAppId = UUIDUtils.getTrueTimeUUID();
		
		insertSuperAppData(superAppId, title, Integer.parseInt(AppMetaConfigurationConstants.DEFAULT_VERSION), configFile, awsBucketConfiguration);
		output.put(AppMetaConfigurationConstants.SUPER_APP_ID, superAppId);
		
		return output;
	}

	@Override
	public Map<String, Object> updateSuperAppConfigService(String superAppIdStr, String superApp, String title, String subTitle, String userNameType, boolean syncVisible, 
			String syncInterval, String gridColumns, String colorAccent, String colorPrimary, String colorPrimaryDark, String splashIcon, 
			String splashDuration, String splashBackground, String loginIcon, JsonNode serviceFrequencyOnApp, Integer formSubmissionUploadRetries, Integer mediaUplaodRetries,
			String awsBucketConfiguration, String currentPlaystoreAppVersion, boolean acceptDataFromOlderApp ) throws JsonProcessingException, CassandraConnectionFailureException, CassandraTypeMismatchException, 
	CassandraReadTimeoutException, CassandraInvalidQueryException,  CassandraQuerySyntaxException, CassandraInternalException {
		
		Map<String, Object> output = new HashMap<>();
		Map<String, Object> appMetaConfigMap = generateParams(splashDuration, splashIcon, splashBackground, loginIcon, colorPrimary, colorPrimaryDark, colorAccent, gridColumns, title, subTitle, 
				userNameType, syncVisible, syncInterval, serviceFrequencyOnApp, formSubmissionUploadRetries, mediaUplaodRetries,currentPlaystoreAppVersion, acceptDataFromOlderApp);
		String configFile = objectMapper.writeValueAsString(appMetaConfigMap);
		UUID superAppId = UUIDUtils.toUUID(superAppIdStr);
		SuperApplicationData superAppDataFromDB = superAppCrudService.findLatestVersion(superAppId);
		int version = -1;
		if(superAppDataFromDB == null) {
			version = 1;
		} else {
			version = superAppDataFromDB.getVersionNumber() + 1;
		}
		insertSuperAppData(superAppId, title, version, configFile, awsBucketConfiguration);
		output.put(AppMetaConfigurationConstants.SUPER_APP_ID, superAppId);
		return output;
	}
	
	private void insertSuperAppData(UUID superAppId, String title, int version, String configFile, String awsConfiguration) {
		SuperApplicationData superAppData = new SuperApplicationData();
		superAppData.setName(title);
		superAppData.setSuperAppId(superAppId);
		superAppData.setVersionNumber(version);
		superAppData.setConfigFile(configFile);
		superAppData.setInsertTs(System.currentTimeMillis());
		superAppData.setAwsProperties(awsConfiguration);

		superAppCrudService.insertSuperAppData(superAppData);
	}

	private Map<String, Object> generateParams(String splashDuration, String splashIcon, String splashBackground, String loginIcon, String colorPrimary, String colorPrimaryDark, String colorAccent,
			String gridColumns, String title, String subTitle, String userNameType, boolean syncVisible, String syncInterval, JsonNode serviceFrequencyOnApp, Integer formSubmissionUploadRetries, Integer mediaUplaodRetries, String currentPlaystoreAppVersion, boolean acceptDataFromOlderApp) {

		Map<String, Object> appMetaConfigMap = new HashMap<>();
		Map<String, String> splashConfigMap = new HashMap<>();
		Map<String, String> colorConfigMap = new HashMap<>();
		System.out.println("SyncVisible::" + syncVisible);
		splashConfigMap.put(AppMetaConfigurationConstants.SPLASH_DURATION, splashDuration);
		splashConfigMap.put(AppMetaConfigurationConstants.SPLASH_ICON, splashIcon);
		splashConfigMap.put(AppMetaConfigurationConstants.SPLASH_BACKGROUND, splashBackground);
		splashConfigMap.put(AppMetaConfigurationConstants.LOGIN_ICON, loginIcon);

		colorConfigMap.put(AppMetaConfigurationConstants.COLOR_PRIMARY, colorPrimary);
		colorConfigMap.put(AppMetaConfigurationConstants.COLOR_PRIMARY_DARK, colorPrimaryDark);
		colorConfigMap.put(AppMetaConfigurationConstants.COLOR_ACCENT, colorAccent);

		Map<String, String> urlPointsMap = generateDefaultConfigAPIMap();
		appMetaConfigMap.put(AppMetaConfigurationConstants.SPLASH_SCREEN_PROPERTIES, splashConfigMap);
		appMetaConfigMap.put(AppMetaConfigurationConstants.COLOR_SCHEME, colorConfigMap);
		appMetaConfigMap.put(AppMetaConfigurationConstants.URL_END_POINTS, urlPointsMap);

		appMetaConfigMap.put(AppMetaConfigurationConstants.TITLE, title);
		appMetaConfigMap.put(AppMetaConfigurationConstants.SUB_TITLE, subTitle);
		appMetaConfigMap.put(AppMetaConfigurationConstants.USER_NAME_TYPE, userNameType);
		appMetaConfigMap.put(AppMetaConfigurationConstants.SYNC_VISIBLE, syncVisible);
		appMetaConfigMap.put(AppMetaConfigurationConstants.SYNC_INTERVAL, syncInterval);

		appMetaConfigMap.put(AppMetaConfigurationConstants.GRID_COLUMNS, gridColumns);
		appMetaConfigMap.put(AppMetaConfigurationConstants.VERSION, AppMetaConfigurationConstants.DEFAULT_VERSION);

		appMetaConfigMap.put(AppMetaConfigurationConstants.CURRENT_PLAYSTORE_APP_VERSION,currentPlaystoreAppVersion);
		appMetaConfigMap.put(AppMetaConfigurationConstants.ACCEPT_DATA_FROM_OLDER_APP,acceptDataFromOlderApp);
		appMetaConfigMap.put(AppMetaConfigurationConstants.SERVICE_FREQUENCY, serviceFrequencyOnApp);
		if(formSubmissionUploadRetries != null && formSubmissionUploadRetries >  0) {
			appMetaConfigMap.put(AppMetaConfigurationConstants.FORM_SUBMISSION_UPLOAD_RETRIES, formSubmissionUploadRetries);
		}
		if(mediaUplaodRetries != null && mediaUplaodRetries >  0) {
			appMetaConfigMap.put(AppMetaConfigurationConstants.MEDIA_UPLOAD_RETRIES, mediaUplaodRetries);
		}
		return appMetaConfigMap;
	}

	private Map<String, String> generateDefaultConfigAPIMap() {
		
		Map<String, String> urlPointsMap = new HashMap<>();
		urlPointsMap.put(ServiceNamesConstants.ROOT_CONFIG_KEY, ServiceNamesConstants.ROOT_CONFIG_DEFAULT_API);
		urlPointsMap.put(ServiceNamesConstants.LOGIN_CONFIG_KEY, ServiceNamesConstants.LOGIN_CONFIG_DEFAULT_API);
		urlPointsMap.put(ServiceNamesConstants.PROJECT_TYPE_CONFIG_KEY, ServiceNamesConstants.PROJECT_TYPE_CONFIG_DEFAULT_API);
		urlPointsMap.put(ServiceNamesConstants.PROJECT_LIST_CONFIG_KEY, ServiceNamesConstants.PROJECT_TYPE_CONFIG_DEFAULT_API);
		urlPointsMap.put(ServiceNamesConstants.LOGOUT_CONFIG_KEY, ServiceNamesConstants.LOGOUT_CONFIG_DEFAULT_API);
		urlPointsMap.put(ServiceNamesConstants.SYNC_CONFIG_KEY, ServiceNamesConstants.SYNC_CONFIG_DEFAULT_API);
		return urlPointsMap;
	}
}
