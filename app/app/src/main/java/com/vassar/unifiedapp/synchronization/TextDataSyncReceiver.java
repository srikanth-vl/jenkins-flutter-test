package com.vassar.unifiedapp.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vassar.unifiedapp.asynctask.TextDataRequestHandler;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.utils.Utils;

public class TextDataSyncReceiver extends BroadcastReceiver {
    private static final Object LOCK = new Object();
    private final static Object SYNC_LOCK = new Object();
    public static Object getSyncLock() {
        return SYNC_LOCK;
    }
    public static boolean isTextSubmissionSyncThreadRunning = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.getInstance().isOnline(null)) {
            Utils.logInfo(LogTags.TEXT_SUBMISSION_SYNC_THREAD,"request for new thread");
            synchronized (LOCK) {
                if (!isTextSubmissionSyncThreadRunning) {
                    Utils.logInfo(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "request task called");
                    TextDataRequestHandler textDataRequestHandler = new TextDataRequestHandler();
                    textDataRequestHandler.execute();
                }
            }}}
}
