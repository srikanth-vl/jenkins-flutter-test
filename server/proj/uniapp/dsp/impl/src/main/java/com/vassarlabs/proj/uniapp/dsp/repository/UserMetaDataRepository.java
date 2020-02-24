package com.vassarlabs.proj.uniapp.dsp.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;


@Repository
public interface UserMetaDataRepository 
	extends CrudRepository<UserDBMetaData, String> {
	

	@Query("SELECT * from user_meta_data"
			+ " WHERE super_app_id = ?0")
	List<UserDBMetaData> findByPartitionKey(UUID superAppId);
	
	@Query("SELECT * from user_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id = ?1")
	UserDBMetaData findByPrimaryKey(UUID superAppId, String userId);

	@Query("UPDATE user_meta_data"
			+ " SET password = ?3"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id = ?1"
			+ " AND user_ext_id = ?2")
	void updatePassword(UUID superAppId, String userId, String userExtId, String newPassword);
	
	@Query("DELETE FROM user_meta_data"
			+ " WHERE super_app_id = ?0" )
	void deleteBySuperAppId(UUID superAppId);
	
	@Query("SELECT * from external_to_internal_user_mapping"
			+ " WHERE super_app_id = ?0"
			+ " and user_ext_id = ?1")
	UserDBMetaData getExternalToInternalMappingFromMV(UUID superAppId, String userExtId);

	@Query("DELETE from external_to_internal_user_mapping"
			+ " WHERE super_app_id = ?0")
	void deleteMVRecords(UUID superAppId);

	@Query("SELECT * from user_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id = ?1")
	List<UserDBMetaData> findByUserIdKey(UUID superAppId, String userId);
	
	@Query("SELECT super_app_id, user_id, user_ext_id, user_details, dept_name,"
			+ " mobile_number, insert_ts, is_active from user_meta_data"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id IN ?1")
	List<UserDBMetaData> getMetaDataForListOfUsers (UUID superAppId, List<String> userIds);
	
	@Query("UPDATE user_meta_data"
			+ " SET otp_object = ?3"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id = ?1"
			+ " AND user_ext_id = ?2")
	void updateOtp(UUID superAppId, String userId, String userExtId, String newOtpObject);
	
	@Query("UPDATE user_meta_data"
			+ " SET map_file_urls = ?3"
			+ " WHERE super_app_id = ?0"
			+ " AND user_id = ?1"
			+ " AND user_ext_id = ?2")
	void updateMapFiles(UUID superAppId, String userId, String userExtId, Map<UUID, String> mapfiles);
	
}

