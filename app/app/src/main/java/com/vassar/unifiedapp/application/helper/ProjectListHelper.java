package com.vassar.unifiedapp.application.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.api.ProjectListService;
import com.vassar.unifiedapp.api.ProjectTypeService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProjectListHelper {

    private static final ProjectListHelper ourInstance = new ProjectListHelper();

    public static ProjectListHelper getInstance() {
        return ourInstance;
    }

    private ObjectMapper mJsonObjectMapper;

    private long lastSyncTime;

    private ProjectListHelper() {
        this.mJsonObjectMapper = new ObjectMapper();
        this.lastSyncTime = 0;
    }

    public void initProjectList(Map<String, ProjectTypeConfiguration> appToPTCMap)
            throws AppCriticalException, AppOfflineException {

        UAAppContext appContext = UAAppContext.getInstance();

        // Fetch Project Count from the DB
        int projectCount = appContext.getDBHelper().getProjectCountForUser(appContext.getUserID());
        if (projectCount > 0) {
            // There are projects available for this user - good to start the app
            return;
        }

        // Fetching the project list config from the server

        if(appToPTCMap != null ) {
            fetchProjectListFromServer(appToPTCMap);
        }
        Utils.logDebug(LogTags.PROJECT_LIST_CONFIG, "Fetched Project List Config from Server successfully");
    }

    private void fetchProjectListFromServer(Map<String, ProjectTypeConfiguration> appToPTCMap)
            throws AppCriticalException, AppOfflineException {

        ProjectListService projectListService = new ProjectListService();

        Iterator<String> appIterator = appToPTCMap.keySet().iterator();
        while (appIterator.hasNext()) {

            ProjectTypeConfiguration ptc = appToPTCMap.get(appIterator.next());

            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Project List fetch");
            }

            try {
                projectListService.callProjectListService(ptc, null);
            } catch (UAException u) {
                // Error - exit app
                u.printStackTrace();
                throw new AppCriticalException(UAAppErrorCodes.PROJECT_LIST_FETCH, "Error fetching Project List for project : " + ptc + " - exit", u);
            }
        }
        this.lastSyncTime = System.currentTimeMillis();
    }

    public void fetchFromServerForBackgroundSync()
            throws UAException, JSONException {

        long syncFrequency = UAAppContext.getInstance().getAppMDConfig().getServerFrequency(Constants.SERVICE_FREQUENCY_PROJECT_LIST);
        if ((System.currentTimeMillis() - lastSyncTime) <= syncFrequency) {
            Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Project List was synced recently, will try in future");
            return;
        }

        // Fetch all apps for this user
        ProjectTypeService pts = new ProjectTypeService();
        String userId = UAAppContext.getInstance().getUserID();
        List<ProjectTypeModel> ptmList = UAAppContext.getInstance().getRootConfig().mApplications;

        Map<String, ProjectTypeConfiguration> ptcMap = pts.fetchAppIdToProjectTypeConfigurationFromDb(userId, ptmList);
        ProjectListService pls = new ProjectListService();

        Iterator<String> projectTypeID = ptcMap.keySet().iterator();
        while (projectTypeID.hasNext()) {
            String appID = projectTypeID.next();
            ProjectTypeConfiguration ptc = ptcMap.get(appID);

            Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Syncing Project list for App : " + appID);
            pls.handleProjectSync(appID, ptc);
        }
    }
}

