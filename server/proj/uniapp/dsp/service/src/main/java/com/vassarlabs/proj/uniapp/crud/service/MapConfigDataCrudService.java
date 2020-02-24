package com.vassarlabs.proj.uniapp.crud.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapConfigData;
import com.vassarlabs.proj.uniapp.dsp.repository.MapConfigDataRepository;

@Component
public class MapConfigDataCrudService {

@Autowired private MapConfigDataRepository repository;
	ObjectMapper mapper =  new ObjectMapper();
	
	public void insertSuperAppData(MapConfigData data) throws CassandraConnectionFailureException, 
	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertSuperAppData(List<MapConfigData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }

    public List<MapConfigData> findDataByPartitionKey(UUID superAppId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(superAppId);
    }
    
    public MapConfigData findByPrimaryKey(UUID superAppId,  UUID appId,int versionNumber) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPrimaryKey(superAppId, appId, versionNumber);
    }
    
    public MapConfigData findLatestVersion(UUID superAppId, UUID appId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException {
    	return repository.findLatestVersion(superAppId, appId);
    }
    
    public Iterable<MapConfigData> getAllMapConfigData(){
    	return repository.findAll();
    }
    
    public String getMapConfiguration(UUID superAppId, UUID appId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException, IOException {
    	MapConfigData mapConfigData= repository.findLatestVersion(superAppId, appId);
    	if(mapConfigData != null) {
    		return mapConfigData.getConfigData();
    	} else {
    		return null;	
    	}
    }
    public List<MapConfigData> getAllLatestMapConfigData(UUID superAppId){
    	List<MapConfigData> data =  repository.findLatestAppMapConfig(superAppId);
    	if(data == null) {
    		data = new ArrayList<MapConfigData>();
    	}
    	return data;
    }
    
    

}
