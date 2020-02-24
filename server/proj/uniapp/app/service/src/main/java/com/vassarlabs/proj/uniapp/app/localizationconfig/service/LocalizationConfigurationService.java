package com.vassarlabs.proj.uniapp.app.localizationconfig.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import com.vassarlabs.proj.uniapp.constants.BackendPropertiesConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.constants.ServiceNamesConstants;
import com.vassarlabs.proj.uniapp.crud.service.LocalizationConfigDataCrudService;
import com.vassarlabs.proj.uniapp.enums.APITypes;

@Component
public class LocalizationConfigurationService {
	
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	@Autowired LocalizationConfigDataCrudService localizationConfigDataCrudService;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	
	public ServiceOutputObject getLocalizationConfigData(ApiRequestObject apiRequestObject) 
			throws IOException, TokenNotFoundException, TokenExpiredException, 
			AppMetaDataNotFoundException ,CassandraConnectionFailureException, 
			CassandraReadTimeoutException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, 
			CassandraQuerySyntaxException, CassandraTypeMismatchException, UserNotFoundException {

		Map<String, Object> localizationConfigMap = new HashMap<>();
		if (apiRequestObject == null) {
			logger.error("ApiRequestObject is NULL");
			return null;
		}
		
		UUID superAppId = apiRequestObject.getSuperAppId();
		String userId = apiRequestObject.getUserId();
		UUID token = apiRequestObject.getTokenId();
		UUID appId = superAppId;
		int maxVersion = -1;
		String config  = localizationConfigDataCrudService.getLocalizationConfiguration(superAppId);
		if(config == null || config.isEmpty()) {
			localizationConfigMap.put("config","{\"hi\":{\"Back\":\"पीछे\",\"Next\":\"अगला\",\"Submit\":\"सबमिट\",\"Preview\":\"पूर्वावलोकन\",\"Cancel\":\"हटाए\",\"Panchayat\":\"पंचायत\",\"District\":\"जिला\",\"Strucutre Type\":\"संरचना का प्रकार\",\"Watershed\":\"वाटरशेड\",\"Survey Number\":\"सर्वे नंबर\"}}");
		} else {
			localizationConfigMap.put("config", config);
		}
		localizationConfigMap.put(RootConfigurationConstants.VERSION, maxVersion);
		long trackingTS = System.currentTimeMillis();
		localizationConfigMap.put(BackendPropertiesConstants.CURRENT_SERVER_TIME, trackingTS);
		 
		UserTrackingObject trackingObject = new UserTrackingObject(superAppId, appId, userId, token, APITypes.LOCALIZATION_CONFIG,
				ServiceNamesConstants.LOCALIZATION_CONFIG_NAME, objectMapper.writeValueAsString(apiRequestObject), null, true, trackingTS);
		
		ServiceOutputObject output = new ServiceOutputObject(localizationConfigMap, trackingObject, true);
		return output;
	}
}
