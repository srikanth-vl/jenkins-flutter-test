package com.vassarlabs.proj.uniapp.crud.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;
import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapFileData;
import com.vassarlabs.proj.uniapp.dsp.repository.MapFileDataRepository;

@Component
public class MapFileDataCrudService {
	
	@Autowired MapFileDataRepository repository;
	
	public void insertMapData(MapFileData data) throws CassandraConnectionFailureException, 
	CassandraWriteTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	repository.save(data);
	}
	
	public MapFileData findDataByPartitionKey(String mapFileName) throws CassandraConnectionFailureException, 
    CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException, CassandraTypeMismatchException {
    	return repository.findByPartitionKey(mapFileName);
    }

}
