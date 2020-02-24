package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.model.LocalizationConfig;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.LocalizationUtils;
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

public class LocalizationConfigService {

    public static final MediaType JSON
            = MediaType.get("application/json");
    private UnifiedAppDBHelper mDbHelper;
    private Context mContext;
    private SharedPreferences mAppPreferences;
    private ObjectMapper mapper;

    public LocalizationConfigService() {
        mDbHelper = UAAppContext.getInstance().getDBHelper();
        mContext = UAAppContext.getInstance().getContext();
        mAppPreferences = UAAppContext.getInstance().getAppPreferences();
        mapper = new ObjectMapper();
    }

    public JSONObject callLocationConfigService()
            throws AppCriticalException, ServerFetchException {

        // Logic
        // 1. Fetch from Server
        // 2. Save to file
        // 3. Return the LocalizationConfig
        // 4. In case of error - return the error type

        String content = fetchFromServer();
        String localizationConfig = "{\"hi\":{\"Structure Type\":\"संरचना प्रकार\",\"Structure Possible\":\"संरचना संभव\",\"Yes\":\"हाँ\",\"No\":\"नहीं\",\"PT\":\"पीटी\",\"LBS\":\"एलबीएस\",\"RFD\":\"आरएफडी\",\"GABION\":\"गेबियन\",\"MINI PT\":\"मिनी पीटी\",\"FARMPOND\":\"कृषि तालाब\",\"CHECKWALL\":\"चेकवाल \",\"PT STORAGE\":\"पीटी भंडारण\",\"PERCOLATION STORAGE\":\"\",\"CHECKDAM-STORAGE\":\"\",\"CHECKDAM-PERCOLATION\":\"पीटी भंडारण\",\"Drain Name\":\"नाली का नाम\",\"Maximum characters\":\"अधिकतम वर्ण\",\"Geo-tagged Picture\":\"भू-टैग चित्र\",\"Back\":\"पीछे\",\"Next\":\"अगला\",\"Submit\":\"सबमिट\",\"Preview\":\"पूर्वावलोकन\",\"Cancel\":\"हटाए\",\"Panchayat\":\"पंचायत\",\"District\":\"जिला\",\"Structre Type\":\"संरचना प्रकार\",\"Watershed\":\"वाटरशेड\",\"Survey Number\":\"सर्वे नंबर\",\"Mandal\":\"मंडल\",\"Sub Basin\":\"सब बेसिन\",\"GPS Location\":\"जीपीएस स्थान\",\"Change\":\"बदलें\",\"Get Direction\":\"दिशा प्राप्त करें\",\"ID\":\"आई डी\",\"Is the structure possible within 50m radius?\":\"क्या संरचना 50 मीटर के दायरे में संभव है?\",\"Select Reason\":\"कारण चुनें\",\"Farmer didn't agree\":\"किसान सहमत नहीं था\",\"Existing Structure\":\"मौजूदा संरचना\",\"Encroached Drain\":\"अतिक्रमित नाली\",\"Other Reason\":\"दूसरी वजह\",\"Proposed Location Picture\":\"प्रस्तावित स्थान चित्र\",\"Changed location Picture\":\"परिवर्तित स्थान चित्र\",\"Drop New Location\":\"नया स्थान चिह्नित करें\",\"Changed Location Picture\":\"परिवर्तित स्थान चित्र\",\"Location\":\"स्थान\"}}"+
                "" ;
        if (content == null) {
            localizationConfig = LocalizationUtils.getInstance().getData();
        } else if (content.equals(Constants.NO_NEW_LOCALIZATION_CONFIG)) {
            Utils.logDebug(LogTags.LOCALIZATION_CONFIG, "LocalizationConfig : No New Version");
            localizationConfig = LocalizationUtils.getInstance().getData();
        } else {
            try{
                LocalizationConfig config = mapper.readValue(content, LocalizationConfig.class);
               localizationConfig = config.mConfig;
                LocalizationUtils.getInstance().saveData(config.mConfig);
            }
            catch (JsonMappingException | JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
	// Save new LocalizationConfig to file
        if(localizationConfig != null && !localizationConfig.isEmpty()) {
            JSONObject localizationConfigJson = mapper.convertValue(localizationConfig, JSONObject.class);
            return localizationConfigJson;
        }
        return null;

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
            Utils.logError(LogTags.LOCALIZATION_CONFIG, "Error creating request parameters - Localization Config");
            throw new AppCriticalException(UAAppErrorCodes.LOCALIZATION_CONFIG_FETCH, "Error creating request parameters - Localization Config");
        }
        RequestBody body = RequestBody.create(JSON, requestParam);
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "localizationconfigdata")
                .post(body)
                .build();

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting LocalizationConfig from server 1", e);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting LocalizationConfig from server 1", e);
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting LocalizationConfig from server 2");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting LocalizationConfig from server 2");
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
                    Utils.logDebug(LogTags.LOCALIZATION_CONFIG, "LocalizationConfig : No New Version");
                    return Constants.NO_NEW_LOCALIZATION_CONFIG;
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
                        , "Error getting LocalizationConfig from server (invalid data) : response code : " + code
                                + " received content : " + content.toString());
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting LocalizationConfig from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    , "Error getting LocalizationConfig from server -"
                            + " received content : " + content.toString());
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    , "Error getting LocalizationConfig from server");
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
                    , "Error getting LocalizationConfig from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting LocalizationConfig from server :"
                    + " received responseString : " + responseString);
        }
    }



    private String createRequestParams() {
        JSONObject localizationConfigParameters = new JSONObject();
        try {
            localizationConfigParameters.put(Constants.ROOT_CONFIG_USER_ID_KEY, UAAppContext.getInstance().getUserID());
            localizationConfigParameters.put(Constants.ROOT_CONFIG_TOKEN_KEY, UAAppContext.getInstance().getToken());
            localizationConfigParameters.put(Constants.ROOT_CONFIG_SUPER_APP_KEY, Constants.SUPER_APP_ID);

            Integer version = null;
            String existingVersion = version == null ? null : String.valueOf(version);
            localizationConfigParameters.put(Constants.ROOT_CONFIG_VERSION_KEY, existingVersion);
            return localizationConfigParameters.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}