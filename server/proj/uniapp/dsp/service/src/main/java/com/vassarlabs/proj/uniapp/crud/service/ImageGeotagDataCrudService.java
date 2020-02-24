package com.vassarlabs.proj.uniapp.crud.service;

import java.util.List;
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

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageGeotagData;
import com.vassarlabs.proj.uniapp.dsp.repository.ImageGeotagDataRepository;

@Component
public class ImageGeotagDataCrudService {
	
	@Autowired private ImageGeotagDataRepository repository;
	
	public void insertImageGeotagData(ImageGeotagData data) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
    }
	
    public void insertListOfImageGeotagData(List<ImageGeotagData> data) 
    		throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.saveAll(data);
    }
    
    public ImageGeotagData findImageGeotagDataByPrimaryKey(UUID superAppId, UUID appId, UUID projectId, UUID fieldId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPrimaryKey(superAppId, appId, projectId, fieldId);
    }
    
    public List<ImageGeotagData> findImageGeotagDataByPartitionKey(UUID superAppId, UUID appId, UUID projectId) 
    		throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraInvalidQueryException, 
    		CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(superAppId, appId, projectId);
    }
}
