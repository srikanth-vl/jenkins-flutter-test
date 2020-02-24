package com.vassar.unifiedapp.newflow;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.vassar.unifiedapp.application.helper.EntityConfigHelper;
import com.vassar.unifiedapp.application.helper.MapConfigHelper;
import com.vassar.unifiedapp.application.helper.LocalizationConfigHelper;
import com.vassar.unifiedapp.application.helper.ProjectListHelper;
import com.vassar.unifiedapp.application.helper.ProjectTypeHelper;
import com.vassar.unifiedapp.application.helper.RootConfigHelper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.util.Map;

/**
 * Part 2 of the App Start up - Thread
 *
 * Executes app initialization post login - based on user id
 *
 */
public class AppStartupThread2 extends AsyncTask<Void, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Void... voids) {

        Utils.logInfo(LogTags.APP_STARTUP, "Start of App Startup Thread");

        Boolean result = true;
        try {



            // Initialize Localization Config
            initializeLocalizationConfig();

            // Initialize Root Config
            initializeRootConfig();

            //Initialize Entity Config
            initializeEntityConfig();

            // Initialize Map Config
            initializeMapConfig();

            // Initialize Project Type
            Map<String, ProjectTypeConfiguration> appToPTCMap = initializeProjectType();

            // Initialize Project List
            initializeProjectList(appToPTCMap);

            // Set the shared Preferences
            saveToAppPreferences(UAAppContext.getInstance().getUserID(), UAAppContext.getInstance().getToken());

        } catch (AppCriticalException e) {
            result = false;
            e.printStackTrace();
            Utils.logError(LogTags.APP_STARTUP, "Error executing App Startup Thread 2 -- continuing without downloading configs");
        } catch (AppOfflineException a) {
            Utils.logError(LogTags.APP_STARTUP, "No internet connection -- continuing without downloading configs");
        }

        return result;
    }

   private void initializeLocalizationConfig()
            throws AppCriticalException, AppOfflineException {
        // Load Root Config from DB
        //   If does not exist then read from server --> RootConfigService.readFromServer() & Save to DB
        LocalizationConfigHelper.getInstance().initLocalizationConfig();
        Utils.logInfo(LogTags.APP_STARTUP, "Completed Localization Config initialization...");
    }
    private void initializeRootConfig()
            throws AppCriticalException, AppOfflineException {
        // Load Root Config from DB
        //   If does not exist then read from server --> RootConfigService.readFromServer() & Save to DB
        RootConfigHelper.getInstance().initRootConfig();

        Utils.logInfo(LogTags.APP_STARTUP, "Completed Root Config initialization...");
    }

    private void initializeMapConfig()
            throws AppCriticalException, AppOfflineException {
        // Load Map Config from DB
        //   If does not exist then read from server --> MapConfigService.readFromServer() & Save to DB

        MapConfigHelper.getInstance().initMapConfig();

        Utils.logInfo(LogTags.APP_STARTUP, "Completed Map Config initialization...");
    }

    private Map<String, ProjectTypeConfiguration> initializeProjectType()
            throws AppCriticalException, AppOfflineException {
        // Load Project Type from DB
        //   If does not exist then read from server --> ProjectTypeService.readFromServer() & Save to DB

        Map<String, ProjectTypeConfiguration> appToPTCMap = ProjectTypeHelper.getInstance().initProjectType();

        Utils.logInfo(LogTags.APP_STARTUP, "Completed Project Type initialization...");
        return appToPTCMap;
    }

    private void initializeProjectList(Map<String, ProjectTypeConfiguration> appToPTCMap)
            throws AppCriticalException, AppOfflineException {
        // Load Project List from DB
        //   If does not exist then read from server --> ProjectListService.readFromServer() & Save to DB

        ProjectListHelper.getInstance().initProjectList(appToPTCMap);

        Utils.logInfo(LogTags.APP_STARTUP, "Completed Project List initialization...");
    }

    private void initializeEntityConfig() throws AppCriticalException, AppOfflineException {

        EntityConfigHelper.getInstance().initEntityConfig();
        Utils.logInfo(LogTags.APP_STARTUP, "Completed Entity List initialization...");
    }

    private void saveToAppPreferences(String username, String token) {
        // Making changes to shared preferences
        SharedPreferences.Editor editor = UAAppContext.getInstance().getAppPreferences().edit();
        editor.putString(Constants.USER_ID_PREFERENCE_KEY, username);
        editor.putString(Constants.USER_TOKEN_PREFERENCE_KEY, token);
        editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY, true);
        editor.apply();
    }
}
