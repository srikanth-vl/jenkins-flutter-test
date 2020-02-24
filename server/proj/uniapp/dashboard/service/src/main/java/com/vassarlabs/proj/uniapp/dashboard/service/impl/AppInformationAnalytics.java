package com.vassarlabs.proj.uniapp.dashboard.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ApplicationMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardTokenData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.DashboardUser;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.SuperApplicationData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserDBMetaData;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.UserTokenData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.DashboardTokenDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.DashboardUserDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectMasterDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.SuperAppDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserProjectMapCrudService;
import com.vassarlabs.proj.uniapp.crud.service.UserTokenDataCrudService;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.AppAnalyticsData;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.SuperAppAnalyticsData;
import com.vassarlabs.proj.uniapp.dashboard.service.api.IAppInformationAnalytics;
import com.vassarlabs.proj.uniapp.enums.ProjectStates;
import com.vassarlabs.proj.uniapp.enums.UserStates;

import javaxt.json.JSONObject;

@Service
public class AppInformationAnalytics implements IAppInformationAnalytics {

	@Autowired
	DashboardUserDataCrudService dashboardUserCrudService;

	@Autowired
	DashboardTokenDataCrudService dashboardTokenService;

	@Autowired
	SuperAppDataCrudService superAppCrudService;

	@Autowired
	UserMetaDataCrudService userDataCrudService;

	@Autowired
	UserTokenDataCrudService userTokenCrudService;

	@Autowired
	ApplicationMetaDataCrudService appMetaCrudService;

	@Autowired
	ProjectMasterDataCrudService projectCrudService;

	@Autowired
	UserProjectMapCrudService userProjectCrudService;

	@Autowired
	AppInstallationFromJsoup appInstallationService;

	@Override
	public DashboardTokenData authenticate(String userId, String password) {
		DashboardTokenData tokenData = null;
		DashboardUser userData = dashboardUserCrudService.findByPartitionKey(userId);
		if (userData != null && userData.getPassword().equals(password)) {
			UUID token = UUIDUtils.getTrueUUID();
			tokenData = new DashboardTokenData();
			tokenData.setUserId(userId);
			tokenData.setToken(token);
			tokenData.setInsertTs(System.currentTimeMillis());
			tokenData.setTokenExpired(CommonConstants.TOKEN_NOT_EXPIRED);
			dashboardTokenService.insertUserTokenData(tokenData);
			return tokenData;
		}
		return tokenData;
	}

	@Override
	public List<SuperAppAnalyticsData> getSuperAppIdToAppData(String userId) throws IOException {

		List<SuperAppAnalyticsData> superAppsData = new ArrayList<SuperAppAnalyticsData>();

		SuperAppAnalyticsData superAppData;
		DashboardUser userData = dashboardUserCrudService.findByPartitionKey(userId);
		List<UUID> superAppIds = userData.getSuperAppIds();

		for (UUID superAppId : superAppIds) {

			superAppData = new SuperAppAnalyticsData();
			SuperApplicationData latestData = superAppCrudService.findLatestVersion(superAppId);

			List<String> userIds = userDataCrudService.findUserDataByPartitionKey(superAppId, UserStates.ACTIVE)
					.stream().map(UserDBMetaData::getUserId).collect(Collectors.toList());
			int registeredUsers = userIds.size();

			String installations = appInstallationService.appInstallationsCount(latestData.getPackageName());
			if (installations != null) {
				superAppData.setInstallations(installations); 
			} else {
				superAppData.setInstallations("0");
			}

			superAppData.setSuperAppId(superAppId);
			superAppData.setSuperAppName(latestData.getName());
			superAppData.setRegisteredUsers(registeredUsers);

			List<UserTokenData> usersLoggedData = userTokenCrudService.findUsersLogInAtleastOnce(superAppId, userIds);
			for (UserTokenData user : usersLoggedData) {
				if (userIds.contains(user.getUserId())) {
					userIds.remove(user.getUserId());
				}
			}

			int usersNeverLoggedIn = userIds.size();
			superAppData.setUsersNeverLoggedIn(usersNeverLoggedIn);
			superAppData.setUsersLoggedIn(registeredUsers - usersNeverLoggedIn);

			JSONObject superAppObj = new JSONObject(latestData.getConfigFile());
			if (superAppObj.get("splashscreenproperties").get("loginicon").toString() != null) {
				superAppData.setImageUrl(superAppObj.get("splashscreenproperties").get("loginicon").toString());
			}

			Map<UUID, ApplicationMetaData> appToDataMap = appMetaCrudService.findLatestDataBySuperappId(superAppId);

			List<AppAnalyticsData> appsAnalData = new ArrayList<AppAnalyticsData>();

			for (UUID appId : appToDataMap.keySet()) {
				AppAnalyticsData data = new AppAnalyticsData();

				List<UUID> projIds = new ArrayList<UUID>();
				projIds.addAll(projectCrudService.getAllProjectIdsForApp(superAppId, appId, ProjectStates.INPROGRESS));
				projIds.addAll(projectCrudService.getAllProjectIdsForApp(superAppId, appId, ProjectStates.NEW));
				Set<UUID> projIdsSet = new HashSet<UUID>();

				for (UUID projId : projIds) {
					projIdsSet.add(projId);
				}

				int assignedProjects = userProjectCrudService.countOfProjectsAssignedToUsers(superAppId, appId);
				data.setAppId(appId);
				data.setParentId(appToDataMap.get(appId).getParentAppId());
				data.setTotalProjects(projIdsSet.size());
				data.setAssignedProjects(assignedProjects);

				JSONObject appObj = new JSONObject(appToDataMap.get(appId).getConfigData());
				List<String> attributesList = new ArrayList<>();
				ObjectMapper objectMapper = new ObjectMapper();
				if (appObj.get(RootConfigurationConstants.DASHBOARD_ANALYTICS_ENTITY_HIERARCHY) != null) {
					String attributesStr = appObj.get(RootConfigurationConstants.DASHBOARD_ANALYTICS_ENTITY_HIERARCHY)
							.toString();
					if (attributesStr != null) {
						try {
							attributesList
							.addAll(objectMapper.readValue(attributesStr, new TypeReference<List<String>>() {
							}));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if (appObj.get("name").toString() != null && appObj.get("icon").toString() != null) {
					data.setAppName(appObj.get("name").toString());
					data.setImageUrl(appObj.get("icon").toString());
					data.setAttribute_heirachy(attributesList);
				}
				appsAnalData.add(data);

			}

			superAppData.setAppAnalyticsData(appsAnalData);
			superAppsData.add(superAppData);
		}

		return superAppsData;
	}

}
