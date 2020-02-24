package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(value = "dashboard_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardUser {
	
	 @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	 String userId;
	
	 @Column(value = "password")
	 String password;
	 
	 @Column(value = "super_app_ids")
	 List<UUID> superAppIds;

	 @Column(value = "user_details")
	 String userDetails;
	 
	 @Column(value = "hierarchy_access")
	 String hierarchyAccess;
	 
	 @Column(value = "apps_assigned")
	 String appsAssigned;
	 
	 @Column(value = "insert_ts")
	 long insertTs;
	 	 
	 @Column(value = "is_active")
	 boolean isActive;
	 
	 @Override
	 public boolean equals(Object obj) {
		 if (this == obj)
			 return true;
		 if (obj == null)
			 return false;
		 if (getClass() != obj.getClass())
			 return false;
		 UserDBMetaData other = (UserDBMetaData) obj;
		 if (userId != other.userId)
			 return false;
		 return true;
	 }

	 @Override
	 public int hashCode() {
		 final int prime = 31;
		 int result = 1;
		 result =  (int) (prime * result + userId.hashCode());
		 return result;
	 }
}
