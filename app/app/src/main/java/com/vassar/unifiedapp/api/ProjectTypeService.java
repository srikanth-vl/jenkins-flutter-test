package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.DBProjectForm;
import com.vassar.unifiedapp.model.ProjectSpecificForms;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.ui.LoginActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ProjectTypeService {

    private UnifiedAppDBHelper mDbHelper;
    private Context mContext;
    private SharedPreferences mAppPreferences;

    public static final MediaType JSON
            = MediaType.get("application/json");

    public ProjectTypeService() {
        mDbHelper = UAAppContext.getInstance().getDBHelper();
        mContext = UAAppContext.getInstance().getContext();
        mAppPreferences = UAAppContext.getInstance().getAppPreferences();
    }

    private String createRequestParams(String appId, Map<String, Map<String, Integer>> formVersion) {
        JSONObject jsonObject = new JSONObject();
        JSONObject  exitingVersionInfo;
        try {
            if (formVersion == null || formVersion.isEmpty())
                exitingVersionInfo = null;
            else
                exitingVersionInfo = createJsonObjectFromMap(formVersion);
            jsonObject.put(Constants.PROJECT_TYPE_CONFIG_USER_ID_KEY, UAAppContext.getInstance().getUserID());
            jsonObject.put(Constants.PROJECT_TYPE_CONFIG_TOKEN_KEY, UAAppContext.getInstance().getToken());
            jsonObject.put(Constants.PROJECT_TYPE_CONFIG_SUPER_APP_KEY, Constants.SUPER_APP_ID);
            jsonObject.put(Constants.PROJECT_TYPE_CONFIG_APP_ID_KEY, appId);

            jsonObject.put(Constants.PROJECT_TYPE_CONFIG_FORM_VERSION_KEY, exitingVersionInfo);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ProjectTypeConfiguration callProjectTypeConfigService(String appId, Map<String, Map<String, Integer>> formVersion)
            throws AppCriticalException, ServerFetchException {

        // Logic
        // 1. Fetch from Server
        // 2. Save to DB
        // 3. Return the AppMetaData
        // 4. In case of error - return the error type

        String content = fetchFromServer(appId, formVersion);
        if (content == null) {
            return null;
        }

        // Add new AppMataDataConfig to the App Context
        ProjectTypeConfiguration projectTypeConfiguration = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            projectTypeConfiguration = mapper.readValue(content, ProjectTypeConfiguration.class);
            storeToDB(projectTypeConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projectTypeConfiguration;
    }

    // Map --> [Project UUID, Map --> [Form Type, Version]]
    private String fetchFromServer(String appId, Map<String, Map<String, Integer>> formVersion)
            throws AppCriticalException, ServerFetchException {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Response response = null;
        String requestParam = createRequestParams(appId, formVersion);
        if (requestParam == null || requestParam.isEmpty()) {
            Utils.logError(LogTags.PROJECT_TYPE_CONFIG, "Error creating request parameters - Project Type Config");
            throw new AppCriticalException(UAAppErrorCodes.PROJECT_TYPE_FETCH, "Error creating request parameters - Project Type Config");
        }
        RequestBody body = RequestBody.create(JSON, requestParam);
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "projecttype")
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error getting ProjectTypeConfiguration from server");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting ProjectTypeConfiguration from server");
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
                    Utils.logDebug(LogTags.APP_MD_CONFIG, "ProjectTypeConfiguration : No New Version");
                    return null;
                } else {
                    return content.toString();
                }
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

                if (content != null) {
                    return content.toString();
                } else {
                    return null;
                }
            } else {
                Utils.logError(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting ProjectTypeConfiguration from server (invalid data) : response code : " + code
                                + " received content : " + content);
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting ProjectTypeConfiguration from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting ProjectTypeConfiguration from server -"
                            + " received content : " + content);
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting ProjectTypeConfiguration from server");
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
                    ,"Error getting ProjectTypeConfiguration from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting ProjectTypeConfiguration from server :"
                    + " received responseString : " + responseString);
        }
    }

    private void storeToDB(ProjectTypeConfiguration projectTypeConfiguration) {
        // Add new projectTypeConfiguration to the DB
        List<DBProjectForm> dbForms = new ArrayList <DBProjectForm> ();
        for ( String projectId: projectTypeConfiguration.mContent.keySet() ) {
            Map<String, ProjectSpecificForms> actionTypeToFormMap = projectTypeConfiguration.mContent.get(projectId);
            for (String formType : actionTypeToFormMap.keySet()) {
                ContentValues values = new ContentValues();
                values.put(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_APP_ID, projectTypeConfiguration.mProjectTypeId);
                values.put(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_USER_ID, projectTypeConfiguration.mUserId);
                values.put(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_PROJECT_ID, projectId);
                values.put(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_FORM_TYPE, formType);
                ProjectSpecificForms formData = actionTypeToFormMap.get(formType);
                ObjectMapper mapper =  new ObjectMapper();
                try{
                String formString = mapper.writeValueAsString(formData);
                values.put(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_FORM_DATA, formString);
                values.put(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_VERSION, formData.mFormVerion);
                values.put(UnifiedAppDbContract.ProjectFormTableEntry.COLUMN_MD_INSTANCE_ID, formData.mMetaDataInstanceId);
                mDbHelper.addOrUpdateProjectFormEntry(values);
                } catch (JsonProcessingException e) {
                    Utils.logError(LogTags.PROJECT_TYPE_CONFIG, "failed to parse json");
		            e.printStackTrace();
                }
            }
        }
    }

    public Map<String, ProjectTypeConfiguration> fetchAppIdToProjectTypeConfigurationFromDb(String userId, List<ProjectTypeModel> ptmList) {
        // If ptmList is null or empty then return all app ids
        // else return for only the appids in the ptmList
        Map<String, ProjectTypeConfiguration> ptcMap =  new HashMap<>();
        if(ptmList == null) {
            ptmList =  UAAppContext.getInstance().getRootConfig().mApplications;

        }
        ProjectTypeConfiguration ptc ;
        for (ProjectTypeModel ptm: ptmList)
        {
            String appId = ptm.mAppId;
            ptc = fetchProjectTypeConfigurationFromDb(userId, appId);
            if(ptc != null) {
                ptcMap.put(appId, ptc);
            }
        }
        return ptcMap;

    }

    public ProjectTypeConfiguration fetchProjectTypeConfigurationFromDb(String userId, String appId) {
        ProjectTypeConfiguration ptc =  mDbHelper.getProjectFormForApp(userId, appId);
        return  ptc;
    }
    public JSONObject createJsonObjectFromMap(Map<String, Map<String, Integer>> formVersionMap) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        for (String projectId : formVersionMap.keySet()) {
            JSONObject formTypeToVersionJson = new JSONObject();
            Map<String, Integer> formTypeToVersionMap = formVersionMap.get(projectId);
            for (String formType : formTypeToVersionMap.keySet()) {
                formTypeToVersionJson.put(formType, formTypeToVersionMap.get(formType));
            }
            jsonObject.put(projectId, formTypeToVersionJson);
        }

        return jsonObject;
    }
}
