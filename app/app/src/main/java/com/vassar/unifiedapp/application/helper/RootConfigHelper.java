package com.vassar.unifiedapp.application.helper;

import android.provider.DocumentsContract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.api.RootConfigService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;

public class RootConfigHelper {

    private static final RootConfigHelper ourInstance = new RootConfigHelper();

    public static RootConfigHelper getInstance() {
        return ourInstance;
    }

    private long lastSyncTime;

    private RootConfigHelper() {
        this.lastSyncTime = 0;
    }

    public void initRootConfig()
            throws AppCriticalException, AppOfflineException {

        UAAppContext appContext = UAAppContext.getInstance();

        // Fetch rootConfig from the DB
        String userId = UAAppContext.getInstance().getUserID();
        ConfigFile rootConfigFile = appContext.getDBHelper().getConfigFile(userId,
                Constants.ROOT_CONFIG_DB_NAME);
        RootConfig rootConfig = null;
        if (rootConfigFile == null || rootConfigFile.getConfigContent().isEmpty()) {
            UAAppContext.getInstance().setRootConfig(null);
            // Add to Logs or console
            // Commented toast because cant launch from worker thread
            // Toast.makeText(appContext.getContext(), UAAppConstants.APP_MD_UNAVAILABLE, Toast.LENGTH_SHORT).show();

            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Root Config fetch");
            }
            // Fetching the app meta config from the server
            rootConfig = fetchRootConfigFromServer();
            Utils.logDebug(LogTags.ROOT_CONFIG, "Fetched Root Config from Server: " + rootConfig);
        } else {
            // Initializing singleton class
            ObjectMapper mapper = new ObjectMapper();
            try {
                rootConfig = mapper.readValue(rootConfigFile.getConfigContent(), RootConfig.class);
            } catch (IOException e) {
                e.printStackTrace();

            }
//            rootConfig = mGson.fromJson(rootConfigFile.getConfigContent(), RootConfig.class);
            Utils.logDebug(LogTags.ROOT_CONFIG, "Fetched Root Config from Local db: " + rootConfig);
        }

        appContext.setRootConfig(rootConfig);
    }

    private RootConfig fetchRootConfigFromServer()
            throws AppCriticalException, AppOfflineException {

        // Check if server available
        if (!Utils.getInstance().isOnline(null)) {
            throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Root Config fetch");
        }

        RootConfigService rootConfigService = new RootConfigService();
        RootConfig rootConfig = null;
        try {
            rootConfig = rootConfigService.callRootConfigService();
        } catch (UAException u) {
            // Error - exit app
            u.printStackTrace();
            throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error fetching Root Config - exit", u);
        }

        if (rootConfig == null) {
            // Error - exit app
            throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error fetching RootConfig (null) - exit");
        }
        this.lastSyncTime = System.currentTimeMillis();
        return rootConfig;
    }

    public RootConfig fetchFromServerForBackgroundSync()
            throws AppCriticalException, AppOfflineException {

        long syncFrequency = UAAppContext.getInstance().getAppMDConfig().getServerFrequency(Constants.SERVICE_FREQUENCY_ROOT_CONFIG);
        if ((System.currentTimeMillis() - lastSyncTime) <= syncFrequency) {
            Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Root Config was synced recently, will try in future");
            return UAAppContext.getInstance().getRootConfig();
        }
        return fetchRootConfigFromServer();
    }
}

