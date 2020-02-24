package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.model.ProjectSpecificForms;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MediaRequestStatus;
import com.vassar.unifiedapp.utils.ProjectSubmissionConstants;
import com.vassar.unifiedapp.utils.ProjectSubmissionUploadStatus;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ProjectListService {

    private Context mContext;
    private UnifiedAppDBHelper mDbHelper;
    private SharedPreferences mAppPreferences;
    private ObjectMapper mJsonObjectMapper;
    public ProjectListService() {
        mDbHelper = UAAppContext.getInstance().getDBHelper();
        mContext = UAAppContext.getInstance().getContext();
        mAppPreferences = UAAppContext.getInstance().getAppPreferences();
        mJsonObjectMapper = new ObjectMapper();
    }

    public static final MediaType JSON
            = MediaType.get("application/json");

    // Create request parameters for projectList Services
    private String createRequestParams(ProjectTypeConfiguration projectTypeConfiguration, List<String> projectIds) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.PROJECT_LIST_CONFIG_USER_ID_KEY, UAAppContext.getInstance().getUserID());
            jsonObject.put(Constants.PROJECT_LIST_CONFIG_TOKEN_KEY, UAAppContext.getInstance().getToken());
            jsonObject.put(Constants.PROJECT_LIST_CONFIG_SUPER_APP_KEY, Constants.SUPER_APP_ID);
            jsonObject.put(Constants.PROJECT_LIST_CONFIG_APP_ID_KEY, projectTypeConfiguration.mProjectTypeId);

            // Fetching MetaDataInstanceId
            JSONArray mdInstanceIds = new JSONArray();
            Map<String, Map<String, ProjectSpecificForms>> projectSpecificForms
                    = projectTypeConfiguration.mContent;
            Iterator iterator = projectSpecificForms.keySet().iterator() ;
            while (iterator.hasNext())   {
                String key =  (String) iterator.next();
                Map<String, ProjectSpecificForms> actionForms = projectSpecificForms.get(key);
                Iterator actionIterator = actionForms.keySet().iterator() ;
                while (actionIterator.hasNext())   {
                    String actionKey =  (String) actionIterator.next();
                    ProjectSpecificForms form = actionForms.get(actionKey);
                    String mdInstanceId = form.mMetaDataInstanceId;
                    mdInstanceIds.put(mdInstanceId);
                }
            }

            if (projectIds == null) {
                jsonObject.put(Constants.PROJECT_LIST_CONFIF_PROJECT_ID_LIST_KEY
                        , projectIds);
            } else {
                JSONArray projectIdsToSend = new JSONArray();
                int i= 0;
                for (String projectId : projectIds) {
                        i++;
                    projectIdsToSend.put(projectId);
                    if(i == 4000) {
                        break;
                    }
                }
                jsonObject.put(Constants.PROJECT_LIST_CONFIF_PROJECT_ID_LIST_KEY
                        , projectIdsToSend);
            }

            jsonObject.put(Constants.PROJECT_LIST_CONFIG_MD_INSTANCE_ID_KEY
                    , mdInstanceIds);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public ProjectList callProjectListService(ProjectTypeConfiguration projectTypeConfiguration, List<String> projectIds)
            throws AppCriticalException, ServerFetchException {

        // Logic
        // 1. Fetch from Server
        // 2. Save to DB
        // 3. Return the ProjectList
        // 4. In case of error - return the error type

        String content = fetchFromServer(projectTypeConfiguration, projectIds);
        if (content == null) {
            return null;
        }
        ProjectList projectList = null;
        // Add new ProjectList to the App Context
//        Gson mGson = new Gson();
//        ProjectList projectList = mGson.fromJson(content.toString(), ProjectList.class);
        try{
            projectList = mJsonObjectMapper.readValue(content.toString(), ProjectList.class);
            storeToDB(projectList, projectTypeConfiguration);
        } catch (IOException e) {
            Utils.logError(LogTags.PROJECT_LIST_CONFIG, "Could not create ProjectList object from json object :: " + content);
            e.printStackTrace();
        }
        return projectList;
    }

    private String fetchFromServer(ProjectTypeConfiguration projectTypeConfiguration, List<String> projectIds)
            throws AppCriticalException, ServerFetchException {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Response response = null;

        String requestParam = createRequestParams(projectTypeConfiguration, projectIds);
        if (requestParam == null || requestParam.isEmpty()) {
            Utils.logError(LogTags.PROJECT_LIST_CONFIG, "Error creating request parameters - Project List Config");
            throw new AppCriticalException(UAAppErrorCodes.PROJECT_LIST_FETCH, "Error creating request parameters - Project List Config");
        }

        RequestBody body = RequestBody.create(JSON, requestParam);
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "projectlist")
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error getting ProjectList from server");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting ProjectList from server");
        }

        // Response code lies within 200-300
        String responseString = null;
        JSONObject content = null;
        int code = 0;
        try {
            responseString = response.body().string();
            JSONObject jsonObject = new JSONObject(responseString);
            JSONObject resultObject = jsonObject.getJSONObject("result");
            code = resultObject.getInt("status");
            if (code == 200) {
                content = resultObject.getJSONObject("content");
                if (content.toString().equals("{}")) {
                    // Same version on the server
                    Utils.logDebug(LogTags.PROJECT_LIST_SYNC, "ProjectList : No Projects found");
                    return null;
                } else {
                    Utils.logDebug(LogTags.PROJECT_LIST_SYNC, "ProjectList : " + content);
                    return content.toString();
                }
            }else if (code == 350) {

                Utils.logError(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Token is expired for this user");

                if (AppBackgroundSync.isSyncInProgress) {
                    Thread.currentThread().interrupt();
                }

                SharedPreferences.Editor editor = mAppPreferences.edit();
                editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY
                        , Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);
                editor.apply();

                Intent intent = new Intent(Constants.LOGOUT_UPDATE_BROADCAST);
                UAAppContext.getInstance().getContext().sendBroadcast(intent);

                if (content != null) {
                    return content.toString();
                } else {
                    return null;
                }

            } else {
                Utils.logError(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting ProjectList from server (invalid data) : response code : " + code
                                + " received content : " + responseString.toString());
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting ProjectList from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting ProjectList from server -"
                            + " received content : " + content);
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting ProjectList from server");
        } catch (Exception e) {
            if (e instanceof UAException) {
                try {
                    throw e;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR
                    ,"Error getting ProjectList from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting ProjectList from server :"
                    + " received responseString : " + responseString);
        }
    }

    private void storeToDB(ProjectList projectList, ProjectTypeConfiguration projectTypeConfiguration) {
        // Add new ProjectList to the DB
        String appId = projectTypeConfiguration.mProjectTypeId;
        String userId =  projectTypeConfiguration.mUserId;
        Map<String, Long> projectIdToTs = mDbHelper.getProjectIdToLastSyncTsMap(userId, appId, true);
        // get rootConfig  for user
        ConfigFile rootConfigFile = mDbHelper.getConfigFile(projectList.mUserId, Constants.ROOT_CONFIG_DB_NAME);
        String rootConfigString = rootConfigFile != null && rootConfigFile.getConfigContent() != null ? rootConfigFile.getConfigContent() : null;
        if (rootConfigFile != null) {
            RootConfig rootConfig = null;
            try {
               rootConfig = mJsonObjectMapper.readValue(rootConfigString.toString(), RootConfig.class);
            } catch (IOException e) {
                Utils.logError(LogTags.PROJECT_LIST, "Could not create RootConfig object from json object :: " + rootConfigString);
                e.printStackTrace();
            }

            List<String> filteringAttributes = new ArrayList<>();
            List<String> groupingAttributes = new ArrayList<>();
            if (rootConfig != null && rootConfig.mApplications != null && rootConfig.mApplications.size() > 0) {
                for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
                    if (projectTypeModel.mAppId.equals(appId)) {
                        if (projectTypeModel.mFilteringAttributes != null && projectTypeModel.mFilteringAttributes.size() > 0) {
                            filteringAttributes.addAll(projectTypeModel.mFilteringAttributes);
                        }
                        if (projectTypeModel.mGroupingAttributes != null && projectTypeModel.mGroupingAttributes.size() > 0) {
                            groupingAttributes.addAll(projectTypeModel.mGroupingAttributes);
                        }
                        break;
                    }
                }
                //  Iterate through all the project in list and store in DB
                for (Project project : projectList.mProjects) {
                    if (project != null) {
                        writeProjectToDb(appId, userId, project, projectList.mLastSyncTime, projectList.mShouldShowMap, filteringAttributes, groupingAttributes);
                    }
                }
            }
        }
    }

    private void writeProjectToDb(String appId, String userId, Project project, long lastServerTs,
                                  boolean shouldShowMap, List<String> filteringAttributes, List<String> groupingAttributes) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_APP_ID, appId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_ID, userId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ID, project.mProjectId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_NAME, project.mProjectName);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LON, project.mLongitude);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAT, project.mLatitude);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_BBOX, project.mBBoxValidation);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_CIRCLE_VALIDATION, project.mCentroidValidation);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAST_SUB_DATE, project.mLastSubDate);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_STATE, project.mState);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_EXTERNAL_PROJECT_ID, project.mExtProjectId);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_LAST_UPDATED_TS, project.mLastSyncTimestamp);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS, true);
        List<ProjectListFieldModel> fields = project.mFields;
        if (fields != null && fields.size() > 0) {
            Type type = new TypeToken<List<ProjectListFieldModel>>() {}.getType();
//            String fieldsString = new Gson().toJson(fields, type);
            try {
                String fieldsString = mJsonObjectMapper.writeValueAsString(fields);
                contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_FIELDS, fieldsString);
            } catch (IOException e) {
                Utils.logError(LogTags.PROJECT_LIST, "Could not create json object from " + fields.toString());
                e.printStackTrace();
            }
        }

        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_USER_TYPE, project.mUserType);
        if (project.mValidation != null) {
            try {
            String validationObjectString = mJsonObjectMapper.writeValueAsString(project.mValidation);
//                    new Gson().toJson(project.mValidation);
            contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_VALIDATIONS, validationObjectString);
            } catch (IOException e) {
                Utils.logError(LogTags.PROJECT_LIST, "Could not create json object from " + project.mValidation.toString());
                e.printStackTrace();
            }
        }
        if (project.mProjectIcon != null) {
            try {
                String projectIconObjectString = mJsonObjectMapper.writeValueAsString(project.mProjectIcon);
//                        new Gson().toJson(project.mProjectIcon);
                contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ICON, projectIconObjectString);
            } catch (IOException e) {
                Utils.logError(LogTags.PROJECT_LIST, "Could not create json object from " + project.mProjectIcon);
                e.printStackTrace();
            }
        }
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_SERVER_SYNC_TS, lastServerTs);
        contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_SHOW_MAP, shouldShowMap);

        if(project.mAttributes != null) {
            String filteringDimensionValues = getHashSeparatedValues(filteringAttributes, project.mAttributes);
            String groupingDimensionValues = getHashSeparatedValues(groupingAttributes, project.mAttributes);
            contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES, filteringDimensionValues);
            contentValues.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES, groupingDimensionValues);
        }
        mDbHelper.addOrUpdateToProjectTableWithTS(UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT, contentValues);
    }

    private String getHashSeparatedValues(List<String> attributes, Map<String, String> projectListAttributesMap) {
        String dimensionValues = "";
        if(attributes != null && !attributes.isEmpty()) {
            String[] valuesList = new String[projectListAttributesMap.size()];
            for (Map.Entry<String, String> entry : projectListAttributesMap.entrySet()) {
                int index = attributes.indexOf(entry.getKey());
                if(index != -1) {
                    if(entry.getValue() != null) {
                        valuesList[index] = entry.getValue().trim().toLowerCase();
                    }
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

    // Returns Map of Project ID to last sync timestamp from Server
    public Map<String, Long> fetchFromServerProjectIDToTSMap(ProjectTypeConfiguration projectTypeConfiguration)
            throws ServerFetchException {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Response response = null;
        RequestBody body = RequestBody.create(JSON, createRequestParams(projectTypeConfiguration, null));
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "projectlistwithts")
                .post(body)
                .build();

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error getting ProjectListWithTs from server");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting ProjectListWithTs from server");
        }

        // Response code lies within 200-300
        String responseString = null;
        JSONObject content = null;
        int code = 0;
        try {
            responseString = response.body().string();
            JSONObject jsonObject = new JSONObject(responseString);
            JSONObject resultObject = jsonObject.getJSONObject("result");
            code = resultObject.getInt("status");
            if (code == 200) {
                content = resultObject.getJSONObject("content");
                if (content == null || content.toString().equals("{}")) {
                    // Same version on the server
                    Utils.logDebug(LogTags.PROJECT_LIST, "ProjectListWithTs : ProjectId List is Empty");
                    return null;
                }

                JSONObject projectToTsString = content.getJSONObject("projects");
                Map<String, Long> pidToTSMap = new HashMap<>();
                // if map is empty no project found for user
                if(projectToTsString != null && projectToTsString.toString().equals("{}"))
                    return pidToTSMap;
                try {
                    Iterator<String> iterator = projectToTsString.keys();

                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        String value = projectToTsString.getString(key);

                        pidToTSMap.put(key, Long.parseLong(value));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return pidToTSMap;

            } else if (code == 350) {

                Utils.logError(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Token is expired for this user");

                if (AppBackgroundSync.isSyncInProgress) {
                    Thread.currentThread().interrupt();
                }

                SharedPreferences.Editor editor = mAppPreferences.edit();
                editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY
                        , Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);
                editor.apply();

                Intent intent = new Intent(Constants.LOGOUT_UPDATE_BROADCAST);
                UAAppContext.getInstance().getContext().sendBroadcast(intent);

                return null;

            } else {
                Utils.logError(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting ProjectListWithTS from server (invalid data) : response code : " + code
                                + " received content : " + content.toString());
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting ProjectListWithTS from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting ProjectListWithTS from server -"
                            + " received content : " + content.toString());
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting ProjectListWithTS from server");
        } catch (Exception e) {
            if (e instanceof UAException) {
                try {
                    throw e;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR
                    ,"Error getting ProjectListWithTS from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting ProjectListWithTS from server :"
                    + " received responseString : " + responseString);
        }
    }

    // Handles project sync - Order of execution is important
    public void handleProjectSync(String appId, ProjectTypeConfiguration ptc)
            throws UAException, JSONException {

        String userId = UAAppContext.getInstance().getUserID();

        //Stage 1 Uploads newer version of the project to the server
//        boolean synced = uploadUnSyncedProjects(userId, appId);
//        if(!synced) {
//            Utils.logError(LogTags.PROJECT_LIST_SYNC, "Error uploading Projects to server for appId: " +appId + " , userId:" + userId);
//            return;
//        }

        Map<String, Long> pidToTSMap = fetchFromServerProjectIDToTSMap(ptc);

        Map<String, Long> existingAssignedProjectIdToTs = mDbHelper.getProjectIdToLastSyncTsMap(userId, appId, true);
        Map<String, Long> existingAllProjectIdToTs = mDbHelper.getProjectIdToLastSyncTsMap(userId, appId, false  );

        // Stage 2 - Identify projects to sync based on timestamp from server
        if(pidToTSMap == null) {
            return;
        }
        List<String> projectsToDownload = filterProjectsToDownload(existingAssignedProjectIdToTs, pidToTSMap);
        List<String> projectsToDelete = filterProjectsToDelete(userId, appId, existingAllProjectIdToTs, pidToTSMap);

        // Stage 3 - Sync projects with server
        // 1. Downloads newer version of the project from the server
        // 2. Deletes unassigned projects for the user

        boolean downloaded =  downloadUpdatedProjects(ptc, projectsToDownload);
        if(!downloaded)  {
            Utils.logError(LogTags.PROJECT_LIST_SYNC, "Error downloading Projects from server for appId: " +appId + " , userId:" + userId);
            return;
        };

        boolean deleted = deleteUnassignedProjects(userId, appId, projectsToDelete);
        if(!deleted) {
            Utils.logError(LogTags.PROJECT_LIST_SYNC, "Error deleting unassigned Projects for appId: " +appId + " , userId:" + userId);
            return;
        };

    }

    private List<String> filterProjectsToDownload(Map<String, Long> existingProjectIdToTs, Map<String, Long> serverProjectIdToTs) {

        List<String> projectsToFetch = new ArrayList<>();
        for (String projectId : serverProjectIdToTs.keySet()) {
            if (!existingProjectIdToTs.containsKey(projectId) || existingProjectIdToTs.get(projectId) < serverProjectIdToTs.get(projectId)) {
                projectsToFetch.add(projectId);
            }
        }
        return projectsToFetch;
    }


    private List<String> filterProjectsToDelete(String userId, String appId
            , Map<String, Long> existingProjectIdToTs, Map<String, Long> serverProjectIdToTs) {

        List<String> projectsToDelete = new ArrayList<>();
        for (String projectId: existingProjectIdToTs.keySet()) {
            if(!serverProjectIdToTs.containsKey(projectId)) {
                projectsToDelete.add(projectId);
            }
        }
        return projectsToDelete;
    }

    public boolean uploadUnSyncedProjects(String userId, String appId) throws JSONException, UAException {
        List<ProjectSubmissionUploadStatus> uploadStatusList = new ArrayList<>();
        uploadStatusList.add(ProjectSubmissionUploadStatus.UNSYNCED);
        uploadStatusList.add(ProjectSubmissionUploadStatus.SERVER_ERROR);

        List<ProjectSubmission> unSyncedProjectSubmission = mDbHelper.getProjectsToSubmit(userId, appId, uploadStatusList);

        boolean uploadStatus = true;
        Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Projects on local DB to update " + unSyncedProjectSubmission.size());

        if (unSyncedProjectSubmission == null || unSyncedProjectSubmission.isEmpty()) {
            return true;
        }

        AppMetaData appMetaConfig = UAAppContext.getInstance().getAppMDConfig();
        Long serverErrorSyncFrequency = (appMetaConfig.mServiceFrequency == null
                || appMetaConfig.mServiceFrequency.get(Constants.SERVICE_FREQUENCY_SERVER_ERROR) == null ) ? ProjectSubmissionConstants.DEFAULT_RETRY_FREQUENCY: appMetaConfig.mServiceFrequency.get(Constants.SERVICE_FREQUENCY_SERVER_ERROR);
        int retryCount = appMetaConfig.mRetries == 0 ? ProjectSubmissionConstants.DEFAULT_SUBMISSION_RETRIES : appMetaConfig.mRetries ;

        NewSubmitProjectService pss = new NewSubmitProjectService();

        for (ProjectSubmission submissionData : unSyncedProjectSubmission) {

            Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Project Submit Status for appId: " +appId + " , userId:" + userId + "ProjectId: "+ submissionData.getProjectId() + " :: " + submissionData.getUploadStatus());

            if(submissionData.getUploadStatus() == ProjectSubmissionUploadStatus.SYNCED.getValue()
                    || submissionData.getUploadStatus() == ProjectSubmissionUploadStatus.FAILED.getValue()) {
                continue;
            }

            if((submissionData.getUploadStatus() == ProjectSubmissionUploadStatus.SERVER_ERROR.getValue())
                    && (System.currentTimeMillis() - submissionData.getLastServerUploadTimestamp() < serverErrorSyncFrequency)
                    && (submissionData.getUploadRetryCount() < retryCount)) {
                continue;
            } else if (submissionData.getUploadStatus() == ProjectSubmissionUploadStatus.SERVER_ERROR.getValue()
                    && submissionData.getUploadRetryCount() > retryCount) {
                setProjectStatusAsFailed(submissionData);
                continue;
            }

            Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : Before Lock "  + submissionData.getProjectId());
            synchronized (AppBackgroundSync.getSyncLock()) {
                Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : After Lock "  + submissionData.getProjectId());
                Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Submitting project ");
                uploadStatus =  pss.callProjectSubmitService(submissionData);
                Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : After Upload "  + submissionData.getProjectId());
            }
            Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "XXXX: In Offline upload : After Lock Release "  + submissionData.getProjectId());

            // if upload failed stop execution
            if(!uploadStatus) {
                return false;
            }
        }
        Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Successfully uploaded projects for appId: " +appId + " , userId:" + userId);
        return true;
    }

    private void setProjectStatusAsFailed(ProjectSubmission submissionData) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS,
                ProjectSubmissionUploadStatus.FAILED.getValue());
        mDbHelper.updateProjectSubmission(submissionData.getmUserId(), submissionData.getAppId(), submissionData.getProjectId(), submissionData.getTimestamp(), values);
    }

    public  boolean downloadUpdatedProjects(ProjectTypeConfiguration ptc, List<String> projectIDs)
            throws ServerFetchException, AppCriticalException {
        synchronized (AppBackgroundSync.getSyncLock()) {
            if (!projectIDs.isEmpty()) {
                callProjectListService(ptc, projectIDs);
            }
        }
        Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Successfully downloaded projects from server for appId: " +ptc.mProjectTypeId + " , userId:" + ptc.mUserId);
        return true;
    }

    public boolean deleteUnassignedProjects(String userId, String appId, List<String> projectsToDelete) {
        if (projectsToDelete == null || projectsToDelete.isEmpty()) {
            return true;
        }
        for (String projectId: projectsToDelete) {
            unAssignProjectFromUser(userId,appId,projectId);
            synchronized (AppBackgroundSync.getSyncLock()) {
//                If status of any one project data submission is 0 do nothing else delete
//                        delete from project table only if project data submission is deleted

                List<ProjectSubmissionUploadStatus> uploadStatusList = new ArrayList<>();
                uploadStatusList.add(ProjectSubmissionUploadStatus.UNSYNCED);
                uploadStatusList.add(ProjectSubmissionUploadStatus.SERVER_ERROR);

                List<Integer> mediaStatusList = new ArrayList<Integer>();
                mediaStatusList.add(MediaRequestStatus.NEW_STATUS.getValue());
                mediaStatusList.add(MediaRequestStatus.PENDING_RETRY_STATUS.getValue());

                // get unSynced projectSubmission count also
                int formSubmissionDataCount  = mDbHelper.getProjectSubmissionCountForGivenStatusList(appId, userId, projectId
                        , uploadStatusList);
                // get unSynced Media count
                int mediaCount = mDbHelper.getFormMediaCountForGivenStatusList(appId, userId, projectId, mediaStatusList);

//                 if no unSynced media and projectSubmission data found delete project else do nothing
                if(formSubmissionDataCount == 0 && mediaCount == 0) {

                    // delete all project submission
                    mDbHelper.deleteAllProjectSubmission(appId, userId, projectId);
                    List<String> mediaPaths = mDbHelper.getMediaPathsForProject(appId, userId, projectId);
                    // delete all media from DB
                    mDbHelper.deleteAllTheMediaForProject(appId, userId, projectId);
                    // delete all media from storage
                    Utils.getInstance().deleteAllMediaFromStorage(mediaPaths);
                    // delete project
                    mDbHelper.deleteProject(appId, userId, projectId);

                } else {
                    continue;
                }
            }
        }
        Utils.logInfo(LogTags.PROJECT_LIST_SYNC, "Successfully deleted unassigned projects for appId: " +
                "" +appId + " , userId:" + userId + ", projectIds : " + projectsToDelete );
        return true;
    }
    public int unAssignProjectFromUser(String userId, String appId, String projectId) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS, false);
        int id = mDbHelper.updateAssignedStatusOfProjectFromUser(userId, appId
                , projectId, values);

        Utils.logInfo(LogTags.PROJECT_DELETE, " -- unassigned the project ::  "
                + projectId + " -- from user ::  " + userId + " -- AppId :: " + appId);
        return id;
    }
}