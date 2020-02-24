package com.vassar.unifiedapp.application.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.api.MapConfigService;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;

public class MapConfigHelper {

    private static final MapConfigHelper ourInstance = new MapConfigHelper();
    private long lastSyncTime;

    private MapConfigHelper() {
        this.lastSyncTime = 0;
    }

    public static MapConfigHelper getInstance() {
        return ourInstance;
    }

    public void initMapConfig()
            throws AppCriticalException, AppOfflineException {

        UAAppContext appContext = UAAppContext.getInstance();

        // Fetch rootConfig from the DB
        String userId = UAAppContext.getInstance().getUserID();

        ConfigFile mapConfigFile = appContext.getDBHelper().getConfigFile(userId,
                Constants.MAP_CONFIG_DB_NAME);

        MapConfigurationV1 mapConfig = null;
        if (mapConfigFile == null || mapConfigFile.getConfigContent().isEmpty()) {
            UAAppContext.getInstance().setMapConfig(null);

            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Map Config fetch");
            }
            // Fetching the app meta config from the server
            mapConfig = fetchMapConfigFromServer();

            Utils.logDebug(LogTags.MAP_CONFIG, "Fetched Map Config from Server: " + mapConfig);
        } else {
            // Initializing singleton class
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapConfig = mapper.readValue(mapConfigFile.getConfigContent(), MapConfigurationV1.class);
            } catch (IOException e) {
                e.printStackTrace();

            }
            Utils.logDebug(LogTags.MAP_CONFIG, "Fetched Map Config from Local db: " + mapConfig);
        }

        appContext.setMapConfig(mapConfig);
    }

    private MapConfigurationV1 fetchMapConfigFromServer()
            throws AppCriticalException, AppOfflineException {

        // Check if server available
        if (!Utils.getInstance().isOnline(null)) {
            throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Map Config fetch");
        }

        MapConfigService mapConfigService = new MapConfigService();
        MapConfigurationV1 mapConfig = null;
        try {
            mapConfig = mapConfigService.callMapConfigService();
        } catch (UAException u) {
            // Error - exit app
            u.printStackTrace();
            throw new AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error fetching Map Config - exit", u);
        }

        if (mapConfig == null) {
            // Error - exit app
            throw new AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error fetching MapConfig (null) - exit");
        }
        this.lastSyncTime = System.currentTimeMillis();
        return mapConfig;
    }

    public MapConfigurationV1 fetchFromServerForBackgroundSync()
            throws AppCriticalException, AppOfflineException {

        long syncFrequency = UAAppContext.getInstance().getAppMDConfig().getServerFrequency(Constants.SERVICE_FREQUENCY_MAP_CONFIG);
        if ((System.currentTimeMillis() - lastSyncTime) <= syncFrequency) {
            Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Map Config was synced recently, will try in future");
            return UAAppContext.getInstance().getMapConfig();
        }
        return fetchMapConfigFromServer();
    }
}