package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "business_analytics_data")
@Data
public class BusinessAnalyticsData {
	
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID applicationId;

	@PrimaryKeyColumn(name = "parent_entity", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
	String parentEntity;
	
	@PrimaryKeyColumn(name = "child_entity", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	String childEntity;
	
	@Column(value = "computed_values")
	String computedValues;
	
	@Column(value = "project_ids")
	Set<UUID> projectIds;
	
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
		BusinessAnalyticsData other = (BusinessAnalyticsData) obj;
		if (!superAppId.equals(other.superAppId))
			return false;
		if (!applicationId.equals(applicationId))
			return false;
		if (!parentEntity.equals(other.parentEntity))
			return false;
		if (!childEntity.equals(other.childEntity))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + superAppId.hashCode();
		result = prime * result + applicationId.hashCode();
		result = prime * result + parentEntity.hashCode();	
		result = prime * result + childEntity.hashCode();

		return (int) result;
	}

	
	
}
