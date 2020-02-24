package com.vassar.unifiedapp.application.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.api.ProjectTypeService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectSpecificForms;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTypeHelper {

    private static final ProjectTypeHelper ourInstance = new ProjectTypeHelper();

    public static ProjectTypeHelper getInstance() {
        return ourInstance;
    }

    private ObjectMapper mJsonObjectMapper;

    private long lastSyncTime;

    private ProjectTypeHelper() {
        this.mJsonObjectMapper = new ObjectMapper();
        this.lastSyncTime = 0;
    }

    public Map<String, ProjectTypeConfiguration> initProjectType()
            throws AppCriticalException, AppOfflineException {

        UAAppContext appContext = UAAppContext.getInstance();

        // Fetch Project Type Count from the DB
        int projectTypeCount = appContext.getDBHelper().getProjectTypeCountForUser(appContext.getUserID());
        if (projectTypeCount > 0) {
            // There are project types available for this user - good to start the app
            // TODO : If the project type was saved and the thread failed for some reason, then
            // null will be returned and the ProjectList call fails
            // Do not return null
            // Fetch the map and return

            ProjectTypeService projectTypeService = new ProjectTypeService();
            Map<String, ProjectTypeConfiguration> appToPTCMap = projectTypeService.fetchAppIdToProjectTypeConfigurationFromDb(
                    UAAppContext.getInstance().getUserID(), UAAppContext.getInstance().getRootConfig().mApplications);

            return appToPTCMap;
        }

        // Fetching the project type config from the server
        Map<String, ProjectTypeConfiguration> appToPTCMap = fetchProjectTypeFromServer();
        Utils.logDebug(LogTags.PROJECT_TYPE_CONFIG, "Fetched Project Type Config from Server successfully");

        return appToPTCMap;
    }

    private Map<String, ProjectTypeConfiguration> fetchProjectTypeFromServer()
            throws AppCriticalException, AppOfflineException {

        // Get the app ids for this user from rootconfig
        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
        if (rootConfig.mApplications == null || rootConfig.mApplications.size() == 0) {
            Utils.logWarn(LogTags.PROJECT_TYPE_CONFIG, "No Project types configured for this user");
            return null;
        }
        String userId = UAAppContext.getInstance().getUserID();
        ProjectTypeService projectTypeService = new ProjectTypeService();
        Map<String, ProjectTypeConfiguration> appToPTCMap = new HashMap<>();
        for (ProjectTypeModel ptm : rootConfig.mApplications) {
            String appId = ptm.mAppId;
            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Project Type fetch");
            }

            try {
                ProjectTypeConfiguration existingConfiguration  = projectTypeService.fetchProjectTypeConfigurationFromDb(userId, appId);
                Map<String, Map<String, Integer>> formVersionMap = getFormVersionMap(existingConfiguration);
                ProjectTypeConfiguration ptc = projectTypeService.callProjectTypeConfigService(ptm.mAppId, formVersionMap);
                if(ptc == null) {
                    return null;
                }
                appToPTCMap.put(ptm.mAppId, ptc);
            } catch (UAException u) {
                // Error - exit app
                u.printStackTrace();
                throw new AppCriticalException(UAAppErrorCodes.PROJECT_TYPE_FETCH, "Error fetching Project Type Config - exit", u);
            }
        }
        this.lastSyncTime = System.currentTimeMillis();
        return appToPTCMap;
    }

    public void fetchFromServerForBackgroundSync()
            throws AppCriticalException, AppOfflineException {

        Map<String, ProjectTypeConfiguration> result = null;
        long syncFrequency = UAAppContext.getInstance().getAppMDConfig().getServerFrequency(Constants.SERVICE_FREQUENCY_PROJECT_TYPE);
        if ((System.currentTimeMillis() - lastSyncTime) <= syncFrequency) {
            Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Project Type was synced recently, will try in future");
        }
        // This result holds only the delta (changes in version) - the next step (syncing
        // project list) requires ProjectTypeConfiguration
        result = fetchProjectTypeFromServer();
        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Project Type was synced successfully");

    }

    public Map<String, Map<String, Integer>> getFormVersionMap(ProjectTypeConfiguration projectTypeConfiguration) {
        if(projectTypeConfiguration == null || projectTypeConfiguration.mContent == null || projectTypeConfiguration.mContent.isEmpty()) {
            return null;
        }
        Map<String, Map<String, ProjectSpecificForms>> projectIdToFormTypeMap = projectTypeConfiguration.mContent;

        Map<String, Map<String, Integer>> formVersionMap = new HashMap<>();
        for (String projectId : projectIdToFormTypeMap.keySet()) {
            Map<String, Integer> formTypeToVersionMap = new HashMap<>();
            Map<String, ProjectSpecificForms> formTypeToFormMap = projectIdToFormTypeMap.get(projectId) == null ? new HashMap<>() : projectIdToFormTypeMap.get(projectId);
            for (String formType : formTypeToFormMap.keySet()) {
                ProjectSpecificForms form = formTypeToFormMap.get(formType);
                formTypeToVersionMap.put(formType, form.mFormVerion);
            }
            formVersionMap.put(projectId, formTypeToVersionMap);
        }

        return formVersionMap;

    }
}

