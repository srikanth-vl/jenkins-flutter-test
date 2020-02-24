package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTrackingData;

@Repository
public interface ProjectSubmissionAnalytic 
	extends CrudRepository<UserTrackingData, String> {
	
	@Query("SELECT * from user_tracking_table"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id = ?2")
	List<UserTrackingData> findByPartitionKey(UUID superAppId, UUID appId, String userId);
	

	@Query("SELECT * from user_tracking_table"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id = ?2"
			+ " AND timestamp = ?3"
			+ " AND api_type = ?4"
			+ " AND token_id = ?5")
	UserTrackingData findByPrimaryKey(UUID superAppId, UUID appId, String userId, long timeStamp, String apiType, UUID tokenId);
	
	@Query("SELECT * from user_tracking_table"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id = ?2"
			+ " AND api_type = ?3"
			+ " AND timestamp < ?5"
			+ " limit ?4")
	List<UserTrackingData> findTransactionLogforUserByApiType(UUID superAppId, UUID appId, String userId, String apiType, int size, long timestamp);
	
	@Query("SELECT timestamp from user_tracking_table"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id = ?2"
			+ " AND api_type = ?3"
			+ " limit ?4")
	List<Long> findTimestamps(UUID superAppId, UUID appId, String userId, String apiType, int noofrows );
	
	@Query("SELECT count(*) from user_tracking_table"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id = ?2"
			+ " AND api_type = ?3")
	int findCountOfRows(UUID superAppId, UUID appId, String userId, String apiType);
	
	@Query("SELECT * from user_tracking_table"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND user_id IN ?2"
			+ " AND api_type = ?3"
			+ " AND timestamp > ?4")
	List<UserTrackingData> findTransactionLogforUsersByApiType(UUID superAppId, UUID appId, List<String> userId, String apiType, long timestamp);
}
