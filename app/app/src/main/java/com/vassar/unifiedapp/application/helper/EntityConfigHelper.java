package com.vassar.unifiedapp.application.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.api.EntityConfigService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;

import java.util.Map;


public class EntityConfigHelper {

    private static final EntityConfigHelper ourInstance = new EntityConfigHelper();
    public static EntityConfigHelper getInstance() {
        return ourInstance;
    }
    private UnifiedAppDBHelper mDbHelper;

    private ObjectMapper mJsonObjectMapper;
    private long lastSyncTime;

    private EntityConfigHelper () {
        this.mJsonObjectMapper = new ObjectMapper();
        this.lastSyncTime = 0;
        mDbHelper = UAAppContext.getInstance().getDBHelper();
    }

    public void initEntityConfig()
            throws AppCriticalException, AppOfflineException {

        Utils.logInfo("ENTITYCONFIG", "Initiate entity config fetch from server");
        // Fetching the entity config from the server
        fetchEntityConfigFromServer();

        Utils.logDebug(LogTags.ENTITY_CONFIG, "Fetched Entity Config from Server successfully");

    }

    private void fetchEntityConfigFromServer()
            throws AppCriticalException, AppOfflineException {

        // Get the app ids for this user from rootconfig
        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
        if (rootConfig == null || rootConfig.mApplications == null || rootConfig.mApplications.size() == 0) {
            Utils.logWarn(LogTags.ENTITY_CONFIG, "No project type configured for this user");
            return;
        }

        EntityConfigService entityConfigService = new EntityConfigService();

        String userId = UAAppContext.getInstance().getUserID();

        for (ProjectTypeModel ptm : rootConfig.mApplications) {
            String appId = ptm.mAppId;
            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Entity config fetch");
            }
            try {

                long latestTimeStamp = mDbHelper.getLatestTimeStampForEntityMetaData(Constants.SUPER_APP_ID, appId);
                entityConfigService.callEntityConfigService(Constants.SUPER_APP_ID, Constants.DEFAULT_PROJECT_ID, userId, null, null, null, latestTimeStamp);

            } catch (UAException u) {
                // Error - exit app
                u.printStackTrace();
                throw new AppCriticalException(UAAppErrorCodes.ENTITY_CONFIG_FETCH, "Error fetching Entity Config - exit", u);
            }
        }
        this.lastSyncTime = System.currentTimeMillis();
    }

    public void fetchFromServerForBackgroundSync()
            throws AppCriticalException, AppOfflineException {

        
        long syncFrequency = UAAppContext.getInstance().getAppMDConfig().getServerFrequency(Constants.SERVICE_FREQUENCY_PROJECT_TYPE);
        if ((System.currentTimeMillis() - lastSyncTime) <= syncFrequency) {
            Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Entity Config was synced recently, will try in future");
        }
        // This result holds only the delta (changes in version) for EntityConfig
        fetchEntityConfigFromServer();
        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Entity Config was synced successfully");

    }
}