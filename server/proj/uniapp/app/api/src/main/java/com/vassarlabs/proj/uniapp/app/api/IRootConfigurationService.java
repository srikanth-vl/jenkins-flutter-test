package com.vassarlabs.proj.uniapp.app.api;

import java.io.IOException;

import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;
import org.springframework.data.cassandra.CassandraWriteTimeoutException;

import com.vassarlabs.common.utils.err.AppMetaDataNotFoundException;
import com.vassarlabs.common.utils.err.TokenExpiredException;
import com.vassarlabs.common.utils.err.TokenNotFoundException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.ApiRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface IRootConfigurationService {

	/**
	 * Returns flat hierarchy for all apps' meta data along with their parent IDs
	 * @param apiRequestObject
	 * @return
	 * @throws IOException
	 * @throws TokenNotFoundException
	 * @throws TokenExpiredException
	 * @throws AppMetaDataNotFoundException
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraWriteTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraInternalException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraTypeMismatchException
	 * @throws UserNotFoundException
	 */
	public ServiceOutputObject getRootConfigData(ApiRequestObject apiRequestObject)
			throws IOException, TokenNotFoundException, TokenExpiredException, AppMetaDataNotFoundException,
			CassandraConnectionFailureException, CassandraReadTimeoutException, CassandraWriteTimeoutException,
			CassandraInvalidQueryException, CassandraInternalException, CassandraQuerySyntaxException,
			CassandraTypeMismatchException, UserNotFoundException;

}
