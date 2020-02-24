package com.vassarlabs.proj.uniapp.app.api;

import java.io.IOException;

import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;

import com.vassarlabs.common.utils.err.AuthenticationException;
import com.vassarlabs.common.utils.err.UserNotFoundException;
import com.vassarlabs.proj.uniapp.api.pojo.LoginRequestDetails;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface ILoginValidationService {
	
	/**
	 * For a superapp and user Id, check if that user exists, if yes, it checks if the password matches with what is there in database
	 * @param loginRequestDetails
	 * @return
	 * @throws IOException
	 * @throws UserNotFoundException
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraInternalException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraTypeMismatchException
	 * @throws AuthenticationException
	 */
	ServiceOutputObject validateUser(LoginRequestDetails loginRequestDetails)
			throws IOException, UserNotFoundException, CassandraConnectionFailureException,
			CassandraReadTimeoutException, CassandraInvalidQueryException, CassandraInternalException,
			CassandraQuerySyntaxException, CassandraTypeMismatchException, AuthenticationException;

}
