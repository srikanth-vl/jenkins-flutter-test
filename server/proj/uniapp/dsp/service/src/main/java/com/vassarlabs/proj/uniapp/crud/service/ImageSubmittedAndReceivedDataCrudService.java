package com.vassarlabs.proj.uniapp.crud.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageSubmittedAndReceivedData;
import com.vassarlabs.proj.uniapp.dsp.repository.ImageSubmittedAndReceivedDataRepository;

@Component
public class ImageSubmittedAndReceivedDataCrudService {
	
	@Autowired private ImageSubmittedAndReceivedDataRepository repository;
	
	public List<ImageSubmittedAndReceivedData> findImageSubmittedAndReceivedDataByPartitionKey(UUID superAppId, UUID applicationId) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		return repository.findByPartitionKey(superAppId, applicationId);
	}


	public ImageSubmittedAndReceivedData findImageSubmittedAndReceivedDataByPrimaryKey(UUID superAppId, UUID applicationId, UUID projectId, UUID imageId) 
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException{
		return repository.findByPrimaryKey(superAppId, applicationId, projectId, imageId);
	}
	
	public void updateSubmittedImageData(Boolean submitStatus, UUID superAppId, UUID applicationId, UUID projectId, UUID imageId, long timestamp, long lastUpdatedTS)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException,
			CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.updateSubmittedImageData(submitStatus, lastUpdatedTS, superAppId, applicationId, projectId, imageId, timestamp);
	}
	
	
	public void updateReceivedImageData(Boolean receiveStatus, UUID superAppId, UUID applicationId, UUID projectId, UUID imageId,long instimestamp)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException,
			CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.updateReceivedImageData(receiveStatus, System.currentTimeMillis(), superAppId, applicationId, projectId, imageId , instimestamp);
	}
	
	public void updateRelayStatus(Integer relayStatus, UUID superAppId, UUID applicationId, UUID projectId, UUID imageId)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException,
			CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.updateRelayStatus(relayStatus, System.currentTimeMillis(), superAppId, applicationId, projectId, imageId);
	}
}
