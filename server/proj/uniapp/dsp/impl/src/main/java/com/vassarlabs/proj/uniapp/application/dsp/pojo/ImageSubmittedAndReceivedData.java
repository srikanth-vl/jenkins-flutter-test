package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "image_submitted_and_received_data")
@Data
public class ImageSubmittedAndReceivedData {
	
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID applicationId;
	
	@PrimaryKeyColumn(name = "project_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	UUID projectId;

	@PrimaryKeyColumn(name = "image_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	UUID imageId;
	
	@Column(value = "submit_status")
	Boolean submitStatus;
	
	@Column(value = "receive_status")
	Boolean receiveStatus;
	
	@Column(value = "relay_status")
	Integer relayStatus;
	
	@Column(value = "last_updated_ts")
	Long lastUpdatedTs;
	
	@Column(value = "submission_ts")
	Long submissionTs;	
		
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageSubmittedAndReceivedData other = (ImageSubmittedAndReceivedData) obj;
		if (!superAppId.equals(other.superAppId))
			return false;
		if (!applicationId.equals(applicationId))
			return false;
		if (!projectId.equals(other.projectId))
			return false;
		if (!imageId.equals(other.imageId))
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
		result = prime * result + imageId.hashCode();	
		return (int) result;
	}
}
