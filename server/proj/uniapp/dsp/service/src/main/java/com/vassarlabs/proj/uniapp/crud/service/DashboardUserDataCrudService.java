package com.vassarlabs.proj.uniapp.crud.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardUser;
import com.vassarlabs.proj.uniapp.dsp.repository.DashboardUserDataRepository;

@Component
public class DashboardUserDataCrudService {
	
@Autowired DashboardUserDataRepository repository;
	
	public void insertDashboardUser(DashboardUser data) throws CassandraConnectionFailureException, CassandraWriteTimeoutException, 
		CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.save(data);
	}
	
	public void insertDashboardUsers(List<DashboardUser> data) throws CassandraConnectionFailureException, CassandraWriteTimeoutException, 
		CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		repository.saveAll(data);
	}
	
	public DashboardUser findByPartitionKey(String userId)
			throws CassandraConnectionFailureException, CassandraWriteTimeoutException, CassandraInvalidQueryException, 
			CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
		
		return repository.findByPartitionKey(userId);
	}
}
