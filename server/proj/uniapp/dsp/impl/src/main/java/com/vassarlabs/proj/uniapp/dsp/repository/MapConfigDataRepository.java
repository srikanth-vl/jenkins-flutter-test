package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapConfigData;

@Repository
public interface MapConfigDataRepository 
	extends CrudRepository<MapConfigData, String> {
	
	@Query("SELECT * from map_config_data"
			+ " WHERE super_app_id = ?0")
	List<MapConfigData> findByPartitionKey(UUID superAppId);

	@Query("SELECT * from map_config_data"
			+ " WHERE super_app_id = ?0")
	List<MapConfigData> findAppsUnderSuperApp(UUID superApp);

	@Query("SELECT * from map_config_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND version =?2"
			+ " LIMIT 1")
	MapConfigData findByPrimaryKey(UUID superAppId, UUID appId, Integer version);
	
	@Query("SELECT * from map_config_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " LIMIT 1")
	MapConfigData findLatestVersion(UUID superAppId, UUID appId);
	
	@Query("SELECT super_app_id, app_id, max(version) as version, markers_info, config_data, insert_ts"
			+ " from map_config_data"
			+ " WHERE super_app_id = ?0"
			+ " GROUP BY"
			+ " super_app_id, app_id")
	List<MapConfigData> findLatestAppMapConfig(UUID superAppId);
}
