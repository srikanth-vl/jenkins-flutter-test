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
import com.vassarlabs.proj.uniapp.application.dsp.pojo.EntityMetaData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.dsp.repository.AppMetaDataRepository;
import com.vassarlabs.proj.uniapp.dsp.repository.EntityMetadataRepository;
import com.vassarlabs.proj.uniapp.utils.DataTypeFormatter;
import com.vassarlabs.proj.uniapp.utils.api.pojo.APIList;

@Component
public class EntityMetadataCrudService {

	@Autowired EntityMetadataRepository repository;
	
	public void insertApplicationData(EntityMetaData data) throws CassandraConnectionFailureException, 
	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertApplicationDBData(List<EntityMetaData> data) throws CassandraConnectionFailureException, 
    CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }
    
    public List<EntityMetaData> findByPartitionKey (UUID superAppId, UUID appId) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(superAppId, appId);
    }
    
    public List<EntityMetaData> getEntityMetadataByPrimaryKey(UUID superAppId, UUID appId, UUID projectId, String userId, String parent, String name ) throws IOException {
    	List<EntityMetaData> entityMetaData = repository.findByPrimaryKey(superAppId, appId, projectId, userId, parent, name);
    	return entityMetaData;
    }
    
    public EntityMetaData getLatestEntityMetadata(UUID superAppId, UUID appId, UUID projectId, String userId, String parent, String name ) throws IOException {
    	EntityMetaData entityMetaData = repository.findLatestEntityMetadata(superAppId, appId, projectId, userId, parent, name);
    	return entityMetaData;
    }
    public List<EntityMetaData> getLatestEntitiesMetadataForUser(UUID superAppId, UUID appId, UUID projectId, String userId) throws IOException {
    	List<EntityMetaData> entityMetaData = repository.findLatestEntityMetadataForUser(superAppId, appId, projectId, userId);
    	return entityMetaData;
    }
    public List<EntityMetaData> getLatestEntityMetadataForApp(UUID superAppId, UUID appId) throws IOException {
    	List<EntityMetaData> entityMetaData = repository.findLatestEntityMetadataForApp(superAppId, appId);
    	if(entityMetaData  == null) {
    		entityMetaData = new ArrayList<>();
    	}
    	return entityMetaData;
    }
    public List<EntityMetaData> getLatestEntityMetadataForProject(UUID superAppId, UUID appId, UUID projectId) throws IOException {
    	List<EntityMetaData> entityMetaData = repository.findLatestEntityMetadataForProject(superAppId, appId, projectId);
    	if(entityMetaData  == null) {
    		entityMetaData = new ArrayList<>();
    	}
    	return entityMetaData;
    }
    
    
}
