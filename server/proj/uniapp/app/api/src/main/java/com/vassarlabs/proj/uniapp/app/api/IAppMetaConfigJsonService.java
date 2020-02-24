package com.vassarlabs.proj.uniapp.app.api;

import java.io.IOException;
import java.util.Map;

import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;

import com.vassarlabs.common.utils.err.DataNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.AppRequestObject;

public interface IAppMetaConfigJsonService {

	/**
	 * If app provides specific version for which it wants information, it returns that config
	 * @param appRequestObject
	 * @return
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraTypeMismatchException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraInternalException
	 * @throws IOException
	 * @throws DataNotFoundException
	 */
	Map<String, Object> getLatestAppMetaConfigJson(AppRequestObject appRequestObject)
			throws CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraReadTimeoutException,
			CassandraInvalidQueryException, CassandraQuerySyntaxException, CassandraInternalException, IOException,
			DataNotFoundException;
	
	/**
	 * Returns latest version super app config else returns empty map
	 * @param appRequestObject
	 * @return
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraTypeMismatchException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraInternalException
	 * @throws IOException
	 * @throws DataNotFoundException
	 */
	Map<String, Object> getAppMetaConfigJson(AppRequestObject appRequestObject)
			throws CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraReadTimeoutException,
			CassandraInvalidQueryException, CassandraQuerySyntaxException, CassandraInternalException, IOException,
			DataNotFoundException;
	
	

}
