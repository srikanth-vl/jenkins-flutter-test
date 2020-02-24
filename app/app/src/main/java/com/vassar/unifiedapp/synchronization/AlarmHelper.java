package com.vassar.unifiedapp.synchronization;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.receiver.MediaRequestReceiver;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

public class AlarmHelper {

    public static void scheduleAlarm(Context context) {

        AppMetaData appMetaData = null;

        appMetaData = UAAppContext.getInstance().getAppMDConfig();

        if (appMetaData == null) {
            Utils.logError(UAAppErrorCodes.ALARM_HELPER, "Post Sync Update Receiver -- Home -- called");
            return;
        }

        long repeatIntervalMillis;
        if (appMetaData.mSyncInterval != null) {
            repeatIntervalMillis = appMetaData.mSyncInterval;
        } else {
            repeatIntervalMillis = Constants.DEFAULT_SYNC_INTERVAL;
        }

        // Get the Alarm Service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent
                (context, SyncReceiver.class), 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    (System.currentTimeMillis() + repeatIntervalMillis),
                    repeatIntervalMillis, intent);
        }
    }
    
    public static void beginMediaRequest(Context context) {

        long media_sync_interval = UAAppContext.getInstance().getAppMDConfig().getMediaSyncFrequency(Constants.MEDIA_SYNC_FREQUENCY);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent
                (context, MediaRequestReceiver.class), 0);

        if (alarmManager != null) {
            Utils.logInfo(LogTags.MEDIA_THREAD, "alarmhelper called");
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + media_sync_interval),
                    media_sync_interval, intent);
        }
    }

    public static void cancelMediaAlarm () {

        AlarmManager alarmManager = (AlarmManager) UAAppContext.getInstance().getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getBroadcast(UAAppContext.getInstance().getContext(), 0, new Intent
                (UAAppContext.getInstance().getContext(), MediaRequestReceiver.class), 0);

        alarmManager.cancel(intent);
        intent.cancel();
    }

    public static void cancelBackgroundSyncAlarm () {

        AlarmManager alarmManager = (AlarmManager) UAAppContext.getInstance().getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = PendingIntent.getBroadcast(UAAppContext.getInstance().getContext(), 0, new Intent
                (UAAppContext.getInstance().getContext(), SyncReceiver.class), 0);

        alarmManager.cancel(intent);
        intent.cancel();
    }

    public static void beginAllTextDataRequest(Context context) {

        AppMetaData appMetaData = null;

        appMetaData = UAAppContext.getInstance().getAppMDConfig();

        if (appMetaData == null) {
            Utils.logError(UAAppErrorCodes.ALARM_HELPER, "Post Sync Update Receiver -- Home -- called");
            return;
        }

        long repeatIntervalMillis;
        if (appMetaData.mSyncInterval != null) {
            repeatIntervalMillis = appMetaData.getServerFrequency(Constants.TEXT_DATA_INTERVAL);
        } else {
            repeatIntervalMillis = Constants.TEXT_DATA_SYNC_INTERVAL;
        }

        // Get the Alarm Service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent
                (context, TextDataSyncReceiver.class), 0);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    (System.currentTimeMillis() + repeatIntervalMillis),
                    repeatIntervalMillis, intent);
        }
    }
}
