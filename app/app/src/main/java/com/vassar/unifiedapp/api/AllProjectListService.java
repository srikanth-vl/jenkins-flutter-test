package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.synchronization.TextDataSyncReceiver;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.ProjectSubmissionConstants;
import com.vassar.unifiedapp.utils.ProjectSubmissionUploadStatus;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class AllProjectListService {

    private UnifiedAppDBHelper mDbHelper;
    public AllProjectListService() {
        mDbHelper = UAAppContext.getInstance().getDBHelper();
    }

    //to sync all submissions irrespective of user
    public boolean uploadAllUnSyncedProjects() throws JSONException, UAException {
        List<ProjectSubmissionUploadStatus> uploadStatusList = new ArrayList<>();
        uploadStatusList.add(ProjectSubmissionUploadStatus.UNSYNCED);
        uploadStatusList.add(ProjectSubmissionUploadStatus.SERVER_ERROR);

        //List<ProjectSubmission> unSyncedProjectSubmission = mDbHelper.getProjectsToSubmit(userId, appId, uploadStatusList);
        List<ProjectSubmission> unSyncedProjectSubmission = mDbHelper.getAllProjectsToSubmit(uploadStatusList);

        boolean uploadStatus = true;
        Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Projects on local DB to update is" + unSyncedProjectSubmission.size());

        if (unSyncedProjectSubmission == null || unSyncedProjectSubmission.isEmpty()) {
            return true;
        }

        AppMetaData appMetaConfig = UAAppContext.getInstance().getAppMDConfig();
//        Long serverErrorSyncFrequency = (appMetaConfig.mServiceFrequency == null
//                || appMetaConfig.mServiceFrequency.get(Constants.SERVICE_FREQUENCY_SERVER_ERROR) == null ) ? ProjectSubmissionConstants.DEFAULT_RETRY_FREQUENCY: appMetaConfig.mServiceFrequency.get(Constants.SERVICE_FREQUENCY_SERVER_ERROR);
        int retryCount = appMetaConfig.mRetries == 0 ? ProjectSubmissionConstants.DEFAULT_SUBMISSION_RETRIES : appMetaConfig.mRetries ;

        NewSubmitProjectService pss = new NewSubmitProjectService();
int i = 0;
        for (ProjectSubmission submissionData : unSyncedProjectSubmission) {

           // Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Project Submit Status for appId: " +appId + " , userId:" + userId + "ProjectId: "+ submissionData.getProjectId() + " :: " + submissionData.getUploadStatus());

            if(submissionData.getUploadStatus() == ProjectSubmissionUploadStatus.SYNCED.getValue()
                    || submissionData.getUploadStatus() == ProjectSubmissionUploadStatus.FAILED.getValue()) {
                continue;
            }

            if (submissionData.getUploadStatus() == ProjectSubmissionUploadStatus.SERVER_ERROR.getValue()
                    && submissionData.getUploadRetryCount() > retryCount) {
                setProjectStatusAsFailed(submissionData);
                continue;
            }
i++;
            Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: Uploading entry "  + i);
            Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : Before Lock "  + submissionData.getProjectId());
            synchronized (TextDataSyncReceiver.getSyncLock()) {
                Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : After Lock " + submissionData.getProjectId());
                Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Submitting project ");
                uploadStatus = pss.callProjectSubmitService(submissionData);
                Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : After Upload " + submissionData.getProjectId());
                Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: Uploaded entry "  + i);
            }
            Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : After Lock Release " + submissionData.getProjectId());

            // if upload failed stop execution
            if(!uploadStatus) {
                return false;
            }
        }
        //Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Successfully uploaded projects for appId: " +appId + " , userId:" + userId);
        return true;
    }
    private void setProjectStatusAsFailed(ProjectSubmission submissionData) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS,
                ProjectSubmissionUploadStatus.FAILED.getValue());
        mDbHelper.updateProjectSubmission(submissionData.getmUserId(), submissionData.getAppId(), submissionData.getProjectId(), submissionData.getTimestamp(), values);
    }
}
