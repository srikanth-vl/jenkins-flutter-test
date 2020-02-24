package com.vassar.unifiedapp.application.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.api.LocalizationConfigService;
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
import com.vassar.unifiedapp.utils.LocalizationUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

import java.io.IOException;

public class LocalizationConfigHelper {

    private static final LocalizationConfigHelper ourInstance = new LocalizationConfigHelper();

    public static LocalizationConfigHelper getInstance() {
        return ourInstance;
    }

    private long lastSyncTime;

    private LocalizationConfigHelper() {
        this.lastSyncTime = 0;
    }

    public void initLocalizationConfig()
            throws AppCriticalException, AppOfflineException {

        UAAppContext appContext = UAAppContext.getInstance();

        // Fetch localizationConfig from the file
        String localizationConfig = LocalizationUtils.getInstance().getData();
        JSONObject localizationConfigJSON = null;
        if (localizationConfig == null || localizationConfig.isEmpty()) {
            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Localization Config fetch");
            }
            // Fetching the app meta config from the server
            localizationConfigJSON = fetchLocalizationConfigFromServer();
            Utils.logDebug(LogTags.LOCALIZATION_CONFIG, "Fetched Localization Config from Server: " + localizationConfigJSON);
        } else {
            if(localizationConfig != null && !localizationConfig.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                localizationConfigJSON = mapper.convertValue(localizationConfig, JSONObject.class);
                Utils.logDebug(LogTags.LOCALIZATION_CONFIG, "Fetched Localization Config from Local file: " + localizationConfig);
            }
        }
        if(localizationConfigJSON != null) {
            appContext.setLocalizationJson(localizationConfigJSON);
        }
    }

    private JSONObject fetchLocalizationConfigFromServer()
            throws AppCriticalException, AppOfflineException {

        // Check if server available
        if (!Utils.getInstance().isOnline(null)) {
            throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Root Config fetch");
        }

        LocalizationConfigService localizationConfigService = new LocalizationConfigService();
        JSONObject localizationConfigJSON = null;
        try {
            localizationConfigJSON = localizationConfigService.callLocationConfigService();
        } catch (UAException u) {
            // Error - exit app
            u.printStackTrace();
            throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error fetching Root Config - exit", u);
        }

        if (localizationConfigJSON == null) {
            // Error - exit app
            throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error fetching RootConfig (null) - exit");
        }
        this.lastSyncTime = System.currentTimeMillis();
        return localizationConfigJSON;
    }

    public JSONObject fetchFromServerForBackgroundSync()
            throws AppCriticalException, AppOfflineException {

        long syncFrequency = UAAppContext.getInstance().getAppMDConfig().getServerFrequency(Constants.SERVICE_FREQUENCY_ROOT_CONFIG);
        if ((System.currentTimeMillis() - lastSyncTime) <= syncFrequency) {
            Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Root Config was synced recently, will try in future");
            return UAAppContext.getInstance().getLocalization();
        }
        return fetchLocalizationConfigFromServer();
    }
}

