package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.ImageSubmittedAndReceivedData;

public interface ImageSubmittedAndReceivedDataRepository  extends 
		CrudRepository<ImageSubmittedAndReceivedData, String>{

	@Query("SELECT * from image_submitted_and_received_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1")
	List<ImageSubmittedAndReceivedData> findByPartitionKey( UUID superAppId, UUID applicationId );

	@Query("SELECT * from image_submitted_and_received_data"
			+ " WHERE super_app_id = ?0"
			+ " and app_id = ?1"
			+ " and project_id = ?2"
			+ " and image_id = ?3")
	ImageSubmittedAndReceivedData findByPrimaryKey( UUID superAppId, UUID applicationId, UUID projectId, UUID imageId );
	
	@Query("UPDATE image_submitted_and_received_data SET submit_status = ?0, last_updated_ts = ?1"
			+ " WHERE super_app_id = ?2"
			+ " AND app_id = ?3"
			+ " AND project_id = ?4"
			+ " AND image_id = ?5")
	void updateSubmittedImageData( Boolean submitStatus, long lastUpdatedTs, UUID superAppId, UUID applicationId, UUID projectId, UUID imageId );
	
	@Query("UPDATE image_submitted_and_received_data SET submit_status = ?0, last_updated_ts = ?1 , submission_ts = ?6"
			+ " WHERE super_app_id = ?2"
			+ " AND app_id = ?3"
			+ " AND project_id = ?4"
			+ " AND image_id = ?5")
	void updateSubmittedImageData( Boolean submitStatus, long lastUpdatedTs, UUID superAppId, UUID applicationId, UUID projectId, UUID imageId ,long timestamp);
	
	
	@Query("UPDATE image_submitted_and_received_data SET receive_status = ?0, last_updated_ts = ?1"
			+ " WHERE super_app_id = ?2"
			+ " AND app_id = ?3"
			+ " AND project_id = ?4"
			+ " AND image_id = ?5")
	void updateReceivedImageData( Boolean receiveStatus, long lastUpdateTs, UUID superAppId, UUID applicationId, UUID projectId,  UUID imageId );
	
	@Query("UPDATE image_submitted_and_received_data SET receive_status = ?0, last_updated_ts = ?1 , submission_ts = ?6"
			+ " WHERE super_app_id = ?2"
			+ " AND app_id = ?3"
			+ " AND project_id = ?4"
			+ " AND image_id = ?5")
	void updateReceivedImageData( Boolean receiveStatus, long lastUpdateTs, UUID superAppId, UUID applicationId, UUID projectId,  UUID imageId , long insTimestamp);

	
	@Query("UPDATE image_submitted_and_received_data SET relay_status = ?0, last_updated_ts = ?1"
			+ " WHERE super_app_id = ?2"
			+ " AND app_id = ?3"
			+ " AND project_id = ?4"
			+ " AND image_id = ?5")
	void updateRelayStatus( Integer relayStatus, long lastUpdateTs, UUID superAppId, UUID applicationId, UUID projectId,  UUID imageId );
}


