package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "user_token_data")
@Data
public class UserTokenData {

	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;

	@PrimaryKeyColumn(name = "user_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	String userId;
	
	@PrimaryKeyColumn(name = "token_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	UUID tokenId;

	@Column(value = "token_expired")
	Integer tokenExpired;

	@Column(value = "insert_ts")
	Long insertTs;

	@Column(value = "sync_ts")
	Long syncTs;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserTokenData other = (UserTokenData) obj;
		if (superAppId != other.superAppId)
			return false;
		if (userId != other.userId)
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
		result =  (int) (prime * result + userId.hashCode());
		result =  prime * result + tokenId.hashCode();
		return result;
	}
}
