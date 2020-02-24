package com.vassar.unifiedapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vassar.unifiedapp.asynctask.MediaRequestHandlerTask;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.utils.Utils;

public class MediaRequestReceiver extends BroadcastReceiver {

    private static final Object LOCK = new Object();
    public static boolean isMediaThreadRunning = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Utils.getInstance().isOnline(null)) {
            Utils.logInfo(LogTags.MEDIA_THREAD,"request for new thread");
            synchronized (LOCK) {
                if(!isMediaThreadRunning) {

                    Utils.logInfo(LogTags.MEDIA_THREAD, "request task called");
                    MediaRequestHandlerTask mediaRequestHandlertask = new MediaRequestHandlerTask();
                    mediaRequestHandlertask.execute();
                }
            }
        }

    }
}
