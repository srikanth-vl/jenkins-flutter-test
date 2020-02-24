package com.vassarlabs.proj.uniapp.dashboard.service.api;

import java.util.UUID;

import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionCountForEntity;

public interface IProjectLastNDayData {

	/**
	 * Get all projects for super app id & app id and get last N day data from form_submission_table
	 * @param superAppId
	 * @param appId
	 * @return
	 */
	public ProjectSubmissionCountForEntity getLastNDayProjectData(UUID superAppId, UUID appId, String parentEntity, String entity, int day );
}
