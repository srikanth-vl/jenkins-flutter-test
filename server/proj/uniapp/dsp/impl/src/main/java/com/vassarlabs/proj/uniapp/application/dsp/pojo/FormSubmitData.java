package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "form_submission_data")
@Data
public class FormSubmitData {
	
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID applicationId;
	
	@PrimaryKeyColumn(name = "project_id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
	UUID projectId;
	
	@PrimaryKeyColumn(name = "key", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	String key;
	
	@PrimaryKeyColumn(name = "date", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	int date;
	
	@PrimaryKeyColumn(name = "user_type", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
	int userType;
	
	@PrimaryKeyColumn(name = "timestamp", ordinal = 6, type = PrimaryKeyType.CLUSTERED)
	long timestamp;
	
	@PrimaryKeyColumn(name = "user_id", ordinal = 7, type = PrimaryKeyType.CLUSTERED)
	String userId;
	
	@Column(value = "form_instance_id")
	String formInstanceId;
	
	@Column(value = "md_instance_id")
	String metaDataInstanceId;
	
	@Column(value = "token_id")
	UUID tokenId;
	
	@Column(value = "datatype")
	String dataType;
	
	@Column(value = "value")
	String value;

	@Column(value = "uom")
	String uom;

	@Column(value = "db_insert_ts")
	long dbInsertTs;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormSubmitData other = (FormSubmitData) obj;
		if (!superAppId.equals(other.superAppId))
			return false;
		if (!applicationId.equals(applicationId))
			return false;
		if (!projectId.equals(other.projectId))
			return false;
		if (!key.equals(other.key))
			return false;
		if (userType != other.userType)
			return false;
		if (date != other.date)
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + superAppId.hashCode();
		result = prime * result + applicationId.hashCode();
		result = prime * result + projectId.hashCode();	
		result = prime * result + userType;
		result = prime * result + date;
		result = prime * result + timestamp;
		result = prime * result + key.hashCode();
		result = prime * result + userId.hashCode();

		return (int) result;
	}
}
