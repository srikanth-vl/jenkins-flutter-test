package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(value = "user_meta_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDBMetaData {
	
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	 UUID superAppId;
	
	 @PrimaryKeyColumn(name = "user_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	 String userId;
	
	 @PrimaryKeyColumn(name = "user_ext_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	 String userExtId;

	 @Column(value = "user_details")
	 String userDetails;
	 
	 @Column(value = "dept_name")
	 String departmentName;
	 
	 @Column(value = "password")
	 String password;
	 
	 @Column(value = "otp_object")
	 String otpObject;
	 
	 @Column(value = "app_actions")
	 Map<UUID, String> appActions;
	 
	 @Column(value = "map_file_urls")
	 Map<UUID, String> mapFileUrls;
	 
	 @Column(value = "insert_ts")
	 long insertTs;
	 
	 @Column(value = "mobile_number")
	 long mobileNumber;
	 
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
			if (superAppId != other.superAppId)
				return false;
			if(userExtId != other.userExtId)
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result =  (int) (prime * result + userId.hashCode());
			result =  (int) (prime * result + superAppId.hashCode());
			result =  (int) (prime * result + userExtId.hashCode());
			return result;
		}
}
