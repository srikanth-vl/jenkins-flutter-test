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

import com.vassarlabs.proj.uniapp.application.dsp.pojo.BusinessAnalyticsData;
import com.vassarlabs.proj.uniapp.dsp.repository.BusinessAnalyticsDataRepository;

@Component
public class BusinessAnalyticsDataCrudService {

	@Autowired BusinessAnalyticsDataRepository repository;

	public void insertBusinessAnalyticsData(BusinessAnalyticsData data) throws CassandraConnectionFailureException, CassandraWriteTimeoutException, 
	CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.save(data);
	}

	public void insertBusinessAnalyticsData(List<BusinessAnalyticsData> data) throws CassandraConnectionFailureException, CassandraWriteTimeoutException, 
	CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.saveAll(data);
	}

	public List<BusinessAnalyticsData> findBusinessAnalyticsDataByPartitionKey(UUID superApp, UUID applicationId, String parentEntity)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {

		return repository.findByPartitionKey(superApp, applicationId, parentEntity);
	}

	public BusinessAnalyticsData findByPrimaryKey(UUID superApp, UUID applicationId, String parentEntity, String childEntity)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {

		return repository.findByPrimaryKey(superApp, applicationId, parentEntity, childEntity);
	}
	
}
