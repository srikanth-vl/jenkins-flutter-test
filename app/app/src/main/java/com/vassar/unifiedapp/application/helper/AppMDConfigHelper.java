package com.vassar.unifiedapp.application.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.api.AppMetaConfigService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;

public class AppMDConfigHelper {

    private static final AppMDConfigHelper ourInstance = new AppMDConfigHelper();

    public static AppMDConfigHelper getInstance() {
        return ourInstance;
    }

    private long lastSyncTime;
    private ObjectMapper mJsonObjectMapper;
    private AppMDConfigHelper() {
        this.lastSyncTime = 0;
        this.mJsonObjectMapper = new ObjectMapper();
    }

    public void initAppMDConfig()
            throws AppCriticalException, AppOfflineException {

        UAAppContext appContext = UAAppContext.getInstance();

        // Fetch appMetaData from the DB
        ConfigFile appMetaConfigFile = appContext.getDBHelper().getConfigFile(Constants.DEFAULT_USER_ID,
                Constants.APP_META_CONFIG_DB_NAME);

        AppMetaData mAppMetaConfig = null;
        if (appMetaConfigFile == null || appMetaConfigFile.getConfigContent() == null || appMetaConfigFile.getConfigContent().isEmpty()) {
            // Add to Logs or console
            // Commented toast because cant launch from worker thread
            // Toast.makeText(appContext.getContext(), UAAppConstants.APP_MD_UNAVAILABLE, Toast.LENGTH_SHORT).show();

            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during App MD Config fetch");
            }
            // Fetching the app meta config from the server
            mAppMetaConfig = fetchAppMetaDataFromServer(null);
            Utils.logDebug(LogTags.APP_MD_CONFIG, "Fetched App MD Config from Server: " + mAppMetaConfig);
        } else {
//            Toast.makeText(appContext.getContext(), UAAppConstants.APP_MD_AVAILABLE, Toast.LENGTH_SHORT).show();
            // Initializing singleton class
            try {
                mAppMetaConfig = mJsonObjectMapper.readValue(appMetaConfigFile.getConfigContent(), AppMetaData.class);
                Utils.logDebug(LogTags.APP_MD_CONFIG, "Fetched App MD Config from Local db: " + mAppMetaConfig);
            } catch ( IOException e) {
                Utils.logError(LogTags.APP_MD_CONFIG, "failed to create AppMetaData object from config json :: " + mAppMetaConfig);
                e.printStackTrace();
            }
        }

        appContext.setAppMDConfig(mAppMetaConfig);
    }

    private AppMetaData fetchAppMetaDataFromServer(String appMDVersion)
            throws AppCriticalException, AppOfflineException {

        // Check if server available
        if (!Utils.getInstance().isOnline(null)) {
            throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during APP MD Config fetch");
        }

        AppMetaConfigService appMetaConfigService = new AppMetaConfigService();
        AppMetaData appMetaData = null;
        try {
            appMetaData = appMetaConfigService.callAppMetaDataService(appMDVersion);
        } catch (UAException u) {
            // Error - exit app
            u.printStackTrace();
            throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching AppMetaData - exit", u);
        }

        if (appMetaData == null) {
            // Error - exit app
            throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching AppMetaData (null) - exit");
        }

        this.lastSyncTime = System.currentTimeMillis();
        return appMetaData;
    }

    public AppMetaData fetchFromServerForBackgroundSync()
        throws AppCriticalException, AppOfflineException {

        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();

        if (appMetaData != null) {
            long syncFrequency = appMetaData.getServerFrequency(Constants.SERVICE_FREQUENCY_APP_MD_CONFIG);
            if ((System.currentTimeMillis() - lastSyncTime) <= syncFrequency) {
                Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Root Config was synced recently, will try in future");
                return UAAppContext.getInstance().getAppMDConfig();
            }
        }
        return fetchAppMetaDataFromServer(UAAppContext.getInstance().getAppMDConfig().mSplashConfigVersion);
    }
}

