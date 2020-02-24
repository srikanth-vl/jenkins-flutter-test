package com.vassar.unifiedapp.newflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.vassar.unifiedapp.application.helper.AppMDConfigHelper;
import com.vassar.unifiedapp.application.helper.EntityConfigHelper;
import com.vassar.unifiedapp.application.helper.LocalizationConfigHelper;
import com.vassar.unifiedapp.application.helper.MapConfigHelper;
import com.vassar.unifiedapp.application.helper.ProjectListHelper;
import com.vassar.unifiedapp.application.helper.ProjectTypeHelper;
import com.vassar.unifiedapp.application.helper.RootConfigHelper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.LocalizationConfig;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;

public class AppBackgroundSync
        extends AsyncTask<Void, Void, Boolean> {

    private final static Object LOCK = new Object();
    private final static Object SYNC_LOCK = new Object();
    public static boolean isSyncInProgress = false;

    public static Object getSyncLock() {
        return SYNC_LOCK;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Start of App Background Sync Thread");

        if (!Utils.getInstance().isOnline(null)) {
            // Not online
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Stopping App Background Sync Thread - Network down");
        }

        // Add a check if Background thread sync is in progress
        // Then do not have to run
        synchronized (LOCK) {
            if (isSyncInProgress) {
                Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Another Background Sync thread is in progress - aborting this call");
                return true;
            }
            isSyncInProgress = true;
        }

        Boolean result = true;
        try {

            // Check if server available
            if (!Utils.getInstance().isOnline(null)) {
                // App offline - do not run background sync
                Utils.logDebug(LogTags.APP_BACKGROUND_SYNC, "Cannot initiate the sync without " +
                        "an active internet connection");
                return false;
            }

//            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Background thread is called");
//            SharedPreferences backgroundPreference;
//            backgroundPreference = UAAppContext.getInstance().getContext().getSharedPreferences("BACKGROUNDPREFERENCES", Context.MODE_PRIVATE);
//
//            int count = backgroundPreference.getInt("SYNCCOUNT", 0);
//            System.out.println("SYNC COUNT : "+count);
//
//            SharedPreferences.Editor edit = backgroundPreference.edit();
//            count = count + 1;
//            edit.putInt("SYNCCOUNT", count);
//            edit.apply();


            Intent intent = new Intent(Constants.PRE_SYNC_BROADCAST_ACTION);
            UAAppContext.getInstance().getContext().sendBroadcast(intent);


            // Start APP MD Config Sync
            if (Thread.currentThread().isInterrupted())
                return false;

            startAppMDConfigSync();

            // Start Localization Config Sync
            if (Thread.currentThread().isInterrupted())
                return false;
            startLocalizationConfigSync();

            // Start Root Config Sync
            if (Thread.currentThread().isInterrupted())
                return false;
            startRootConfigSync();

            //Start Entity Config Sync
            if (Thread.currentThread().isInterrupted())
                return false;
            startEntityConfigSync();

            // Start Map Config Sync
            if (Thread.currentThread().isInterrupted())
                return false;
            startMapConfigSync();

            // Start Project Type Sync
            if (Thread.currentThread().isInterrupted())
                return false;
            startProjectTypeSync();

            // Start Project List Sync
            if (Thread.currentThread().isInterrupted())
                return false;
            startProjectListSync();

        } catch (AppCriticalException e) {
            result = false;
            Utils.logError(LogTags.APP_BACKGROUND_SYNC, "App critical exception -- stopping sync", e);
        } catch (AppOfflineException e) {
            result = false;
            Utils.logError(LogTags.APP_BACKGROUND_SYNC, "App is offline, cannot continues sync " +
                    "-- stopping sync", e);
        } catch (JSONException e) {
            result = false;
            Utils.logError(LogTags.APP_BACKGROUND_SYNC, "Json exception encountered while " +
                    "background sync was in progress -- stopping sync", e);
        } catch (UAException e) {
            result = false;
            Utils.logError(LogTags.APP_BACKGROUND_SYNC, "Exception while background sync was" +
                    " in progress -- stopping sync", e);
        } finally {
            isSyncInProgress = false;
            Intent intent = new Intent(Constants.POST_SYNC_BROADCAST_ACTION);
            intent.putExtra("completed", result);
            UAAppContext.getInstance().getContext().sendBroadcast(intent);
        }

        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC + "App Background completed. Status -- " + result);

        return result;
    }

    private void startAppMDConfigSync()
            throws AppCriticalException, AppOfflineException {

        // Read AppMDConfig from Server and update local DB if there is a change
        AppMDConfigHelper appMDConfigHelper = AppMDConfigHelper.getInstance();
        appMDConfigHelper.fetchFromServerForBackgroundSync();

        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Completed APP MD Config sync...");
    }

    private void startRootConfigSync()
            throws AppCriticalException, AppOfflineException {

        // Read RootConfig from Server and update local DB if there is a change
        RootConfigHelper rootConfigHelper = RootConfigHelper.getInstance();
        rootConfigHelper.fetchFromServerForBackgroundSync();
        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Completed Root Config sync...");
    }
    private void startLocalizationConfigSync()
            throws AppCriticalException, AppOfflineException {
        // Read LocalizationConfig from Server and update local file if there is a change
        LocalizationConfigHelper localizationConfigHelper = LocalizationConfigHelper.getInstance();
        localizationConfigHelper.fetchFromServerForBackgroundSync();
        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Completed Localization Config sync...");
    }

    private void startMapConfigSync()
            throws AppCriticalException, AppOfflineException {

        // Read MapConfig from Server and update local DB if there is a change
        MapConfigHelper mapConfigHelper = MapConfigHelper.getInstance();
        mapConfigHelper.fetchFromServerForBackgroundSync();
        LocalizationConfigHelper.getInstance().fetchFromServerForBackgroundSync();
        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Completed Map Config sync...");
    }
    private void startProjectTypeSync()
            throws AppCriticalException, AppOfflineException {

        // Read Project Type from Server and update local DB if there is a change
        ProjectTypeHelper projectTypeHelper = ProjectTypeHelper.getInstance();
        projectTypeHelper.fetchFromServerForBackgroundSync();

        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Completed Project Type sync...");
    }

    private void startProjectListSync()
            throws UAException, JSONException {

        // Read Project Type from Server and update local DB if there is a change
        ProjectListHelper.getInstance().fetchFromServerForBackgroundSync();

        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Completed Project List sync...");
    }

    private void startEntityConfigSync()
            throws UAException, JSONException {

        EntityConfigHelper.getInstance().fetchFromServerForBackgroundSync();
        Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Completed Entity Config sync...");
    }
}
