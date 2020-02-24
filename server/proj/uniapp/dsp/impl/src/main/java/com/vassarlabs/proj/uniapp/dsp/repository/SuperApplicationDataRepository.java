package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;


@Repository
public interface SuperApplicationDataRepository
	extends CrudRepository<SuperApplicationData, String> {
	
	@Query("SELECT * from superapp_metadata"
			+ " WHERE super_app_id = ?0"
			+ " AND version = ?1")
	SuperApplicationData findByPrimaryKey(UUID superAppId, int versionNumber);
	
	@Query("SELECT * from superapp_metadata"
			+ " WHERE super_app_id = ?0")
	List<SuperApplicationData> findByPartitionKey(UUID superAppId);

	
	@Query("SELECT * from superapp_metadata"
			+ " WHERE super_app_id = ?0"
			+ " LIMIT 1")
	SuperApplicationData findLatestVersion(UUID superAppId);
	
	@Query("SELECT super_app_id, max(version) as version, name, config_file, aws_bucket_name,"
			+ " package_name, insert_ts"
			+ " from superapp_metadata"
			+ " GROUP BY"
			+ " super_app_id")
	List<SuperApplicationData> findLatestSuperapp();
}
