package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageGeotagData;

@Repository
public interface ImageGeotagDataRepository extends 
		CrudRepository<ImageGeotagData, String>{

	@Query("SELECT * from image_geotag_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2"
			+ " AND field_id = ?3")
	ImageGeotagData findByPrimaryKey(UUID superAppId, UUID appId, UUID projectId, UUID fieldId);
	
	
	@Query("SELECT * from image_geotag_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND project_id = ?2")
	List<ImageGeotagData> findByPartitionKey(UUID superAppId, UUID appId, UUID projectId);
}
