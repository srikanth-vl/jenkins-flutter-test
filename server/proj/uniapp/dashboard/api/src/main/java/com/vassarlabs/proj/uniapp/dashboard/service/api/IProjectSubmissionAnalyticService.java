package com.vassarlabs.proj.uniapp.dashboard.service.api;

import java.util.List;
import java.util.UUID;

import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionCountForEntity;

public interface IProjectSubmissionAnalyticService {
	public List<ProjectSubmissionCountForEntity> getProjectSubmssionAnyticsReport(UUID superApp, UUID appId,String parentEntityName, String parentEntityValue);
}
