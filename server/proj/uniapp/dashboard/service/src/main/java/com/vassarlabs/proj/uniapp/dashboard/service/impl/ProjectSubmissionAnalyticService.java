package com.vassarlabs.proj.uniapp.dashboard.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kenai.constantine.Constant;
import com.vassarlabs.prod.common.utils.DateUtils;
import com.vassarlabs.proj.uniapp.application.dsp.pojo.ProjectSubmissionAnalyticsData;
import com.vassarlabs.proj.uniapp.constants.CommonConstants;
import com.vassarlabs.proj.uniapp.constants.RootConfigurationConstants;
import com.vassarlabs.proj.uniapp.crud.service.ApplicationMetaDataCrudService;
import com.vassarlabs.proj.uniapp.crud.service.ProjectSubmissionAnalyticsDataCrudService;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.LastNDaySubmissionCount;
import com.vassarlabs.proj.uniapp.dashboard.api.pojo.ProjectSubmissionCountForEntity;
import com.vassarlabs.proj.uniapp.dashboard.service.api.IProjectSubmissionAnalyticService;
@Component
public class ProjectSubmissionAnalyticService implements IProjectSubmissionAnalyticService {

	@Autowired 
	private ProjectSubmissionAnalyticsDataCrudService projectSubmissionAnalyticsDataCrudService;
	@Autowired  private 
	ApplicationMetaDataCrudService applicationMetaDataCrudService;
	@Override
	public List<ProjectSubmissionCountForEntity> getProjectSubmssionAnyticsReport(UUID superApp, UUID appId,String parentEntityName, String parentEntityValue) {
		if(parentEntityName == null || parentEntityName.equals("null") || parentEntityValue == null || parentEntityValue.equals("null")) {
			parentEntityValue=CommonConstants.NA;
		}
		List<ProjectSubmissionCountForEntity> projectSubmissionAnalytics = new ArrayList<ProjectSubmissionCountForEntity>();
		int dateb1MonthBack = DateUtils.getYYYYMMdd(DateUtils.getLastMonthSameTimeTs(System.currentTimeMillis()));
		int date1WeekBack = DateUtils.getYYYYMMdd(DateUtils.getLast7DaysTs(System.currentTimeMillis()));
		int date1DayBack =  DateUtils.getYYYYMMdd(DateUtils.getYesterdaySameTimeTs(System.currentTimeMillis()));
		List<String>  attributes = applicationMetaDataCrudService.getEntityHeirarchyForApp(superApp, appId);
		String entityName = null;
		if(parentEntityName !=  null  && attributes != null && !attributes.isEmpty()) {
			int indexOfParentEntityName = attributes.indexOf(parentEntityName);

			if(indexOfParentEntityName>= 0 && indexOfParentEntityName < attributes.size()-1) {
				entityName = attributes.get(indexOfParentEntityName+1);
			} else {
				return projectSubmissionAnalytics;
			} 
		} else {
			entityName = attributes== null || attributes.isEmpty() ? CommonConstants.PROJECT_SUBMISSION_ANALYTICS_DEFAULT_HIERARCHY_ELEMENT : attributes.get(0);
		}
		List<ProjectSubmissionAnalyticsData> data =  projectSubmissionAnalyticsDataCrudService.findSubmissionAnalyticsForForGivenParentEntity(superApp, appId, parentEntityValue, dateb1MonthBack);
		
		Map<String, ProjectSubmissionCountForEntity> enitityToSubmissionCountMap = new HashMap<String, ProjectSubmissionCountForEntity>();

		if(data != null && !data.isEmpty()) {
			for (ProjectSubmissionAnalyticsData projectSubmissionAnalyticsData : data) {
				Set<UUID> ids = projectSubmissionAnalyticsData.getSuccessfulSubmissionProjectIds() == null ? new HashSet<>() : projectSubmissionAnalyticsData.getSuccessfulSubmissionProjectIds();
				Set<UUID> failedids = projectSubmissionAnalyticsData.getFailedSubmissionProjectIds() == null ? new HashSet<>() : projectSubmissionAnalyticsData.getFailedSubmissionProjectIds();

				String entity = projectSubmissionAnalyticsData.getEntity();
				int date = projectSubmissionAnalyticsData.getDate();
				ProjectSubmissionCountForEntity submissionCountData = null;
				if( enitityToSubmissionCountMap.get(entity) == null) {
					submissionCountData = new ProjectSubmissionCountForEntity();
					submissionCountData.setEntity(entity);
					submissionCountData.setParentEntity(parentEntityValue);
					submissionCountData.setEntityName(entityName);
					submissionCountData.setLast30DaysSubmissionCount(getLastSubmissionObject());
					submissionCountData.setLast7DaysSubmissionCount(getLastSubmissionObject());
					submissionCountData.setLastDaySubmissionCount(getLastSubmissionObject());
				} else {
					submissionCountData= enitityToSubmissionCountMap.get(entity);
				}
				Set<UUID> last1daySuccessfullProjectIds = submissionCountData.getLastDaySubmissionCount().getSuccessfulProjectIds();
				Set<UUID> last1dayFailedProjectIds = submissionCountData.getLastDaySubmissionCount().getFailedProjectids();

				Set<UUID> last1WeakSuccessfullProjectIds = submissionCountData.getLast7DaysSubmissionCount().getSuccessfulProjectIds();
				Set<UUID> last1WeakFailedProjectIds = submissionCountData.getLast7DaysSubmissionCount().getFailedProjectids();

				Set<UUID> last1MonthSuccessfullProjectIds = submissionCountData.getLast30DaysSubmissionCount().getSuccessfulProjectIds();
				Set<UUID> last1MonthFailedProjectIds = submissionCountData.getLast30DaysSubmissionCount().getFailedProjectids();
				if(date >= date1DayBack) {
					last1daySuccessfullProjectIds.addAll(ids);
					submissionCountData.getLastDaySubmissionCount().setSuccessfulProjectIds(last1daySuccessfullProjectIds);
					submissionCountData.getLastDaySubmissionCount().setSuccessfulSubmissionCount(last1daySuccessfullProjectIds.size());
					last1dayFailedProjectIds.addAll(failedids);
					submissionCountData.getLastDaySubmissionCount().setFailedProjectids(last1dayFailedProjectIds);
					submissionCountData.getLastDaySubmissionCount().setFailedSubmissionCount(last1dayFailedProjectIds.size());
				}
				if(date >= date1WeekBack) {
					last1WeakSuccessfullProjectIds.addAll(ids);
					submissionCountData.getLast7DaysSubmissionCount().setSuccessfulProjectIds(last1WeakSuccessfullProjectIds);
					submissionCountData.getLast7DaysSubmissionCount().setSuccessfulSubmissionCount(last1WeakSuccessfullProjectIds.size());
					last1WeakFailedProjectIds.addAll(failedids);
					submissionCountData.getLast7DaysSubmissionCount().setFailedProjectids(last1WeakFailedProjectIds);
					submissionCountData.getLast7DaysSubmissionCount().setFailedSubmissionCount(last1WeakFailedProjectIds.size());

				}
				if(date >= dateb1MonthBack) {
					last1MonthSuccessfullProjectIds.addAll(ids);
					submissionCountData.getLast30DaysSubmissionCount().setSuccessfulProjectIds((last1MonthSuccessfullProjectIds) );
					submissionCountData.getLast30DaysSubmissionCount().setSuccessfulSubmissionCount(last1MonthSuccessfullProjectIds.size());
					last1MonthFailedProjectIds.addAll(failedids);
					submissionCountData.getLast30DaysSubmissionCount().setFailedProjectids(last1MonthFailedProjectIds);
					submissionCountData.getLast30DaysSubmissionCount().setFailedSubmissionCount(last1MonthFailedProjectIds.size());
				}



				enitityToSubmissionCountMap.put(entity, submissionCountData);
			}
			for (String  entity: enitityToSubmissionCountMap.keySet()) {
				if(enitityToSubmissionCountMap.get(entity) != null) {
					projectSubmissionAnalytics.add(enitityToSubmissionCountMap.get(entity));
				}

			}
		}
		return projectSubmissionAnalytics;
	}

	LastNDaySubmissionCount getLastSubmissionObject() {
		LastNDaySubmissionCount projectSubmisionCountInfo = new LastNDaySubmissionCount();
		projectSubmisionCountInfo.setFailedProjectids(new HashSet<>());
		projectSubmisionCountInfo.setFailedSubmissionCount(0);
		projectSubmisionCountInfo.setSuccessfulSubmissionCount(0);
		projectSubmisionCountInfo.setSuccessfulProjectIds(new HashSet<>());
		return projectSubmisionCountInfo;
	}
}
