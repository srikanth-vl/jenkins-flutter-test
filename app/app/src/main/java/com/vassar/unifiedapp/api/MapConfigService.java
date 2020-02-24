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
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MapUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MapConfigService {

    public static final MediaType JSON
            = MediaType.get("application/json");
    private UnifiedAppDBHelper mDbHelper;
    private Context mContext;
    private SharedPreferences mAppPreferences;
    private ObjectMapper mapper;

    public MapConfigService() {
        mDbHelper = UAAppContext.getInstance().getDBHelper();
        mContext = UAAppContext.getInstance().getContext();
        mAppPreferences = UAAppContext.getInstance().getAppPreferences();
        mapper = new ObjectMapper();
    }

    public MapConfigurationV1 callMapConfigService()
            throws AppCriticalException, ServerFetchException {

        // Logic
        // 1. Fetch from Server
        // 2. Save to DB
        // 3. Return the MapConfig
        // 4. In case of error - return the error type

        String content = fetchFromServer();
        if (content == null) {
            return null;
        } else if (content.equals(Constants.NO_NEW_MAP_CONFIG)) {
            Utils.logDebug(LogTags.MAP_CONFIG, "MapConfig : No New Version");
            return UAAppContext.getInstance().getMapConfig();
        } else {
            MapConfigurationV1 mapConfig = null;
            // Add new RootConfig to the DB
            try {
                mapConfig = mapper.readValue(content, MapConfigurationV1.class);

                storeToDB(content, mapConfig);

                if (mapConfig.files != null) {
                    MapUtils.getInstance().downloadMapLayers(mapConfig.files);
                }
                if (mapConfig.mapMarkers != null) {
                    for (String key : mapConfig.mapMarkers.keySet()) {
                        MapUtils.getInstance().downloadMapMarkers(mapConfig.mapMarkers.get(key));
                    }
                }

                UAAppContext.getInstance().setMapConfig(mapConfig);
            } catch (IOException e) {
                Utils.logError(LogTags.MAP_CONFIG, "Could not create MapConfig object from json object :: " + content);
                e.printStackTrace();
            }
            return mapConfig;
        }
    }

    private String fetchFromServer()
            throws AppCriticalException, ServerFetchException {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        okhttp3.Response response = null;
        String requestParam = createRequestParams();
        if (requestParam == null || requestParam.isEmpty()) {
            Utils.logError(LogTags.MAP_CONFIG, "Error creating request parameters - Map Config");
            throw new AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error creating request parameters - Map Config");
        }
        RequestBody body = RequestBody.create(JSON, requestParam);
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "mapconfigdata")
                .post(body)
                .build();

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting MapConfig from server 1", e);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting MapConfig from server 1", e);
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting MapConfig from server 2");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting MapConfig from server 2");
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
                    Utils.logDebug(LogTags.MAP_CONFIG, "MapConfig : No New Version");
                    return Constants.NO_NEW_MAP_CONFIG;
                } else {
                    return content.toString();
                }
            } else if (code == 350) {

                if (AppBackgroundSync.isSyncInProgress) {
                    Thread.currentThread().interrupt();
                }

                Utils.logError(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Token is expired for this user");

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
                        , "Error getting MapConfig from server (invalid data) : response code : " + code
                                + " received content : " + content.toString());
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting MapConfig from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    , "Error getting MapConfig from server -"
                            + " received content : " + content.toString());
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    , "Error getting MapConfig from server");
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
                    , "Error getting MapConfig from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting MapConfig from server :"
                    + " received responseString : " + responseString);
        }
    }

    private void storeToDB(String content, MapConfigurationV1 mapConfig) {
        // Add new RootConfig to the DB
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_NAME
                , Constants.MAP_CONFIG_DB_NAME);
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_FILE_CONTENT
                , content);
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_LAST_SYNC_TS
                , mapConfig.currentServerTime);
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_USER_ID
                , UAAppContext.getInstance().getUserID());
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_VERSION
                , mapConfig.version);

        // Add RootConfig to DB
        mDbHelper.addOrUpdateConfigFile(values);
    }

    private String createRequestParams() {
        JSONObject mapConfigParameters = new JSONObject();
        try {
            mapConfigParameters.put(Constants.MAP_CONFIG_USER_ID_KEY, UAAppContext.getInstance().getUserID());
            mapConfigParameters.put(Constants.MAP_CONFIG_TOKEN_KEY, UAAppContext.getInstance().getToken());
            mapConfigParameters.put(Constants.MAP_CONFIG_SUPER_APP_KEY, Constants.SUPER_APP_ID);

            MapConfigurationV1 existingMapConfig = UAAppContext.getInstance().getMapConfig();

            Integer version = existingMapConfig == null ? null : existingMapConfig.getVersion();
            String existingVersion = version == null ? null : String.valueOf(version);
            mapConfigParameters.put(Constants.MAP_CONFIG_VERSION_KEY, existingVersion);
            return mapConfigParameters.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
