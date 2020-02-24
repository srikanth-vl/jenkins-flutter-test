package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;

@Table(value = "app_form")
@Data
public class ApplicationFormData {
	
	 @PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	 UUID superAppId;

	 @PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	 UUID applicationId;

	 @PrimaryKeyColumn(name = "project_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
	 UUID projectId;
	 
	 @PrimaryKeyColumn(name = "form_type", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	 int formType;
	 
	 @PrimaryKeyColumn(name = "form_version_number", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	 int formVersionNumber;
	 
	 @Column(value = "md_instance_id")
	 String metaDataInstanceId;
	 
	 @Column(value = "form_instance_id")
	 String formInstanceId;
	 
	 @Column(value = "is_active")
	 int activeFlag;
	 
	 @Column(value = "form_json")
	 String formJson;
	 
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
		 ApplicationFormData other = (ApplicationFormData) obj;
		 if(superAppId != other.superAppId) 
			 return false;
		 if (applicationId != other.applicationId)
			 return false;
		 if (projectId != other.projectId)
			 return false;
		 if (formType != other.formType)
			 return false;
		 if (formVersionNumber != other.formVersionNumber)
			 return false;
		 return true;
	 }

	 @Override
	 public int hashCode() {
		 final int prime = 31;
		 int result = 1;
		 result = prime * result + superAppId.hashCode();
		 result = prime * result + applicationId.hashCode();
		 result = prime * result + projectId.hashCode();
		 result = prime * result + formType;
		 result = prime * result + formVersionNumber;
		 return result;
	 }
}
