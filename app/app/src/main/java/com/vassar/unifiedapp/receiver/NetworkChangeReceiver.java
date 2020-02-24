package com.vassar.unifiedapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.vassar.unifiedapp.asynctask.ForceUpdateAsync;
import com.vassar.unifiedapp.synchronization.SynchronizationService;
import com.vassar.unifiedapp.utils.Utils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    final int DELAY = 2000;
    Context mContext;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        this.mContext=context;

        try
        {
            if (isOnline(context)) {
                Log.d("NetworkStatus", "Online Connect Intenet ");
                checkForForceUpdate(mContext);
            } else {
                Log.d("NetworkStatus", "Conectivity Failure !!! ");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Utils.getInstance().showLog("NetworkChangeReceiver", "INVOKED");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initiateService(context);
            }
        }, DELAY);
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initiateService(Context context) {
        if (Utils.getInstance().isOnline(context)) {
            if (!SynchronizationService.mIsSynchronizationRunning) {
//                Intent intent = new Intent("pre.sync.update.receiver");
//                context.sendBroadcast(intent);
            }
        }
    }

    public void checkForForceUpdate(Context context){
        String currentVersion="";
        try {
            currentVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            Log.d("Current Version","::"+currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //Initialisation of AsyncTask class
        //ForceUpdateAsync.getInstance(mContext, currentVersion).execute();
        new ForceUpdateAsync(context,currentVersion).execute();
    }

}
