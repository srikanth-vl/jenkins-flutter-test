package com.vassar.unifiedapp.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vassar.unifiedapp.newflow.AppBackgroundSync;

public class SyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppBackgroundSync appBackgroundSync = new AppBackgroundSync();
        appBackgroundSync.execute();
    }
}
