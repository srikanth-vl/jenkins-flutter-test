package com.vassar.unifiedapp.api;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.asynctask.MediaRequestHandlerTask;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.model.ProjectSubmissionResult;
import com.vassar.unifiedapp.ui.ProjectFormActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.ProjectSubmissionConstants;
import com.vassar.unifiedapp.utils.ProjectSubmissionResultParser;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectSubmissionThread extends AsyncTask<Void, Void
        , ProjectSubmissionResult> {

    private ContentValues mContentValues;
    private List<String> mMediaUUids = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    private ProjectFormActivity activity;

    public ProjectSubmissionThread(ContentValues contentValues, List<String> uuids, ProjectFormActivity activity) {
        this.mContentValues = contentValues;
        this.mMediaUUids.addAll(uuids);
        this.activity = activity;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Utils.logInfo(LogTags.PROJECT_SUBMIT, "XXXX: RealTieme Project submission from FORM in onPreExecute -- ");
    }

    @Override
    protected void onPostExecute(ProjectSubmissionResult projectSubmissionResult) {

        ProjectSubmissionResultParser resultMessageParser = new ProjectSubmissionResultParser();

        if(projectSubmissionResult != null) {

            String messageToDisplayToUser = resultMessageParser.parseProjectSubmissionResult(projectSubmissionResult);
            activity.hideProgressBar();

            if(projectSubmissionResult.isSuccessful()) {
                createDialog(messageToDisplayToUser, true, false);
                MediaRequestHandlerTask mediaRequestHandlerTask = new MediaRequestHandlerTask();
                mediaRequestHandlerTask.execute();

            } else {
                if (projectSubmissionResult.getStatusCode() == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
                    // Validation Error
                    createDialog(messageToDisplayToUser, false, false);
                } else if(projectSubmissionResult.getStatusCode() == ProjectSubmissionConstants.TOKEN_EXPIRY_ERROR) {
                    // Token Expired while submission
                    createDialog(ProjectSubmissionConstants.TOKEN_EXPIRY_MESSAGE, false, true);
                }
                else {
                    //Other errors
                    createDialog(projectSubmissionResult.getMessage(), true, false);
                }
            }
        }

        super.onPostExecute(projectSubmissionResult);
    }

    @Override
    protected ProjectSubmissionResult doInBackground(Void... voids) {

        // Project Submission
        // 1. User takes media -> they go to media table with click_ts but no form_submission_ts
        // 2. User clicks on submit button

        // ONLINE
        // * Update form_submission_ts to all the media taken
        // * Data gets submitted to project submission table with form_submission_ts
        // * Validation Error from server -> then also update form_submission_ts (for retry in future)
        // OFFLINE
        // * Update form_submission_ts for all the media taken
        // * Data gets submitted to project submission table with form_submission_ts

        Utils.logInfo(LogTags.PROJECT_SUBMIT, "XXXX: RealTieme Project submission from FORM - inDoInBackground() -- ");

        Long submissionTimestamp = mContentValues.getAsLong(UnifiedAppDbContract.ProjectSubmissionEntry
                .COLUMN_PROJECT_SUBMISSION_TIMESTAMP);

        ContentValues mediaContentVal = new ContentValues();
        mediaContentVal.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP, submissionTimestamp);

        // Update submissionTimestamp for all the media entries in the DB
        UnifiedAppDBHelper dbHelper = UAAppContext.getInstance().getDBHelper();
        for (String uuid : mMediaUUids) {
            int status = dbHelper.updateFormMedia(mediaContentVal, uuid);
            if(status == Constants.DATABASE_INSERT_ERROR_CODE) {
                Utils.logError(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR, "Error while inserting in Media table ");
            }
        }

        // Add the submission to the DB
        long status = dbHelper.addOrUpdateProjectSubmission(mContentValues);
        if(status == Constants.DATABASE_INSERT_ERROR_CODE) {
            Utils.logError(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR, "Error while inserting in Project Submission table ");
        }

        if (!Utils.getInstance().isOnline(null)) {
            // Is not online
            ProjectSubmission projectSubmission = getProjectSubmissionObjectFromDBObject(mContentValues);
            Utils.logInfo(LogTags.PROJECT_SUBMIT, "XXXX: Server offline will submit later -- " + projectSubmission.getProjectId());
            return new ProjectSubmissionResult(Constants.DEFAULT_APP_ERROR_CODE, false,
                    ProjectSubmissionConstants.PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC);
        }

        NewSubmitProjectService newSubmitProjectService = new NewSubmitProjectService();
        ProjectSubmissionResult result = null;
        ProjectSubmission projectSubmission = getProjectSubmissionObjectFromDBObject(mContentValues);
        Utils.logInfo(LogTags.PROJECT_SUBMIT, "Project submission while calling submit in real time -- " + projectSubmission.toString());
        Utils.logInfo(LogTags.PROJECT_SUBMIT, "XXXX: RealTieme Project submission -- " + projectSubmission.getProjectId());
        result = newSubmitProjectService.uploadProjectInRealTime(projectSubmission);
        Utils.logInfo(LogTags.PROJECT_SUBMIT, "Result for submisison -- " + result.toString());
        return result;
    }

    private ProjectSubmission getProjectSubmissionObjectFromDBObject(ContentValues contentValues) {
        ProjectSubmission projectSubmission = new ProjectSubmission();
        if(contentValues == null) {
            return projectSubmission;
        }
        projectSubmission.setAppId(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID)));
        projectSubmission.setUserId(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID)));
        projectSubmission.setUserType(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE)));
        projectSubmission.setFormId(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID)));
        projectSubmission.setProjectId(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID)));
        projectSubmission.setTimestamp(getLongValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP)));
        projectSubmission.setUploadStatus(getIntValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS)));
        projectSubmission.setApi(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API)));
        projectSubmission.setMdInstanceId(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID)));
        projectSubmission.setFields(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS)));
        projectSubmission.setResponse(getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE)));
        projectSubmission.setLastServerUploadTimestamp(getLongValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS)));
        projectSubmission.setUploadRetryCount(getIntValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT)));

        String additionalProps = getStringValue(contentValues.get(UnifiedAppDbContract
                .ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES));

        Map<String, String> myMap = new HashMap<>();
        if(additionalProps != null && !additionalProps.isEmpty() ) {
            try {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                ObjectMapper mapper = new ObjectMapper();
                myMap = mapper.readValue(additionalProps, new TypeReference<Map <String, String>>() {});
            } catch (IOException e) {
                Utils.logError(LogTags.PROJECT_SUBMISSION_SERVICE, "failed to parse json String :: " + additionalProps);
                e.printStackTrace();
            }
        }

        projectSubmission.setAdditionalProps(myMap);

        return projectSubmission;
    }

    private String getStringValue(Object object) {
        if(object == null) {
            return null;
        }
        return String.valueOf(object);
    }

    private Long getLongValue(Object object) {
        if(object == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(object));
    }

    private Integer getIntValue(Object object) {
        if(object == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(object));
    }

    private void createDialog(String message, boolean shouldFinishActivity, boolean redirectToLogin) {
        // create a Dialog component
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        activity.enableUI();
        activity.allowBackPress = true;

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.project_submission_alert_dialog);

        TextView txt = (TextView) dialog.findViewById(R.id.project_submission_message);

        txt.setText(message);

        Button dialogButton = (Button) dialog.findViewById(R.id.project_submission_close);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                activity.dismissDialog(activity.DIALOG_LOADING);
                if(shouldFinishActivity) {
                    activity.finish();
                }
                if(redirectToLogin) {
                    activity.moveToLoginScreen();
                }
            }
        });

        dialog.show();
    }

}


