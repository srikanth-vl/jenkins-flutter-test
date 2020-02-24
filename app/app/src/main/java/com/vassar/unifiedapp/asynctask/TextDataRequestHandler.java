package com.vassar.unifiedapp.asynctask;

import android.os.AsyncTask;

import com.vassar.unifiedapp.api.AllProjectListService;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.synchronization.TextDataSyncReceiver;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;

public class TextDataRequestHandler extends AsyncTask<Void,Void,Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        Utils.logInfo(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "initiate form Text Submission sync");
        if (!Utils.getInstance().isOnline(null)) {
            Utils.logInfo(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "Cannot initiate form Text Submission sync without active internet connection");
            return null;
        } else if (Thread.currentThread().isInterrupted()) {
            Utils.logInfo(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "Cannot initiate form Text Submission sync");
            return null;
        }
        TextDataSyncReceiver.isTextSubmissionSyncThreadRunning = true;
        AllProjectListService allProjectListService = new AllProjectListService();
        try {
            Utils.logInfo(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "Started form Text Submission Upload");
            allProjectListService.uploadAllUnSyncedProjects();
            Utils.logInfo(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "Completed form Text Submission Upload");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UAException e) {
            e.printStackTrace();
        }
        TextDataSyncReceiver.isTextSubmissionSyncThreadRunning = true;

        return null;
    }
}
