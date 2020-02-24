package com.vassarlabs.proj.uniapp.appmetaconfig.service;
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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.AppRequestObject;
import com.vassarlabs.proj.uniapp.app.api.IAppMetaConfigJsonService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;

/**
 * Holds super app configuration
 * @author gpradeepng
 */
@Component
public class AppMetaConfigJsonService
	implements IAppMetaConfigJsonService {
	
	@Autowired private SuperAppDataCrudService superAppCrudService;
	
	@Autowired private IVLLogService logFactory;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Map<String, Object> getAppMetaConfigJson (AppRequestObject appRequestObject) 
			throws CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
			CassandraQuerySyntaxException, CassandraInternalException, IOException, DataNotFoundException {

		Map<String, Object> output = new HashMap<>();
		if(appRequestObject == null) {
			logger.error("In generateAppMetaConfigJson() - AppRequestObject is null");
			return output;
		}

		UUID superAppId = appRequestObject.getSuperAppId();
		int version = appRequestObject.getVersion();
		SuperApplicationData superAppLatestData = superAppCrudService.findByPrimaryKey(superAppId, version);
		if (superAppLatestData == null) {
			throw new DataNotFoundException("For App Meta Config Object : " + appRequestObject);
		}

		String outputString = superAppLatestData.getConfigFile();		
		output = objectMapper.readValue(outputString, new TypeReference<Map<String, Object>>(){});		
		return output;
	}
	
	@Override
	public Map<String, Object> getLatestAppMetaConfigJson(AppRequestObject appRequestObject) 
			throws CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
			CassandraQuerySyntaxException, CassandraInternalException, IOException, DataNotFoundException {

		Map<String, Object> output = new HashMap<>();
		if(appRequestObject == null) {
			logger.error("In generateAppMetaConfigJson() - AppRequestObject is null");
			return output;
		}

		UUID superAppId = appRequestObject.getSuperAppId();
		int version = appRequestObject.getVersion();
		SuperApplicationData superAppLatestData = superAppCrudService.findLatestVersion(superAppId);
		if (superAppLatestData == null) {
			throw new DataNotFoundException("For App Meta Config Object : " + appRequestObject);
		}

		int latestVersion = superAppLatestData.getVersionNumber();
		if (version < latestVersion) {
			String outputString = superAppLatestData.getConfigFile();		
			output = objectMapper.readValue(outputString, new TypeReference<Map<String, Object>>(){});		
		}
		return output;
	}
}
