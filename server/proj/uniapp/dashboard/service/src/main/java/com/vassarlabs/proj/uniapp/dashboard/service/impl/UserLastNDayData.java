package com.vassarlabs.proj.uniapp.dashboard.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionCountForEntity;
import com.vassarlabs.proj.uniapp.dashboard.service.api.IUserLastNDayData;

@Service
public class UserLastNDayData implements IUserLastNDayData {

	@Override
	public ProjectSubmissionCountForEntity getLastNDayUserData(UUID superAppId, UUID appId) {
		return null;
	}

}
