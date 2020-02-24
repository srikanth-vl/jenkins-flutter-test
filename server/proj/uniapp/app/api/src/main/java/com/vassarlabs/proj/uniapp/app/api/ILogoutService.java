package com.vassarlabs.proj.uniapp.app.api;

import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.ApiRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface ILogoutService {
	
	/**
	 * Expires the token of the user
	 * @param requestObject
	 * @return
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraWriteTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraInternalException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraTypeMismatchException
	 * @throws TokenNotFoundException
	 * @throws TokenExpiredException
	 * @throws JsonProcessingException
	 */
	public ServiceOutputObject logout(ApiRequestObject requestObject)
			throws CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraWriteTimeoutException,
			CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException,
			CassandraTypeMismatchException, TokenNotFoundException, TokenExpiredException, JsonProcessingException;

}
