package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "dashboard_token_data")
@Data
public class DashboardTokenData {
	
	@PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	String userId;
	
	@PrimaryKeyColumn(name = "token_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	UUID token;

	@Column(value = "token_expired")
	int tokenExpired;

	@Column(value = "insert_ts")
	long insertTs;

	@Column(value = "sync_ts")
	long syncTs;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DashboardTokenData other = (DashboardTokenData) obj;
		if (userId != other.userId)
			return false;
		if (token != other.token)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =  (int) (prime * result + userId.hashCode());
		result =  prime * result + token.hashCode();
		return result;
	}

}
