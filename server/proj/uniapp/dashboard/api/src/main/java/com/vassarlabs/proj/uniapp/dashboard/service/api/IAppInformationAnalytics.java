package com.vassarlabs.proj.uniapp.dashboard.service.api;

import java.io.IOException;
import java.util.List;

import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardTokenData;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.SuperAppAnalyticsData;

public interface IAppInformationAnalytics {
	
	/**
	 * For given user Id, get all super app Ids, and count of users for that super appId
	 * For all app Ids, get app Name and project submissions, total projects, total assigned projects
	 * @param userId
	 * @return
	 * @throws IOException 
	 */
	public List<SuperAppAnalyticsData> getSuperAppIdToAppData(String userId) throws IOException;
	
	public DashboardTokenData authenticate(String userId, String password);

}
