package com.vassarlabs.proj.uniapp.crud.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.rest.call.object.RequestObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.dsp.repository.AppMetaDataRepository;
import com.vassarlabs.proj.uniapp.utils.DataTypeFormatter;
import com.vassarlabs.proj.uniapp.utils.api.pojo.APIList;

@Component
public class ApplicationMetaDataCrudService {

	@Autowired AppMetaDataRepository repository;
	
	public void insertApplicationData(ApplicationMetaData data) throws CassandraConnectionFailureException, 
	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertApplicationDBData(List<ApplicationMetaData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }
    
    public ApplicationMetaData findLatestAppDataByPartitionKey(UUID superAppId, UUID appId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findLatestByKey(superAppId, appId);
    }
    
    public Map<String, String> getFormmaterList(UUID superAppId, UUID appId) throws IOException {
    	ApplicationMetaData appMetaData = repository.findLatestByKey(superAppId, appId);
    	Map<String, String> fomatterList = getFormatterList(appMetaData);
    	return fomatterList;
    }
    
    private Map<String, String> getFormatterList(ApplicationMetaData appMetaData) throws IOException {
    	Map<String, String> formatterList =  new HashMap<>();
    	if(appMetaData != null) {
    		ObjectMapper objectMapper = new ObjectMapper();
    		JsonNode jsonNode = objectMapper.readTree(appMetaData.getConfigData());
    		if(jsonNode.get(RootConfigurationConstants.FORMATTER) != null) {
    			jsonNode = jsonNode.get(RootConfigurationConstants.FORMATTER);
    			for (String dataType : CommonConstants.getAllDataTypes()) {
    				JsonNode value = jsonNode.get(dataType);
    				formatterList.put(dataType, DataTypeFormatter.getFormatter(dataType, value == null ? "" : value.asText()));
    			}
    		} else {
    			for (String dataType : CommonConstants.getAllDataTypes()) {
    				formatterList.put(dataType, DataTypeFormatter.getFormatter(dataType, ""));
    			}
    		}
    	}
		return formatterList;
	}

	public Map<String, Object> getFormatterAndAttributesList(UUID superAppId, UUID appId) throws IOException {
		ApplicationMetaData appMetaData = repository.findLatestByKey(superAppId, appId);
		Map<String, Object> formatterAndAttributesMap = new HashMap<>();
    	Map<String, String> formatterList = getFormatterList(appMetaData);
    	if(formatterList != null && !formatterList.isEmpty()) 
    		formatterAndAttributesMap.put(CommonConstants.DATATYPE_FORMATTER, formatterList);
    	List<String> attributesList = getAttributesList(appMetaData);
    	if(attributesList != null && !attributesList.isEmpty())
    		formatterAndAttributesMap.put(CommonConstants.ATTRIBUTES, attributesList);
    	return formatterAndAttributesMap;
    }
    
    private List<String> getAttributesList(ApplicationMetaData appMetaData) throws IOException {
    	List<String> attributesList = new ArrayList<>();
    	if(appMetaData != null) {
    		ObjectMapper objectMapper = new ObjectMapper();
    		JsonNode jsonNode = objectMapper.readTree(appMetaData.getConfigData());
    		if(jsonNode.get(RootConfigurationConstants.ATTRIBUTES) != null) {
    			String attributesStr = jsonNode.get(RootConfigurationConstants.ATTRIBUTES).toString();
    			attributesList.addAll(objectMapper.readValue(attributesStr, new TypeReference<List<String>>(){})); 
    		}
    		if(jsonNode.get(RootConfigurationConstants.GROUPING_ATTRIBUTES) != null) {
    			String attributesStr = jsonNode.get(RootConfigurationConstants.GROUPING_ATTRIBUTES).toString();
    			if(attributesStr != null && !attributesStr.isEmpty()) {
    				attributesList.addAll(objectMapper.readValue(attributesStr, new TypeReference<List<String>>(){})); 
    			}
    		}
    	}
		return attributesList;
	}

	public APIList getExternalAPIList(UUID superAppId, UUID appId) throws IOException {
    	ApplicationMetaData appMetaData = repository.findLatestByKey(superAppId, appId);
    	APIList apilist = new APIList() ;
    	Map<String, List<RequestObject>> apiListMap = new HashMap<>();
    	if(appMetaData != null) {
    		ObjectMapper objectMapper = new ObjectMapper();
    		JsonNode jsonNode = objectMapper.readTree(appMetaData.getConfigData());
    		if(jsonNode.get(RootConfigurationConstants.EXTERNAL_API_LIST) != null) {
    			//JsonNode apiListNode = jsonNode.path(RootConfigurationConstants.EXTERNAL_API_LIST);
    			jsonNode = jsonNode.get(RootConfigurationConstants.EXTERNAL_API_LIST);
    			Iterator<String> iterator  = jsonNode.fieldNames();
    			while(iterator.hasNext()) {
    				String key = iterator.next();
    				JsonNode apiListNode = jsonNode.get(key);
    				List<RequestObject> apis = new ArrayList<>();
    			if (apiListNode != null && apiListNode.isArray()) {
    				for (JsonNode node : apiListNode) {
    					RequestObject requestObject = objectMapper.treeToValue(node, RequestObject.class);
    					apis.add(requestObject);
    				}
    			}
    			apiListMap.put(key, apis);
    			}
    		}
    	}
    	apilist.setApiList(apiListMap);
    	return apilist;
    }
    
    public List<ApplicationMetaData> getApplicationMetaDataForSuperApp(UUID superApp){
    	List<ApplicationMetaData> apps =  repository.findLatestApps(superApp);
    	if(apps == null ) {
    		apps = new ArrayList<>();
    	}
    	return apps;
    }
    
    public Map<UUID, ApplicationMetaData> findLatestDataBySuperappId( UUID superAppId ){
    	List<ApplicationMetaData> appsData = getApplicationMetaDataForSuperApp(superAppId);
    	Map<UUID, ApplicationMetaData> appToDataMap = new HashMap<UUID, ApplicationMetaData>();
    	for (ApplicationMetaData data : appsData) {
    		appToDataMap.put(data.getAppId(), data);
    	}
    	return appToDataMap;
    }
    
    public List<String> getEntityHeirarchyForApp(UUID superAppId, UUID appId) {
    	ApplicationMetaData appMetaData = repository.findLatestByKey(superAppId, appId);
    	List<String> attributesList = new ArrayList<>();
    	if(appMetaData != null) {
    		ObjectMapper objectMapper = new ObjectMapper();
    		JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(appMetaData.getConfigData());
			
    		if(jsonNode.get(RootConfigurationConstants.DASHBOARD_ANALYTICS_ENTITY_HIERARCHY) != null) {
    			String attributesStr = jsonNode.get(RootConfigurationConstants.DASHBOARD_ANALYTICS_ENTITY_HIERARCHY).toString();
    			if(attributesStr != null) {
    			attributesList.addAll(objectMapper.readValue(attributesStr, new TypeReference<List<String>>(){})); 
    			}
    		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		return attributesList;
    }

}
