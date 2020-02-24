package com.vassarlabs.proj.uniapp.crud.service;

import java.util.ArrayList;
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

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectSubmissionAnalyticsData;
import com.vassarlabs.proj.uniapp.dsp.repository.ProjectSubmissionAnalyticsDataRepository;


@Component
public class ProjectSubmissionAnalyticsDataCrudService {
	
	@Autowired ProjectSubmissionAnalyticsDataRepository repository;
	
	public void insertHierarchicalData(ProjectSubmissionAnalyticsData data) throws CassandraConnectionFailureException, CassandraWriteTimeoutException, 
		CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.save(data);
	}
	
	public void insertHierarchicalData(List<ProjectSubmissionAnalyticsData> data) throws CassandraConnectionFailureException, CassandraWriteTimeoutException, 
		CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.saveAll(data);
	}
	
	public List<ProjectSubmissionAnalyticsData> findHierarchicalDataByPartitionKey(UUID superApp, UUID applicationId, String parentEntity)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		
		return repository.findByPartitionKey(superApp, applicationId, parentEntity);
	}
	
	public ProjectSubmissionAnalyticsData findByPrimaryKey(UUID superApp, UUID applicationId, String parentEntity, int date, String childEntity)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		
		return repository.findByPrimaryKey(superApp, applicationId, parentEntity, date, childEntity);
	}
	public List<ProjectSubmissionAnalyticsData> findSubmissionAnalyticsForForGivenParentEntity(UUID superApp, UUID applicationId, String parentEntity, int date)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		List<ProjectSubmissionAnalyticsData> data = new ArrayList<ProjectSubmissionAnalyticsData>();
		data = repository.findSubmissionCountForGivenParenEntity(superApp, applicationId, parentEntity, date);
		return data;
	}
	
}
