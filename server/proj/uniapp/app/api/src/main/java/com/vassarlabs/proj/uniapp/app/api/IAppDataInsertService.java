package com.vassarlabs.proj.uniapp.app.api;

import java.util.List;
import java.util.Map;

import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.proj.uniapp.api.pojo.AppJsonData;

public interface IAppDataInsertService {
	
	/**
	 * Inserts app level configurations into app_meta_data table
	 * @param superAppId
	 * @param noOfApps
	 * @param appJsonData
	 * @return
	 * @throws JsonProcessingException
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraTypeMismatchException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraInternalException
	 */
	Map<String, Object> insertAppDataConfigService(String superAppId, int noOfApps, List<AppJsonData> appJsonData)
			throws JsonProcessingException, CassandraConnectionFailureException, CassandraTypeMismatchException,
			CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraQuerySyntaxException,
			CassandraInternalException;

}
