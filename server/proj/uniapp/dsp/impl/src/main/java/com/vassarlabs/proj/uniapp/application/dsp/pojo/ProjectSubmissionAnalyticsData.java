package com.vassarlabs.proj.uniapp.application.dsp.pojo;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.Data;
import lombok.ToString;

@Table(value = "project_submission_analytics")
@Data
@ToString
public class ProjectSubmissionAnalyticsData {
	
	/*
	 * ((super_app_id, app_id, parent_entity_name), date, entity_name), 
	 * no_of_succsessful_submissions, no_of_failed_submissions, project_list_submitted, 
	 * project_list_failed, user_list
	 */
	
	@PrimaryKeyColumn(name = "super_app_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	UUID superAppId;
	
	@PrimaryKeyColumn(name = "app_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
	UUID applicationId;

	@PrimaryKeyColumn(name = "parent_entity", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
	String parentEntity;

	@PrimaryKeyColumn(name = "date", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
	int date;
	
	@PrimaryKeyColumn(name = "entity", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
	String entity;
	
	@Column(value = "no_of_successful_submissions")
	Integer noOfSuccessfulSubmissions;
	
	@Column(value ="no_of_failed_submissions")
	Integer noOfFailedSubmissions;
	
	@Column(value = "successful_submission_project_ids")
	Set<UUID> successfulSubmissionProjectIds;
	
	@Column(value = "failed_submission_project_ids")
	Set<UUID> failedSubmissionProjectIds;
	
	@Column(value = "user_ids")
	Set<String> userIds;
	
	@Column(value = "insert_ts")
	Long insertTs;
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectSubmissionAnalyticsData other = (ProjectSubmissionAnalyticsData) obj;
		if (!superAppId.equals(other.superAppId))
			return false;
		if (!applicationId.equals(applicationId))
			return false;
		if (!parentEntity.equals(other.parentEntity))
			return false;
		if (date != other.date)
			return false;
		if (!entity.equals(other.entity))
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
		result = prime * result + date;
		result = prime * result + entity.hashCode();

		return (int) result;
	}

}
