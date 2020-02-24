package com.vassarlabs.proj.uniapp.dsp.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardUser;

@Repository
public interface DashboardUserDataRepository 
		extends CrudRepository<DashboardUser, String> {
	
	@Query("SELECT * from dashboard_users"
			+ " WHERE user_id = ?0")
	DashboardUser findByPartitionKey(String userId);

}
