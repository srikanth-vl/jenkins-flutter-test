package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTokenData;

@Repository
public interface UserTokenDataRepository 
	extends CrudRepository<UserTokenData, String> {

	@Query("SELECT * from user_token_data"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id = ?1")
	List<UserTokenData> findByPartitionKey(UUID superAppId, String userId);

	@Query("SELECT * from user_token_data"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id = ?1"
			+ " AND token_id = ?2")
	UserTokenData findByPrimaryKey(UUID superAppId, String userId, UUID tokenId);

	@Query("UPDATE user_token_data SET token_expired = ?0"
			+ " WHERE super_app_id = ?1"
			+ " AND user_id = ?2"
			+ " AND token_id = ?3")
	void updateTokenExpiry(int tokenExpired, UUID superAppId, String userId, UUID tokenId);

	@Query("UPDATE user_token_data SET sync_ts = ?0"
			+ " WHERE super_app_id = ?1"
			+ " AND user_id = ?2"
			+ " AND token_id = ?3")
	void updateSyncTime(long syncTS, UUID superAppId, String userId, UUID tokenId);
	
	@Query("SELECT * from user_token_data "
			+ " where super_app_id = ?0"
			+ " AND user_id IN ?1 ")
	List<UserTokenData> findDistinctUsersLogIn(UUID superAppId, List<String> userIds);
}
