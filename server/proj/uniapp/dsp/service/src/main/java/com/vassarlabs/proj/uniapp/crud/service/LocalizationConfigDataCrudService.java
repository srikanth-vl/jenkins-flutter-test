package com.vassarlabs.proj.uniapp.crud.service;

import java.io.IOException;
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
import com.vassarlabs.proj.uniapp.application.dsp.pojo.LocalizationConfigData;
import com.vassarlabs.proj.uniapp.dsp.repository.LocalizationConfigDataRepository;

@Component
public class LocalizationConfigDataCrudService {

@Autowired private LocalizationConfigDataRepository repository;
	ObjectMapper mapper =  new ObjectMapper();
	
	public void insertSuperAppData(LocalizationConfigData data) throws CassandraConnectionFailureException, 
	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertSuperAppData(List<LocalizationConfigData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }

    public List<LocalizationConfigData> findDataByPartitionKey(UUID superAppId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(superAppId);
    }
    
    public LocalizationConfigData findByPrimaryKey(UUID superAppId, int versionNumber) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPrimaryKey(superAppId, versionNumber);
    }
    
    public LocalizationConfigData findLatestVersion(UUID superAppId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException {
    	return repository.findLatestVersion(superAppId);
    }
    
    public Iterable<LocalizationConfigData> getAllLocalizationConfigData(){
    	return repository.findAll();
    }
    
    public String getLocalizationConfiguration(UUID superAppId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException, 
    		UnsupportedCassandraOperationException, DataAccessException, IOException {
    	LocalizationConfigData mapConfigData= repository.findLatestVersion(superAppId);
    	if(mapConfigData != null) {
    		return mapConfigData.getConfigData(); 
    	} else {
    		return null;
    	}
    }
    public List<LocalizationConfigData> getAllLatestLocalizationConfigData(){
    	return repository.findLatestLocalizationConfig();
    }
    
    

}
