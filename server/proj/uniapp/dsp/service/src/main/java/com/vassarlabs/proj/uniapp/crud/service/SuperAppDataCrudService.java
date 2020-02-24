package com.vassarlabs.proj.uniapp.crud.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.data.cassandra.core.mapping.UnsupportedCassandraOperationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.rest.call.object.RequestObject;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.dsp.repository.SuperApplicationDataRepository;
import com.vassarlabs.proj.uniapp.utils.api.pojo.APIList;

@Component
public class SuperAppDataCrudService {

	@Autowired private SuperApplicationDataRepository repository;
	
	public void insertSuperAppData(SuperApplicationData data) throws CassandraConnectionFailureException, 
	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertSuperAppData(List<SuperApplicationData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }

    public List<SuperApplicationData> findDataByPartitionKey(UUID superAppId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(superAppId);
    }
    
    public SuperApplicationData findByPrimaryKey(UUID superAppId, int versionNumber) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPrimaryKey(superAppId, versionNumber);
    }
    
    public SuperApplicationData findLatestVersion(UUID superAppId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException {
    	return repository.findLatestVersion(superAppId);
    }
    
    public Iterable<SuperApplicationData> getAllSuperAppData(){
    	return repository.findAll();
    }
    
    public Map<String, Object> getSuperAppConfiguration(UUID superAppId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException, IOException {
    	SuperApplicationData superAppData= repository.findLatestVersion(superAppId);
    	ObjectMapper objectMapper = new ObjectMapper();
    	JsonNode jsonNode = objectMapper.readTree(superAppData.getConfigFile());
    	Iterator<String> iterator = jsonNode.fieldNames();
    	Map<String, Object> configuartion = new HashMap<>();
    	while(iterator.hasNext()) {
    		String key = iterator.next();
    		JsonNode value = jsonNode.get(key);
    		configuartion.put(key, value);
    	}
    	return configuartion;
    }
    public String getEntityConfig(UUID superAppId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException, IOException {
    	SuperApplicationData superAppData= repository.findLatestVersion(superAppId);
    	ObjectMapper objectMapper = new ObjectMapper();
    	JsonNode jsonNode = objectMapper.readTree(superAppData.getConfigFile());
    	Iterator<String> iterator = jsonNode.fieldNames();
    	while(iterator.hasNext()) {
    		String key = iterator.next();
    		if(key.equals("entity_config"))
    		{
    			return jsonNode.get(key).toString();
    		}
    	}
    	 return null;
    	
    }
    public APIList getApiListConfig(UUID superAppId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException, IOException {
    	SuperApplicationData superAppData= repository.findLatestVersion(superAppId);
    	
    	
    	APIList apilist = new APIList() ;
    	Map<String, List<RequestObject>> apiListMap = new HashMap<>();
    	if(superAppData != null && superAppData.getConfigFile() != null) {
    		ObjectMapper objectMapper = new ObjectMapper();
    		JsonNode jsonNode = objectMapper.readTree(superAppData.getConfigFile());
    		if(jsonNode.get(RootConfigurationConstants.EXTERNAL_API_LIST) != null) {
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
    public List<SuperApplicationData> getAllLatestSuperAppData(){
    	return repository.findLatestSuperapp();
    }
    
}
