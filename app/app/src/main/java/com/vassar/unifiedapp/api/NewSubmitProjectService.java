package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Intent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.asynctask.TextDataRequestHandler;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.ProjectSubmissionException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.model.ProjectSubmissionResult;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.synchronization.TextDataSyncReceiver;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaRequestStatus;
import com.vassar.unifiedapp.utils.ProjectSubmissionConstants;
import com.vassar.unifiedapp.utils.ProjectSubmissionUploadStatus;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NewSubmitProjectService {

    private UnifiedAppDBHelper mDbHelper;
    private ObjectMapper mObjectMapper = new ObjectMapper();
    public static final MediaType JSON
            = MediaType.get("application/json");

    public NewSubmitProjectService() {
        mObjectMapper = new ObjectMapper();
        mDbHelper = UAAppContext.getInstance().getDBHelper();
    }

    // uplaod project in background sync
    public boolean callProjectSubmitService(ProjectSubmission projectSubmission)
            throws UAException {

        // Logic
        // 1. Upload to server
        // 2. Update Status in DB
        // 3. In case of error - Update Status in DB with error details
        int projectStatus = UAAppContext.getInstance().getDBHelper().getProjectSubmissionStatus(
                projectSubmission.mAppId, projectSubmission.mUserId,
                projectSubmission.mProjectId, projectSubmission.mTimestamp);
        ProjectSubmissionResult result ;
        if (!(projectStatus == ProjectSubmissionUploadStatus.UNSYNCED.getValue()
                || projectStatus == ProjectSubmissionUploadStatus.SERVER_ERROR.getValue())) {
            return true;
        }

        try {
            result = uploadToServer(projectSubmission);
            ProjectSubmissionUploadStatus uploadStatus;
            if(result.isSuccessful()) {
                uploadStatus = ProjectSubmissionUploadStatus.SYNCED;
            } else {
                if (result.getStatusCode() == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
                    uploadStatus = ProjectSubmissionUploadStatus.VALIDATION_ERROR;
                    updateMediaStatusToFailed(projectSubmission);
                } else if (result.getStatusCode() == ProjectSubmissionConstants.APP_VERSION_MISMATCH_ERROR) {
                    uploadStatus = ProjectSubmissionUploadStatus.APP_VERSION_MISMATCH_ERROR;
                    updateMediaStatusToFailed(projectSubmission);
                } else if(result.getStatusCode() == ProjectSubmissionConstants.PROJECT_DELETED_ERROR) {
                    uploadStatus = ProjectSubmissionUploadStatus.DELETED;
                    updateMediaStatusToFailed(projectSubmission);
                } else {
                    uploadStatus = ProjectSubmissionUploadStatus.SERVER_ERROR;
                }
            }
            updateSyncStatus(projectSubmission, uploadStatus, result, System.currentTimeMillis());
            Utils.logDebug(LogTags.PROJECT_SUBMIT, "Project Submission  : Uploaded successfully -- " + result);
            return true;
        } catch (UAException e) {
            Utils.logError(LogTags.PROJECT_SUBMIT, "Project Submission  : Upload failed -- " + projectSubmission.toString(), e);
            return false;
        }
    }

    private ProjectSubmissionResult uploadToServer(ProjectSubmission projectSubmission)
            throws UAException   {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Response response = null;
        RequestBody body = RequestBody.create(JSON, createRequestParams(projectSubmission));
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + projectSubmission.mApi)
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error uploading ProjectSubmission to server -- " + projectSubmission.toString());
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error uploading ProjectSubmission to server -- " + projectSubmission.toString());
        }
        // Response code lies within 200-300
        String responseString = null;
        try {
            responseString = response.body().string();
            JSONObject jsonObject = new JSONObject(responseString);
            JSONObject resultObject = jsonObject.getJSONObject("result");

            if(resultObject == null || resultObject.toString().isEmpty() || resultObject.toString().equals("{}")) {
                // proper response not received from server
                Utils.logError(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR
                        , "Error uploading ProjectSubmission to server -- Project Submission -- " + projectSubmission.toString()
                                + " -- received responseString -- " + responseString);
                throw new ProjectSubmissionException(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR, "Error uploading ProjectSubmission to server -- Project Submission -- " + projectSubmission.toString()
                        + " -- received responseString -- " + responseString);
            } else {
                return new ProjectSubmissionResult(resultObject.getInt(Constants.PROJECT_RESPONSE_STATUS_CODE),
                        resultObject.getBoolean(Constants.PROJECT_RESPONSE_IS_SUCCESSFUL),
                        resultObject.getString(Constants.PROJECT_RESPONSE_MESSAGE));
            }

        } catch (Exception e) {
            if (e instanceof UAException) {
                throw (UAException) e;
            }
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR
                    ,"Error uploading ProjectSubmission to server -- Project Submission -- " + projectSubmission.toString()
                            + " -- received responseString -- " + responseString, e);
            e.printStackTrace();
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error uploading ProjectSubmission to server -- Project Submission -- " + projectSubmission.toString()
                    + " -- received responseString -- " + responseString, e);
        }
    }

    private int  updateSyncStatus(ProjectSubmission projectSubmission, ProjectSubmissionUploadStatus status, ProjectSubmissionResult result, Long serverSyncTimestamp) {
        String serverResponse = null;
        try {
            serverResponse = mObjectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            Utils.logError(LogTags.PROJECT_SUBMISSION_SERVICE,"could not process json :: " + result.toString());
            e.printStackTrace();
        }
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS, status.getValue());
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE, serverResponse);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS, serverSyncTimestamp);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT, projectSubmission.getUploadRetryCount()+1);
        int id = mDbHelper.updateProjectSubmission(projectSubmission.mUserId, projectSubmission.mAppId
                , projectSubmission.mProjectId, projectSubmission.mTimestamp, values);

        Utils.logInfo(LogTags.PROJECT_SUBMIT, " -- Updated the sync status for the project -- "
                + projectSubmission.toString() + " -- status -- " + status + " -- serverResponse -- " + serverResponse);
        return id;
    }

    private String createRequestParams(ProjectSubmission projectSubmission)
            throws UAException {
        JSONObject requestObject = new JSONObject();
        JSONArray submitArray = new JSONArray();
        JSONObject params = new JSONObject();

        try {
            JSONObject projectSubmissionObject = new JSONObject();
            projectSubmissionObject.put(Constants.PROJECT_SUBMIT_FORM_ID_KEY, projectSubmission.mFormId);
            projectSubmissionObject.put(Constants.PROJECT_SUBMIT_MD_INSTANCE_ID_KEY, projectSubmission.mMdInstanceId);
            projectSubmissionObject.put(Constants.PROJECT_SUBMIT_PROJECT_ID_KEY, projectSubmission.mProjectId);
            projectSubmissionObject.put(Constants.PROJECT_SUBMIT_USER_TYPE_KEY, projectSubmission.mUserType);
            projectSubmissionObject.put(Constants.PROJECT_SUBMIT_INSERT_TS_KEY, projectSubmission.mTimestamp);
            Map <String, String > additionalProperties= projectSubmission.getAdditionalProps();
    // get token from additional properties if available else form user_meta table;
            String token  = null;
            if(additionalProperties != null && additionalProperties.get(Constants.USER_TOKEN) != null && !additionalProperties.get(Constants.USER_TOKEN).isEmpty()) {
                token = additionalProperties.get(Constants.USER_TOKEN);
                additionalProperties.remove(Constants.USER_TOKEN);
            } else {
               UserMetaData userMetaData =  mDbHelper.getUserMeta(projectSubmission.mUserId);
               if(userMetaData != null) {
                    token = userMetaData.mToken;
               }
            }
            JSONObject additionPropertiesJson = new JSONObject();
            for (String key : additionalProperties.keySet()) {
                additionPropertiesJson.put(key, additionalProperties.get(key));
            }
            projectSubmissionObject.put(Constants.PROJECT_ADDITIONAL_PROPERTIES,  additionPropertiesJson);
            JSONObject jsonObject = new JSONObject(projectSubmission.mFields);
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.PROJECT_SUBMIT_FIELDS_KEY);
            projectSubmissionObject.put(Constants.PROJECT_SUBMIT_FIELDS_KEY, jsonArray);
            submitArray.put(projectSubmissionObject);
            requestObject.put(Constants.PROJECT_SUBMIT_SUBMIT_DATA_KEY, submitArray);
            requestObject.put(Constants.PROJECT_SUBMIT_SUPER_APP_KEY, Constants.SUPER_APP_ID);
            requestObject.put(Constants.PROJECT_SUBMIT_USER_ID_KEY, projectSubmission.mUserId);
//            requestObject.put(Constants.PROJECT_SUBMIT_TOKEN_KEY, UAAppContext.getInstance().getToken());
            requestObject.put(Constants.PROJECT_SUBMIT_TOKEN_KEY, token);
            requestObject.put(Constants.PROJECT_SUBMIT_APP_ID_KEY, projectSubmission.mAppId);
            params.put(Constants.PROJECT_SUBMISSION_PARAMETER_KEY, requestObject.toString());
        } catch (JSONException e) {
            Utils.logError(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR
                    ,"Error creating parameters -- Project Submission -- " + projectSubmission.toString(), e);
            throw new ServerFetchException(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR
                    , "Error creating parameters -- Project Submission -- " + projectSubmission.toString(), e);
        }

        Utils.logInfo(LogTags.PROJECT_SUBMIT, " -- Request Parameters have been created for " +
                "ProjectSubmission -- " + projectSubmission.toString() + " -- Parameters -- " + params);

        return params.toString();
    }

    public ProjectSubmissionResult uploadProjectInRealTime(ProjectSubmission projectSubmission) {

        // Check if connection available, if not return
        //    the project will be submitted in background sync
        //
        // Get AppBackgroundSync.getSyncLock()
        // Lock on SYNC_LOCK
        // Check projectSubmission status in DB
        //     if uploaded then return
        //    else upload now
        // Release SYNC_LOCK Object

        if (!Utils.getInstance().isOnline(null)) {
            // Is not online
            return new ProjectSubmissionResult(Constants.DEFAULT_APP_ERROR_CODE, false,
                    ProjectSubmissionConstants.PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC);
        }

        Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In RealTime upload : Waiting for Lock : "  + projectSubmission.getProjectId());

        Object SYNC_LOCK = TextDataSyncReceiver.getSyncLock();
        synchronized (SYNC_LOCK) {
            Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In RealTime upload : After getting Lock : "  + projectSubmission.getProjectId());
            int projectStatus = UAAppContext.getInstance().getDBHelper().getProjectSubmissionStatus(
                    projectSubmission.mAppId, projectSubmission.mUserId,
                    projectSubmission.mProjectId, projectSubmission.mTimestamp);

            String message;
            boolean isSuccess;
            if (projectStatus != ProjectSubmissionUploadStatus.UNSYNCED.getValue()) {
                if(projectStatus == ProjectSubmissionUploadStatus.SYNCED.getValue()
                        || projectStatus == ProjectSubmissionUploadStatus.SYNCED_WITH_MEDIA.getValue()) {
                    isSuccess = true;
                    message = ProjectSubmissionConstants.PROJECT_SUBMITTED_SUCCESSFULLY;
                } else if(projectStatus == ProjectSubmissionUploadStatus.VALIDATION_ERROR.getValue()) {
                    isSuccess = false;
                    message = ProjectSubmissionConstants.PROJECT_SUBMISSION_VALIDATION_ERROR;
                } else {
                    isSuccess = false;
                    message = ProjectSubmissionConstants.PROJECT_SUBMISSION_FAILED;
                }
                return new ProjectSubmissionResult(Constants.DEFAULT_APP_ERROR_CODE, isSuccess, message);
            }

            ProjectSubmissionResult result = null;
            try {
                result = uploadToServer(projectSubmission);
                ProjectSubmissionUploadStatus uploadStatus;
                if(result.isSuccessful()) {
                    uploadStatus = ProjectSubmissionUploadStatus.SYNCED;
                } else {
                    if (result.getStatusCode() == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
                        uploadStatus = ProjectSubmissionUploadStatus.VALIDATION_ERROR;
                        updateMediaStatusToFailed(projectSubmission);
                    } else if (result.getStatusCode() == ProjectSubmissionConstants.APP_VERSION_MISMATCH_ERROR) {
                        uploadStatus = ProjectSubmissionUploadStatus.APP_VERSION_MISMATCH_ERROR;
                        updateMediaStatusToFailed(projectSubmission);
                    } else if(result.getStatusCode() == ProjectSubmissionConstants.PROJECT_DELETED_ERROR) {
                        uploadStatus = ProjectSubmissionUploadStatus.DELETED;
                        updateMediaStatusToFailed(projectSubmission);
                    } else {
                        uploadStatus = ProjectSubmissionUploadStatus.FAILED;
                    }
//                    if (result.getStatusCode() == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
//                        uploadStatus = ProjectSubmissionUploadStatus.VALIDATION_ERROR;
//                    } else {
//                        uploadStatus = ProjectSubmissionUploadStatus.FAILED;
//                    }
                }
                updateSyncStatus(projectSubmission, uploadStatus, result, System.currentTimeMillis());

                Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In RealTime upload : After submitting project : "  + projectSubmission.getProjectId());
                Utils.logDebug(LogTags.PROJECT_SUBMIT, "Project Submission  : Uploaded successfully -- " + result);
                return result;
            } catch (UAException e) {

                if(result != null) {
                    result.setStatusCode(UAAppErrorCodes.PROJECT_SUBMISSION_FAILED_ERROR);
                    result.setMessage(ProjectSubmissionConstants.PROJECT_SUBMISSION_FAILED);
                    result.setSuccessful(false);
                    updateSyncStatus(projectSubmission, ProjectSubmissionUploadStatus.FAILED, result, System.currentTimeMillis());
                } else {
                    // Server is down
                    result = new ProjectSubmissionResult();
                    result.setStatusCode(UAAppErrorCodes.PROJECT_SUBMISSION_FAILED_ERROR);
                    result.setMessage(ProjectSubmissionConstants.PROJECT_SUBMITTED_OFFLINE);
                    result.setSuccessful(false);
                }

                Utils.logError(LogTags.PROJECT_SUBMIT, "Project Submission  : Upload failed -- " + projectSubmission.toString());
                return result;
            } finally {
                Intent intent = new Intent(Constants.POST_SYNC_BROADCAST_ACTION);
                intent.putExtra("completed", true);
                UAAppContext.getInstance().getContext().sendBroadcast(intent);
                Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In RealTime upload : After releasing Lock : " + projectSubmission.getProjectId());
            }
        }
    }
    public void updateMediaStatusToFailed(ProjectSubmission submissionData) {
        Long formSubmissionTimestamp = submissionData.getTimestamp();
        String projectId = submissionData.getProjectId();
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS, MediaRequestStatus.FAILURE_STATUS.getValue());
        mDbHelper.updateFormMediaForProjectAtGivenTimestamp(values, projectId, formSubmissionTimestamp);
    }
}
