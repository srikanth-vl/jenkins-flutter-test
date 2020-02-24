package com.vassar.unifiedapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.vassar.unifiedapp.BuildConfig;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.ui.ProjectFormActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBObjectCreationUtils {

    // Creates a submission entry that can be submitted into the Submission table
    public ContentValues createFormSubmissionEntry(JSONArray jsonArray, FormButton formButton, String appId, String projectFormId, String mdInstanceId, Project project) {

        ContentValues values = new ContentValues();
        try {
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID
                    , appId);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID
                    , UAAppContext.getInstance().getUserID());
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE
                    , project.mUserType);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID
                    , projectFormId);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID
                    , project.mProjectId);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP
                    , System.currentTimeMillis());
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS
                    , ProjectSubmissionUploadStatus.UNSYNCED.getValue());
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API
                    , formButton.mApi);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID
                    , mdInstanceId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.SUBMISSION_FIELD_ARRAY, jsonArray);

            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS
                    , jsonObject.toString());
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE
                    , Constants.PROJECT_SUBMISSION_ENTRY_DEFAULT_SERVER_RESPONSE);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS
                    , Constants.PROJECT_SUBMISSION_ENTRY_DEFAULT_SERVER_SYNC_TS);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT
                    , Constants.PROJECT_SUBMISSION_ENTRY_DEFAULT_RETRY_COUNT);
            JSONObject additionalProperties = new JSONObject();
            String app_version = BuildConfig.VERSION_NAME;
            additionalProperties.put(Constants.SUBMISSION_APP_VERSION_KEY, app_version);

