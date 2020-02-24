package com.vassarlabs.proj.uniapp.app.config.insert.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.AppJsonData;
import com.vassarlabs.proj.uniapp.app.api.IAppDataInsertService;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;

@Component
public class AppDataInsertService
	implements IAppDataInsertService {
	
	@Autowired private ApplicationMetaDataCrudService appDataCrudService;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Map<String, Object> insertAppDataConfigService(String superAppId, int noOfApps, List<AppJsonData> appJsonData) throws JsonProcessingException,
	CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
	CassandraQuerySyntaxException, CassandraInternalException {
		
		long insertTs = System.currentTimeMillis();
		List<ApplicationMetaData> appMetaDataList = new ArrayList<>();
		Map<String, Object> output = new HashMap<>();
		
		for (AppJsonData appData : appJsonData) {
			
			Map<String, Object> configMap = new HashMap<>();
			configMap.put(RootConfigurationConstants.APPLICATION_NAME, appData.getAppName());
			configMap.put(RootConfigurationConstants.ICON, appData.getIcon());
			configMap.put(RootConfigurationConstants.DESCRIPTION, appData.getDesc());
			configMap.put(RootConfigurationConstants.ALERT_INTERVAL, appData.getAlertInterval());
			configMap.put(RootConfigurationConstants.CLIENT_EXPIRY_INTERVAL, appData.getClientExpiry());
			configMap.put(RootConfigurationConstants.FORMATTER, appData.getFormatter());
			if(!(appData.getAttributes() == null 
					||  appData.getAttributes().isEmpty())) 
				configMap.put(RootConfigurationConstants.ATTRIBUTES, appData.getAttributes());
			configMap.put(RootConfigurationConstants.EXTERNAL_API_LIST, appData.getExternalAPIList());
			configMap.put(RootConfigurationConstants.SORT_TYPE, appData.getSortType());
			configMap.put(RootConfigurationConstants.ORDER, appData.getOrder());
			if(appData.getGroupingAttributes() != null && !appData.getGroupingAttributes().isEmpty())
				configMap.put(RootConfigurationConstants.GROUPING_ATTRIBUTES, appData.getGroupingAttributes());
			if(appData.getFilteringForm() != null) {
				configMap.put(RootConfigurationConstants.FILTERING_FORM, appData.getFilteringForm());
			}
			if(appData.getDisplayProjectIcon() != null) {
				configMap.put(RootConfigurationConstants.DISPLAY_PROJECT_ICON, appData.getDisplayProjectIcon());
			}
			String configFile = mapper.writeValueAsString(configMap);
			
			ApplicationMetaData appMetaData = new ApplicationMetaData();
			appMetaData.setAppId(appData.getAppId());
			appMetaData.setSuperAppId(UUIDUtils.toUUID(superAppId));
			appMetaData.setParentAppId(appData.getParentId());
			appMetaData.setInsertTs(insertTs);
			appMetaData.setConfigData(configFile);
			appMetaData.setVersionNumber(appData.getVersion());
			appMetaDataList.add(appMetaData);
		}
		
		appDataCrudService.insertApplicationDBData(appMetaDataList);
		output.put("object", appMetaDataList);
		return output;
	}

}
