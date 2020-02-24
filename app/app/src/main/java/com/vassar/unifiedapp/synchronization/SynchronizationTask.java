package com.vassar.unifiedapp.synchronization;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

public class SynchronizationTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private String mCallingActivity;
    private static Object LOCK = new Object();

    public SynchronizationTask(Context context, String callingActivity) {
        this.mContext = context;
        this.mCallingActivity = callingActivity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Utils.getInstance().showLog("SYNC TASK STARTED", String.valueOf(System.currentTimeMillis()));

        if (Utils.getInstance().isOnline(mContext)) {
            synchronized (LOCK) {
                if (!SynchronizationService.mIsSynchronizationRunning) {
                    SynchronizationService.mIsSynchronizationRunning = true;
                    SynchronizationService synchronizationService = new SynchronizationService(mContext, mCallingActivity);
                    synchronizationService.initiateSynchronizationService();
                }
            }
        } else {
            Intent intent = new Intent(Constants.FAILED_SYNC_BROADCAST);
            intent.putExtra("message", mContext.getResources().getString(R.string.CHECK_INTERNET_CONNECTION));
            mContext.sendBroadcast(intent);
        }

        return null;
    }
}
