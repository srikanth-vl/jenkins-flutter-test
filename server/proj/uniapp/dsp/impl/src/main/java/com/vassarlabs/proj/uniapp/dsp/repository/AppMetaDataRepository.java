package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;

@Repository
public interface AppMetaDataRepository 
	extends CrudRepository<ApplicationMetaData, String> {
	
	@Query("SELECT * from app_meta_data"
			+ " WHERE super_app_id = ?0")
	List<ApplicationMetaData> findByPartitionKey(UUID superAppId);

	@Query("SELECT * from app_meta_data"
			+ " WHERE super_app_id = ?0")
	List<ApplicationMetaData> findAppsUnderSuperApp(UUID superApp);

	@Query("SELECT * from app_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " LIMIT 1")
	ApplicationMetaData findLatestByKey(UUID superAppId, UUID appId);
	
	@Query("SELECT super_app_id, app_id, max(version) as version, parent_app_id, config_data, insert_ts"
			+ " from app_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " GROUP BY"
			+ " super_app_id, app_id")
	List<ApplicationMetaData> findLatestApps(UUID superAppId);
}
