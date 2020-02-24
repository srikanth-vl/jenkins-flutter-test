package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "user_tracking_table")
@Data
public class UserTrackingData {

	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID appId;

	@PrimaryKeyColumn(name = "user_id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
	String userId;
	
	@PrimaryKeyColumn(name = "token_id", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
	UUID tokenId;
	
	@PrimaryKeyColumn(name = "timestamp", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	long timeStamp;

	@PrimaryKeyColumn(name = "api_type", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	String apiType;

	@Column(value = "api")
	String api;
	
	@Column(value = "request_object")
	String requestObj;

	@Column(value = "request_success")
	boolean isRequestSuccessful;
	
	@Column(value = "errors")
	List<String> errors;
	
	@Column(value = "insert_ts")
	long insertTs;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserTrackingData other = (UserTrackingData) obj;
		if (superAppId != other.superAppId)
			return false;
		if (appId != other.appId)
			return false;
		if (userId != other.userId)
			return false;
		if (timeStamp != other.timeStamp)
			return false;
		if (apiType != other.apiType)
			return false;
		if (tokenId != other.tokenId)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =  prime * result + superAppId.hashCode();
		result =  prime * result + appId.hashCode();
		result =  (int) (prime * result + userId.hashCode());
		result =  prime * result + tokenId.hashCode();
		result =  (int) (prime * result + timeStamp);
		result =  (int) (prime * result + apiType.hashCode());
		return result;
	}
}
