package com.vassarlabs.proj.uniapp.dashboard.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionCountForEntity;
import com.vassarlabs.proj.uniapp.dashboard.service.api.IProjectLastNDayData;

@Component
public class ProjectLastNDayData implements IProjectLastNDayData {

	@Override
	public ProjectSubmissionCountForEntity getLastNDayProjectData(UUID superAppId, UUID appId, String parentEntity, String entity, int day ) {
		return null;
	}

}
