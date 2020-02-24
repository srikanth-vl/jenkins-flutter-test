package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.listener.AppMetaConfigServiceListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AppMetaConfigService {

    private UnifiedAppDBHelper mDbHelper;
    private AppMetaConfigServiceListener mListener;
    private Context mContext;
    private  ObjectMapper mapper = new ObjectMapper();

    public static final MediaType JSON
            = MediaType.get("application/json");

    public AppMetaConfigService() {
        mDbHelper = UAAppContext.getInstance().getDBHelper();
        mContext = UAAppContext.getInstance().getContext();
    }

    private String createRequestParams(String appMDVersion) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (appMDVersion == null) {
                jsonObject.put(Constants.APP_META_DATA_VERSION_KEY,
                        Constants.SPLASH_VERSION_DEFAULT);
            } else {
                jsonObject.put(Constants.APP_META_DATA_VERSION_KEY,
                        appMDVersion);
            }
            jsonObject.put(Constants.APP_META_DATA_SUPER_APP_KEY, Constants.SUPER_APP_ID);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Logic
    // 1. Fetch from Server
    // 2. Save to DB
    // 3. Return the AppMetaData
    // 4. In case of error - return the error type
    //
    // Err:
    // 1. If network error - return as network error
    //    (the caller decides what to do with error
    //         if this is the first time launch - then exit - decided by App Controller
    //         if it is called by background thread - then its offline case, do nothing, try later)
    // 2. If local DB does not exist - app is corrupted or someone delete the DB
    //    (the caller decides what to do with error
    //         in this case simply exit)
    public AppMetaData callAppMetaDataService(String appMDVersion)
        throws AppCriticalException, ServerFetchException {

        // Logic
        // 1. Fetch from Server
        // 2. Save to DB
        // 3. Return the AppMetaData
        // 4. In case of error - return the error type
        AppMetaData appMetaData = null;
        String content = fetchFromServer(appMDVersion);
        if (content == null) {
            return null;
        } else if (content.equals(Constants.NO_NEW_APP_MD)) {
            Utils.logDebug(LogTags.APP_MD_CONFIG, "AppMDConfig : No New Version");
            ConfigFile existingAppMetaConfigFile = mDbHelper.getConfigFile(Constants.DEFAULT_USER_ID, Constants.APP_META_CONFIG_DB_NAME);
            String existingAppMetaConfig = existingAppMetaConfigFile.getConfigContent();
//            Gson gson = new Gson();
            try {
                appMetaData = mapper.readValue(existingAppMetaConfig, AppMetaData.class);
//              return gson.fromJson(existingAppMetaConfig, AppMetaData.class);
            } catch (IOException e) {
                Utils.logError(LogTags.APP_MD_CONFIG, "failed to create AppMetaData object from config json :: " + existingAppMetaConfig);
                e.printStackTrace();
            }
        } else {
            // Add new AppMataDataConfig to the App Context
            try {
                Utils.logInfo("APPMETACONTENT", content);
                appMetaData = mapper.readValue(content, AppMetaData.class);

                // Download if any new images that come in the AppMDConfig
                downloadAppMDConfigImages(appMetaData);

                storeToDB(content, appMetaData);
            } catch (IOException e) {
                Utils.logError(LogTags.APP_MD_CONFIG, "Could Not Convert response JSON "+  content.toString() +"  to AppMetaConfig Object");
                e.printStackTrace();
            }
        }
        return appMetaData;
    }

    private String fetchFromServer(String appMDVersion)
        throws AppCriticalException, ServerFetchException {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Response response = null;
        String requestParam = createRequestParams(appMDVersion);
        if (requestParam == null || requestParam.isEmpty()) {
            Utils.logError(LogTags.APP_MD_CONFIG, "Error creating request parameters - App MD Config");
            throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error creating request parameters - App MD Config");
        }
        RequestBody body = RequestBody.create(JSON, createRequestParams(appMDVersion));
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "appmetaconfigjson")
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error getting AppMetaData from server 1", e);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting AppMetaData from server 1", e);
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error getting AppMetaData from server 2");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting AppMetaData from server 2");
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
                    Utils.logDebug(LogTags.APP_MD_CONFIG, "AppMDConfig : No New Version");
                    return Constants.NO_NEW_APP_MD;
                } else {
                    return content.toString();
                }
            } else {
                Utils.logError(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting AppMetaData from server (invalid data) : response code : " + code
                                + " received content : " + content);
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting AppMetaData from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting AppMetaData from server -"
                            + " received content : " + content);
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting AppMetaData from server");
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
                    ,"Error getting AppMetaData from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting AppMetaData from server :"
                    + " received responseString : " + responseString);
        }
    }

    private void storeToDB(String content, AppMetaData appMetaData) {
        // Add new AppMetaDataConfig to the DB
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_NAME
                , Constants.APP_META_CONFIG_DB_NAME );
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_FILE_CONTENT
                , content);
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_LAST_SYNC_TS
                ,  appMetaData.mCurrentServerTime);
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_USER_ID
                , Constants.DEFAULT_USER_ID);
        values.put(UnifiedAppDbContract.ConfigFilesEntry.COLUMN_CONFIG_VERSION
                ,  appMetaData.mSplashConfigVersion);
        mDbHelper.addOrUpdateConfigFile(values);
    }

    private void downloadAppMDConfigImages(AppMetaData appMetaData) {
        String splashIconUrl = appMetaData.mSplashScreenProperties.mSplashIconUrl;
        String splashBackgroundUrl = appMetaData.mSplashScreenProperties.mSplashBackground;
        String loginIconUrl = appMetaData.mSplashScreenProperties.mLoginIcon;

        if (splashIconUrl != null) {
            IncomingImage incomingImage = UAAppContext.getInstance().getDBHelper().getIncomingImageWithUrl(splashIconUrl);
            if (incomingImage == null) {
                IncomingImage splashIcon = new IncomingImage(Constants.SPLASH_IMAGE_FOREGROUND,
                        null, splashIconUrl);
//                Utils.getInstance().saveImagesToDatabase(UAAppContext.getInstance().getDBHelper(), splashIcon);
                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(splashIcon);
                newImageDownloaderService.execute();
            }
        }

        if (splashBackgroundUrl != null) {
            IncomingImage incomingImage = UAAppContext.getInstance().getDBHelper().getIncomingImageWithUrl(splashBackgroundUrl);
            if (incomingImage == null) {
                IncomingImage splashBackground = new IncomingImage(Constants.SPLASH_IMAGE_BACKGROUND,
                        null, splashBackgroundUrl);
//                Utils.getInstance().saveImagesToDatabase(mDBHelper, splashBackground);
                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(splashBackground);
                newImageDownloaderService.execute();
            }
        }

        if (loginIconUrl != null) {
            IncomingImage incomingImage = UAAppContext.getInstance().getDBHelper().getIncomingImageWithUrl(loginIconUrl);
            if (incomingImage == null) {
                IncomingImage loginIcon = new IncomingImage(Constants.LOGIN_ICON_IMAGE,
                        null, loginIconUrl);
//                Utils.getInstance().saveImagesToDatabase(mDBHelper, loginIcon);
                NewImageDownloaderService newImageDownloaderService = new NewImageDownloaderService(loginIcon);
                newImageDownloaderService.execute();
            }
        }
    }
}
