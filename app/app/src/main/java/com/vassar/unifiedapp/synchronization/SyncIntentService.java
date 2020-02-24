package com.vassar.unifiedapp.synchronization;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

public class SyncIntentService extends IntentService {

    public SyncIntentService() {
        super("SyncIntentService");
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Toast.makeText(this, "yoyoyoyoyoyoyoy", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Toast.makeText(this, "yoyoyoyoyoyoyoy", Toast.LENGTH_LONG).show();

//        Utils.getInstance().showLog("INTENTSERVICE", "STARTING FOR NO BAG DAY");
//
//        Context context = ((UnifiedAppApplication) getApplicationContext()).mContext;
//        SynchronizationTask synchronizationTask = new SynchronizationTask(context, "Scheduler");
//        synchronizationTask.execute();
//
//        if (Utils.getInstance().isOnline(this)) {
//            if (!SynchronizationService.mIsSynchronizationRunning) {
//                SynchronizationTask synchronizationTask = new SynchronizationTask(this, null);
//                synchronizationTask.execute();
//            }
//        }

        // Condition 1 : If the application is present in foreground or background
        // Condition 2 : If there is an active internet connection
        // Condition 3 : If the SynchronizationService is not running already
        // If all conditions are satisfied, the SyncService is executed
//        Utils.getInstance().showLog("SYNCINTENTSERVICE", "CALLED");
//
//        long currentTime = System.currentTimeMillis();
//
//        UnifiedAppDBHelper dbHelper = new UnifiedAppDBHelper(this);
//        SharedPreferences appPreferences = getSharedPreferences(Constants.APP_PREFERENCES_KEY
//                , MODE_PRIVATE);
//        String userId = appPreferences.getString(Constants.USER_ID_PREFERENCE_KEY
//                , Constants.USER_ID_PREFERENCE_DEFAULT);
//
//        String appMetaConfigString = dbHelper.getConfigFile(Constants.APP_META_CONFIG_DB_NAME);
//        AppMetaData appMetaData = null;
//
//        if (appMetaConfigString != null) {
//            // AppMetaConfig exists on the local DB
//            Gson gson = new Gson();
//            appMetaData = gson.fromJson(appMetaConfigString, AppMetaData.class);
//        }
//
//        long repeatIntervalMillis;
//        if (appMetaData != null) {
//            if (appMetaData.mSyncInterval != null) {
//                repeatIntervalMillis = appMetaData.mSyncInterval * 60 * 1000;
//            } else {
//                repeatIntervalMillis = Constants.DEFAULT_SYNC_INTERVAL * 60 * 1000;
//            }
//        } else {
//            repeatIntervalMillis = Constants.DEFAULT_SYNC_INTERVAL * 60 * 1000;
//        }
//
//        if (userId != null && !userId.isEmpty()) {
//            UserMetaData userMetaData = dbHelper.getUserMeta(userId);
//
//            if (userMetaData != null) {
//                long lastNetworkConnection = userMetaData.mTimestamp;
//                if (currentTime - lastNetworkConnection > repeatIntervalMillis) {
//                    if (Utils.getInstance().isOnline(this)) {
//                        if (!SynchronizationService.mIsSynchronizationRunning) {
//                            SynchronizationTask synchronizationTask = new SynchronizationTask(this, null);
//                            synchronizationTask.execute();
//                        }
//                    }
//                }
//            }
//        }
    }
}
