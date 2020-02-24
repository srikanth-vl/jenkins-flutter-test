package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vassarlabs.proj.uniapp.app.custom.deserialisers.CustomDeserialiser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(value = "field_meta_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FieldMetaData {
	
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID applicationId;
	
	@PrimaryKeyColumn(name = "project_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	UUID projectId;
	
	@PrimaryKeyColumn(name = "form_type", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	int formType;
	
	@PrimaryKeyColumn(name = "md_version", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	int metaDataVersion;
	
	@PrimaryKeyColumn(name = "key_type", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
	int keyType;
	
	@PrimaryKeyColumn(name = "key", ordinal = 6, type = PrimaryKeyType.CLUSTERED)
	String key;
	
	@Column(value = "attributes")
	String attributes;
	
	@Column(value = "md_instance_id")
	String metadataInstanceId;
	
	@Column(value = "datatype")
	String dataType;
	
	@Column(value = "target_field")
	String targetField;
	
	@Column(value = "display_labels")
	String displayNames;
	
	@Column(value = "is_mandatory")
	boolean isMandatory;
	
	@JsonDeserialize(using = CustomDeserialiser.class)
	@Column(value = "validations")
	String validations;
	
	@Column(value = "label_name")
	String labelName;
	
	@Column(value = "default_value")
	String defaultValue;
	
	@Column(value = "uom")
	String uom;
	
	@Column(value = "computation_type")
	String computationType;
	
	@Column(value = "dimension")
	String dimension;
	
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
		FieldMetaData other = (FieldMetaData) obj;
		if (!superAppId.equals(other.superAppId))
			return false;
		if (!applicationId.equals(other.applicationId))
			return false;
		if (metaDataVersion != other.metaDataVersion)
			return false;
		if (!projectId.equals(other.projectId))
			return false;
		if(formType != other.formType) 
			return false;
		if (keyType!=other.keyType)
			return false;
		if (!key.equals(other.key))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + superAppId.hashCode();
		result = prime * result + applicationId.hashCode();
		result = prime * result + metaDataVersion;
		result = prime * result + formType;
		result = prime * result + projectId.hashCode();
		result = prime * result + keyType;
		result = prime * result + key.hashCode();
		
		return (int) result;
	}

	public FieldMetaData(FieldMetaData metaData) {
		this.superAppId = metaData.getSuperAppId();
		this.applicationId = metaData.getApplicationId();
		this.projectId = metaData.getProjectId();
		this.metadataInstanceId = metaData.getMetadataInstanceId();
		this.metaDataVersion = metaData.getMetaDataVersion();
		this.formType = metaData.getFormType();
		this.key = metaData.getKey();
		this.keyType = metaData.keyType;
		this.validations = metaData.validations;
		this.attributes = metaData.attributes;
		this.computationType = metaData.computationType;
		this.dataType = metaData.getDataType();
		this.defaultValue = metaData.getDefaultValue();
		this.dimension = metaData.getDimension();
		this.displayNames = metaData.getDisplayNames();
		this.uom = metaData.getUom();
		this.labelName = metaData.getLabelName();
		this.isMandatory = metaData.isMandatory();
		this.insertTs = metaData.getInsertTs();
		}
}