/// Add token to additional-properties
            String user_token_preference_key=UAAppContext.getInstance().getToken();
            additionalProperties.put(Constants.USER_TOKEN, user_token_preference_key);
            values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES, additionalProperties.toString());
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return values;
    }

    // Fetch list of media UUIDs from submission
    public List<String> getMediaUuidsFromSubmission(JSONArray jsonArray)
            throws JSONException {
        List<String> uuids = new ArrayList<>();
        for(int i = 0; i<jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if(jsonObject.has("ui") && jsonObject.getString("ui") != null && !jsonObject.getString("ui").isEmpty()) {
                String uiType = jsonObject.getString("ui");
                if (uiType.contains("image") || uiType.contains("video")) {
                    String uuidsForMediaField = jsonObject.getString("val");
                    uuids.addAll(Arrays.asList(uuidsForMediaField.split("\\s*,\\s*")));
                }
            }
        }
        return uuids;
    }

    /** Saves image meta data into the database (OutgoingImages table) */
    public void saveImageToFormDatabase(UnifiedAppDBHelper dbHelper, FormMedia image) {
        ContentValues values = new ContentValues();

        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UUID, image.getmUUID());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID, image.getmAppId());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID, image.getmProjectId());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID, image.getmUserId());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_TYPE, image.getMediaType());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_SUBTYPE, image.getMediaSubType());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP, image.getFormSubmissionTimestamp());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_EXTENSION, image.getMediaFileExtension());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_RETRIES, image.getMediaUploadRetries());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS, image.getMediaRequestStatus());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_CLICK_TS, image.getMediaClickTimeStamp());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LOCAL_PATH, image.getLocalPath());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_BITMAP, image.getmBitmap());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP, image.getMediaUploadTimestamp());
        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_ACTION_TYPE, MediaActionType.UPLOAD.getValue());

        if (image.ismHasGeotag()) {
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_HAS_GEOTAG, true);
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE, image.getLatitude());
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE, image.getLongitude());
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY, image.getAccuracy());
        } else {
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_HAS_GEOTAG, false);
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE
                    , Constants.DEFAULT_LATITUDE);
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE, Constants.DEFAULT_LONGITUDE);
            values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY, Constants.DEFAULT_ACCURACY);
        }

        String additionalProps = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            additionalProps = objectMapper.writeValueAsString(image.getAdditionalProps());
        } catch (IOException e) {
            Utils.logError(LogTags.MEDIA_THREAD, "Failed to create Json String from Object");
            e.printStackTrace();
        }

        values.put(UnifiedAppDbContract.FormMediaEntry.COLUMN_ADDITIONAL_PROPERTIES, additionalProps);
        dbHelper.addToDatabase(UnifiedAppDbContract.FormMediaEntry.TABLE_FORM_MEDIA, values);
    }

    /** Saves the project fields to the database */
    private void saveProjectToDatabase(UnifiedAppDBHelper dbHelper, JSONArray jsonArray, FormButton formButton, String appId, String projectFormId, String mdInstanceId, Project project) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID
                , appId);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID
                , UAAppContext.getInstance().getUserID());
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE
                , project.mUserType);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID
                , projectFormId);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID
                , project.mProjectId);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP
                , System.currentTimeMillis());
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS
                , ProjectSubmissionUploadStatus.UNSYNCED.getValue());
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API
                , formButton.mApi);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID
                , mdInstanceId);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.SUBMISSION_FIELD_ARRAY, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS
                , jsonObject.toString());
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE
                , "");
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS
                , 0);
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT
                , 0);
        dbHelper.addToDatabase(UnifiedAppDbContract.ProjectSubmissionEntry.TABLE_PROJECT_SUBMISSION, values);
    }
    /** Saves the project created by user to the database */
    public ContentValues addNewProject(String appId, String userId, Project project,
                                   List<String> filteringAttributes, List<String> groupingAttributes) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID, appId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID, userId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID, project.mProjectId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_NAME, project.mProjectName);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LON, project.mLongitude);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAT, project.mLatitude);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_BBOX, project.mBBoxValidation);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_CIRCLE_VALIDATION, project.mCentroidValidation);
        project.mLastSubDate = null;
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAST_SUB_DATE, project.mLastSubDate);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_STATE, project.mState);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_EXTERNAL_PROJECT_ID, project.mExtProjectId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAST_UPDATED_TS, project.mLastSyncTimestamp);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS, project.mAssigned);
        List<ProjectListFieldModel> fields = project.mFields;
        if (fields != null && fields.size() > 0) {
            Type type = new TypeToken<List<ProjectListFieldModel>>() {}.getType();
//            String fieldsString = new Gson().toJson(fields, type);
            try {
                String fieldsString = new ObjectMapper().writeValueAsString(fields);
                contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_FIELDS, fieldsString);
            } catch (JsonProcessingException e) {
                Utils.logError(LogTags.DB_OBJECT_CREATION_UTILS,"failed to create json from Object :: " + fields.toString());
                e.printStackTrace();
            }
        }

        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_TYPE, project.mUserType);
        if (project.mValidation != null) {
//            String validationObjectString = new Gson().toJson(project.mValidation);
            try {
                String validationObjectString = new ObjectMapper().writeValueAsString(project.mValidation);
                contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_VALIDATIONS, validationObjectString);
            } catch (JsonProcessingException e) {
                Utils.logError(LogTags.DB_OBJECT_CREATION_UTILS,"failed to create json from Object :: " + project.mValidation.toString());
                e.printStackTrace();
            }
        }
        if (project.mProjectIcon != null) {
//            String projectIconObjectString = new Gson().toJson(project.mProjectIcon);
            try {
                String projectIconObjectString = new ObjectMapper().writeValueAsString(project.mProjectIcon);
                contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ICON, projectIconObjectString);
            } catch (JsonProcessingException e) {
                Utils.logError(LogTags.DB_OBJECT_CREATION_UTILS,"failed to create json from Object :: " + project.mProjectIcon.toString());
                e.printStackTrace();
            }
        }
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_SERVER_SYNC_TS, project.mLastSyncTimestamp);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_SHOW_MAP, false);

        if(project.mAttributes != null) {
            String filteringDimensionValues = getHashSeparatedValues(filteringAttributes, project.mAttributes);
            String groupingDimensionValues = getHashSeparatedValues(groupingAttributes, project.mAttributes);
            contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES, filteringDimensionValues);
            contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES, groupingDimensionValues);
        }
       return  contentValues;
    }

    private String getHashSeparatedValues(List<String> attributes, Map<String, String> projectListAttributesMap) {
        String dimensionValues = "";
        if(attributes != null && !attributes.isEmpty()) {
            String[] valuesList = new String[projectListAttributesMap.size()];
            for (Map.Entry<String, String> entry : projectListAttributesMap.entrySet()) {
                int index = attributes.indexOf(entry.getKey());
                if(index != -1) {
                    valuesList[index] = entry.getValue().trim().toLowerCase();
                }
            }
            for(int i = 0; i < projectListAttributesMap.size(); i++) {
                if (valuesList[i] != null && valuesList.length != 0) {
                    dimensionValues += valuesList[i] + "#";
                }
            }
        }
        if(!dimensionValues.isEmpty() && dimensionValues.charAt(dimensionValues.length() - 1) == '#') {
            dimensionValues = dimensionValues.substring(0, dimensionValues.length()-1);
        }
        return dimensionValues;
    }

}
