package com.vassar.unifiedapp.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.ui.LoginActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TransactionLogService {

    private Context mContext;
    private SharedPreferences mAppPreferences;
    private ObjectMapper mObjectMapper = new ObjectMapper();
    public static final MediaType JSON
            = MediaType.get("application/json");

    public TransactionLogService() {
        mContext = UAAppContext.getInstance().getContext();
        mAppPreferences = UAAppContext.getInstance().getAppPreferences();
    }

    private String createRequestParams(String appId, String projectId, String key) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", UAAppContext.getInstance().getUserID());
            jsonObject.put("token", UAAppContext.getInstance().getToken());
            jsonObject.put("super_app", PropertyReader.getProperty("SUPER_APP_ID"));
            jsonObject.put("app_id", appId);
            jsonObject.put("project_id", projectId);
            jsonObject.put("key", key);
            Utils.logInfo("JSON STRING : ", jsonObject.toString());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> fetchFromServer(String appId, String projectId, String key)
            throws ServerFetchException, AppCriticalException {

        OkHttpClient client = new OkHttpClient();

        okhttp3.Response response = null;
        String requestParam = createRequestParams(appId, projectId, key);
        if (requestParam == null || requestParam.isEmpty()) {
            Utils.logError("TRANSACTION LOG", "Error creating request parameters - last transactions");
            throw new AppCriticalException("TRANSACTION LOG", "Error creating request parameters - last transactions");
        }
        RequestBody body = RequestBody.create(JSON, requestParam);
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "transactionlogforkey")
                .post(body)
                .build();

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error getting last transactions from server");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting last transactions from server");
        }

        // Response code lies within 200-300
        String responseString = null;
        List<String> content = null;
        JSONArray jsonContent = null;
        int code = 0;
        try {
            responseString = response.body().string();
            JSONObject jsonObject = new JSONObject(responseString);
            JSONObject resultObject = jsonObject.getJSONObject("result");
            code = resultObject.getInt("status");
            if (code == 200) {
                jsonContent = (JSONArray) resultObject.get("content");

                content = mObjectMapper.readValue(jsonContent.toString(),new TypeReference<List<String>>() {});

                if (content == null) {
                    return null;
                } else {
                    return content;
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
                    return content;
                } else {
                    return null;
                }

            }else {
                Utils.logError(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting last transactions from server (invalid data) : response code : " + code
                                + " received content : " + content);
                throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
                        , "Error getting last transactions from server (invalid data) : response code : " + code);
            }
        } catch (JSONException je) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting last transactions from server -"
                            + " received content : " + content);
            throw new ServerFetchException(UAAppErrorCodes.JSON_ERROR
                    ,"Error getting last transactions from server");
        } catch (Exception e) {
            if (e instanceof UAException) {
                try {
                    throw e;
                } catch (IOException e1) {
			Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR
                            ,"Error getting last transactions from server :"
                                    + " received responseString : " + responseString);
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR
                    ,"Error getting last transactions from server :"
                            + " received responseString : " + responseString);
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
                    , "Error getting last transactions from server :"
                    + " received responseString : " + responseString);
        }

    }

    public List<String> callTransactionLogService (String appId, String projectId, String key)
            throws AppCriticalException, ServerFetchException {

        Utils.logInfo("TRANSACTION LOG :: ", "TRANSACTION LOG SERVICE CALLED");
        List<String> content = fetchFromServer(appId, projectId, key);
        if (content == null || content.isEmpty()) {
            return null;
        } else{
            for(String log : content){
                Utils.logInfo("Log :: ", log);
            }
        }
        return content;
    }
}
