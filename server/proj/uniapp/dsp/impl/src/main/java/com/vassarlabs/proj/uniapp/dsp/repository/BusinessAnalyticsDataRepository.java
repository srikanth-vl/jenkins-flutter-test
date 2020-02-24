package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.BusinessAnalyticsData;

@Repository
public interface BusinessAnalyticsDataRepository 
	extends CrudRepository<BusinessAnalyticsData, String> {
	
	@Query("SELECT * from business_analytics_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND parent_entity = ?2")
	List<BusinessAnalyticsData> findByPartitionKey(UUID superApp, UUID applicationId, String parentEntity);
	
	
	@Query("SELECT * from business_analytics_data"
			+ " WHERE super_app_id = ?0"
			+ " AND app_id = ?1"
			+ " AND parent_entity = ?2"
			+ " AND child_entity = ?3")
	BusinessAnalyticsData findByPrimaryKey(UUID superApp, UUID applicationId, String parentEntity, String childEntity);

	
}
