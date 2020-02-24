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
import com.vassarlabs.proj.uniapp.api.pojo.EntityMetadataConfigRequestObject;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface IEntityMetadataConfigurationService {

	/**
	 * Returns flat hierarchy for all entity meta data along with their parent
	 * @param entityMetadataConfigRequestObject
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
	public ServiceOutputObject getEntityConfigData(EntityMetadataConfigRequestObject requestObject)
			throws IOException, TokenNotFoundException, TokenExpiredException;

}
