package com.vassarlabs.proj.uniapp.dashboard.service.api;

import java.util.UUID;

import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionCountForEntity;

public interface IUserLastNDayData {

	/**
	 * For super app id && app id, get last N day data from user tracking table
	 * @param superAppId
	 * @param appId
	 * @return
	 */
	public ProjectSubmissionCountForEntity getLastNDayUserData(UUID superAppId, UUID appId);
}
