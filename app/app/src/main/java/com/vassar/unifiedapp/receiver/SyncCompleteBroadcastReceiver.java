package com.vassar.unifiedapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.vassar.unifiedapp.ui.HomeActivity;

public class SyncCompleteBroadcastReceiver extends BroadcastReceiver{

    AppCompatActivity mHomeActivity = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mHomeActivity != null) {
//            ((HomeActivity) mHomeActivity).refreshAppListForUpdatedProjectCount();
        }
    }

    public void setContext(AppCompatActivity appCompatActivity) {
        mHomeActivity = appCompatActivity;
    }
}
