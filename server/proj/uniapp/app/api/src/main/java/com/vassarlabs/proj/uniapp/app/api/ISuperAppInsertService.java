package com.vassarlabs.proj.uniapp.app.api;

import java.util.Map;

import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.data.cassandra.CassandraInternalException;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.data.cassandra.CassandraQuerySyntaxException;
import org.springframework.data.cassandra.CassandraReadTimeoutException;
import org.springframework.data.cassandra.CassandraTypeMismatchException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;

public interface ISuperAppInsertService {
	
	/**
	 * Modifies an existing Super App Properties : Increments the version and inserts into {@link SuperApplicationData}
	 * @param superAppIdStr
	 * @param superApp
	 * @param title
	 * @param subTitle
	 * @param userNameType
	 * @param syncVisible
	 * @param syncInterval
	 * @param gridColumns
	 * @param colorAccent
	 * @param colorPrimary
	 * @param colorPrimaryDark
	 * @param splashIcon
	 * @param splashDuration
	 * @param splashBackground
	 * @param loginIcon
	 * @param serviceFrequencyOnApp
	 * @param formSubmissionUploadRetries
	 * @param mediaUplaodRetries
	 * @param awsBucketConfiguration
	 * @param currentPlaystoreAppVersion
	 * @param acceptDataFromOlderApp
	 * @return
	 * @throws JsonProcessingException
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraTypeMismatchException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraInternalException
	 */
	Map<String, Object> updateSuperAppConfigService(String superAppIdStr, String superApp, String title,
			String subTitle, String userNameType, boolean syncVisible, String syncInterval, String gridColumns,
			String colorAccent, String colorPrimary, String colorPrimaryDark, String splashIcon, String splashDuration,
			String splashBackground, String loginIcon, JsonNode serviceFrequencyOnAppp, Integer formSubmissionUploadRetries
			, Integer mediaUplaodRetries, String awsBucketConfiguration, String currentPlaystoreAppVersion, boolean acceptDataFromOlderApp) throws JsonProcessingException,
			CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraReadTimeoutException,
			CassandraInvalidQueryException, CassandraQuerySyntaxException, CassandraInternalException;
	
	
	/**
	 * Inserts super app data :: generates a UUID for the super app and inserts into DB with version = 1
	 * @param superApp
	 * @param title
	 * @param subTitle
	 * @param userNameType
	 * @param syncVisible
	 * @param syncInterval
	 * @param gridColumns
	 * @param colorAccent
	 * @param colorPrimary
	 * @param colorPrimaryDark
	 * @param splashIcon
	 * @param splashDuration
	 * @param splashBackground
	 * @param loginIcon
	 * @param serviceFrequencyOnApp
	 * @param formSubmissionUploadRetries
	 * @param mediaUplaodRetries
	 * @param awsBucketConfiguration
	 * @param currentPlaystoreAppVersion
	 * @param acceptDataFromOlderApp
	 * @return
	 * @throws JsonProcessingException
	 * @throws CassandraConnectionFailureException
	 * @throws CassandraTypeMismatchException
	 * @throws CassandraReadTimeoutException
	 * @throws CassandraInvalidQueryException
	 * @throws CassandraQuerySyntaxException
	 * @throws CassandraInternalException
	 */
	Map<String, Object> insertSuperAppConfigService(String superApp, String title, String subTitle, String userNameType,
			boolean syncVisible, String syncInterval, String gridColumns, String colorAccent, String colorPrimary,
			String colorPrimaryDark, String splashIcon, String splashDuration, String splashBackground,
			String loginIcon, JsonNode serviceFrequencyOnApp, Integer formSubmissionUploadRetries, 
			Integer mediaUplaodRetries, String awsBucketConfiguration, String currentPlaystoreAppVersion, boolean acceptDataFromOlderApp) throws JsonProcessingException,
			CassandraConnectionFailureException, CassandraTypeMismatchException, CassandraReadTimeoutException,
			CassandraInvalidQueryException, CassandraQuerySyntaxException, CassandraInternalException;

}
