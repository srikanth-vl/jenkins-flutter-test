package com.vassarlabs.proj.uniapp.dashboard.service.api;

import java.util.List;
import java.util.UUID;

import com.vassarlabs.proj.uniapp.dashboard.api.pojo.BusinessAnalyticsResponse;

public interface IBusinessAnalyticsService {
		
	public List<BusinessAnalyticsResponse> generateComputedJson(UUID superAppId, UUID appId, String parentEntityValue, String parentEntityName);

}
