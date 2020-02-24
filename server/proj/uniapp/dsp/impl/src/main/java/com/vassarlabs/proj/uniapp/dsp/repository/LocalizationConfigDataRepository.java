package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.LocalizationConfigData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.MapConfigData;

@Repository
public interface LocalizationConfigDataRepository 
	extends CrudRepository<LocalizationConfigData, String> {
	
	@Query("SELECT * from localization_config_data"
			+ " WHERE super_app_id = ?0"
			+ " AND version = ?1")
	LocalizationConfigData findByPrimaryKey(UUID superAppId, int versionNumber);
	
	@Query("SELECT * from localization_config_data"
			+ " WHERE super_app_id = ?0")
	List<LocalizationConfigData> findByPartitionKey(UUID superAppId);

	
	@Query("SELECT * from localization_config_data"
			+ " WHERE super_app_id = ?0"
			+ " LIMIT 1")
	LocalizationConfigData findLatestVersion(UUID superAppId);
	
	@Query("SELECT super_app_id, max(version) as version, config_data, insert_ts"
			+ " from localization_config_data"
			+ " GROUP BY"
			+ " super_app_id")
	List<LocalizationConfigData> findLatestLocalizationConfig();
}
