package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.Entity;
import com.vassar.unifiedapp.model.EntityMetaData;
import com.vassar.unifiedapp.model.EntityMetaDataConfiguration;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.ui.LoginActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.ServerConstants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class EntityConfigService {

    private UnifiedAppDBHelper mDbHelper;
    private Context mContext;
    private SharedPreferences mAppPreferences;

    public static final MediaType JSON
            = MediaType.get("application/json");

    public EntityConfigService() {
        mDbHelper = UAAppContext.getInstance().getDBHelper();
        mContext = UAAppContext.getInstance().getContext();
        mAppPreferences = UAAppContext.getInstance().getAppPreferences();
    }

    private String createRequestParams(String superAppId, String appId, String userId, String projectId,
                                       String parentName, String name, long latestTimeStamp) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_USER_ID, null);
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_USER, userId);
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_TOKEN, UAAppContext.getInstance().getToken());
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_SUPER_APP_ID, superAppId);
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_APP_ID, appId);
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_PROJECT_ID, projectId);
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_PARENT_ENTITY, parentName);
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_ENTITY_NAME, name);
            jsonObject.put(ServerConstants.ENTITY_METADATA_CONFIG_LATEST_TIMESTAMP, latestTimeStamp);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String fetchFromServer(String superAppId, String appId, String userId, String projectId,
                                   String parentName, String name, long latestTimeStamp)
            throws ServerFetchException, AppCriticalException{

        OkHttpClient client = new OkHttpClient();

        okhttp3.Response response = null;
        String requestParam = createRequestParams(superAppId, appId, userId, projectId, parentName, name, latestTimeStamp);
        if (requestParam == null || requestParam.isEmpty()) {
            Utils.logError(LogTags.ENTITY_CONFIG, "Error creating request parameters - Entity Config");
            throw new AppCriticalException(UAAppErrorCodes.ENTITY_CONFIG_FETCH, "Error creating request parameters - Entity Config");
        }
        RequestBody body = RequestBody.create(JSON, requestParam);
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "entitymetadataconfig")
                .post(body)
                .build();

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error getting EntityConfig from server");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting EntityConfig from server");
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
                    Utils.logDebug(LogTags.APP_MD_CONFIG, "EntityConfig : No New Version");
                    return null;
                } else {
                    return content.toString();
                }
            } else if (code == 350 && AppBackgroundSync.isSyncInProgress) {

                Utils.logError(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Token is expired for this user");
                SharedPreferences.Editor editor = mAppPreferences.edit();
                editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY
                        , Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);
                editor.apply();

                Intent i = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(i);

                Thread.currentThread().interrupt();
                if (content != null) {
                    return content.toString();
                } else {
                    return null;
                }

            }else {
                Utils.logError(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting EntityConfig from server (invalid data) : response code : " + code
                                + " received content : " + content);
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting EntityConfig from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting EntityConfig from server -"
                            + " received content : " + content);
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting EntityConfig from server");
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
                    ,"Error getting EntityConfig from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting EntityConfig from server :"
                    + " received responseString : " + responseString);
        }

    }

    public void callEntityConfigService (String superAppId, String appId, String userId, String projectId,
                                         String parentName, String name, long latestTimeStamp)
            throws AppCriticalException, ServerFetchException {

        String content = fetchFromServer(superAppId, appId, userId, projectId, parentName, name, latestTimeStamp);
        if (content == null) {
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try{
            EntityMetaDataConfiguration entityMetaDataConfiguration = objectMapper.readValue(content, EntityMetaDataConfiguration.class);
            List<EntityMetaData> entityMetaDataArrayList = entityMetaDataConfiguration.getEntityMetaDataArrayList();

            if(entityMetaDataArrayList != null && !entityMetaDataArrayList.isEmpty()) {

                for (int i = 0; i < entityMetaDataArrayList.size(); i++) {

                    EntityMetaData entityMetaData = entityMetaDataArrayList.get(i);
                    if (entityMetaData != null) {
                        storeToDB(entityMetaData);
                    }
                }
            }
        }
        catch (IOException e) {
            Utils.logError(LogTags.ENTITY_CONFIG,"Failed to parse json String :: " + content);
            e.printStackTrace();
        }
    }

    private void storeToDB(EntityMetaData entityMetadata) {
        // Add new EntityConfig to the DB
        ContentValues values = new ContentValues();

        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_SUPER_APP_ID, entityMetadata.getSuperAppId());
        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_APP_ID, entityMetadata.getAppId());
        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_PROJECT_ID, entityMetadata.getProjectId());
        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_USER_ID, entityMetadata.getUserId());
        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_PARENT_ENTITY, entityMetadata.getParentName());
        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_ENTITY_NAME, entityMetadata.getName());
        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_ELEMENTS, entityMetadata.getEntityList());
        values.put(UnifiedAppDbContract.EntityMetaEntry.COLUMN_INSERT_TIMESTAMP, entityMetadata.getTimeStamp());

        long id = mDbHelper.addOrUpdateEntityMetaData(values);
       /*List<Entity> elements =  new ArrayList<>();

            elements = mDbHelper.getEntityList(entityMetadata.getSuperAppId(), entityMetadata.getAppId(), entityMetadata.getProjectId(), entityMetadata.getUserId(), entityMetadata.getParentName(), entityMetadata.getName());

        System.out.println(elements);*/
    }
}
