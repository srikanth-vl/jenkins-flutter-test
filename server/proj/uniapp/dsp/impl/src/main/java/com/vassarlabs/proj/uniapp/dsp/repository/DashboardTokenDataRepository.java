package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardTokenData;

@Repository
public interface DashboardTokenDataRepository 
	extends CrudRepository<DashboardTokenData, String> {
	
	@Query("SELECT * from dashboard_token_data"
			+ " WHERE user_id = ?0")
	List<DashboardTokenData> findByPartitionKey(String userId);
	
	@Query("SELECT * from dashboard_token_data"
			+ " WHERE user_id = ?0"
			+ " AND token_id = ?1")
	DashboardTokenData findByPrimaryKey(String userId, UUID tokenId);

	@Query("UPDATE dashboard_token_data SET token_expired = ?0"
			+ " WHERE user_id = ?1"
			+ " AND token_id = ?2")
	void updateTokenExpiry(int tokenExpired, String userId, UUID tokenId);

	@Query("UPDATE dashboard_token_data SET sync_ts = ?0"
			+ " WHERE user_id = ?1"
			+ " AND token_id = ?2")
	void updateSyncTime(long syncTS, String userId, UUID tokenId);

}
