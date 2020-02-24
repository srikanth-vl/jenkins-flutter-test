package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "image_geotag_data")
@Data
public class ImageGeotagData {
	
	@PrimaryKeyColumn(value = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    UUID superAppId;
	
	@PrimaryKeyColumn(value = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    UUID appId;

	@PrimaryKeyColumn(value = "project_id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    UUID projectId;

	@PrimaryKeyColumn(value = "field_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    UUID fieldId;

	@Column(value = "latitude")
	double latitude;
	
	@Column(value = "longitude")
	double longitude;
	
	@Column(value = "media_type")
	String mediaType;
	
	@Column(value = "image_data")
	ByteBuffer imageData;
	
	@Column(value = "media_path")
	String mediaPath;
			 
	@Column(value = "sync_ts")
	long syncTs;
	
	@Column(value = "gps_accuracy")
	String gpsAccuracy;
	
	@Column(value = "timestamp_overlay")
	String timestampOverlay;
	
	@Column(value = "media_md_data")
	String mediaMetaData;
	
	@Column(value = "insert_ts")
	long insertTs;
	
	@Column(value = "additional_prop")
	String additionalProperties;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageGeotagData other = (ImageGeotagData) obj;
		if (superAppId != other.superAppId)
			return false;
		if (appId != other.appId)
			return false;
		if (projectId != other.projectId)
			return false;
		if (fieldId != other.fieldId)
			return false;
		return true;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + superAppId.hashCode();
		result = prime * result + appId.hashCode();
		result = prime * result + projectId.hashCode();
		result = prime * result + fieldId.hashCode();
		return (int) result;
	}
}
