package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.listener.AuthenticationServiceListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AuthenticationResponse;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;

public class NewAuthenticationService extends AsyncTask<Void, Void, AuthenticationResponse> {

    private UnifiedAppDBHelper mDbHelper;
    private SharedPreferences mAppPreferences;
    private String mRequestParams;
//    private AuthenticationServiceListener mListener;
    private static final MediaType JSON = MediaType.get("application/json");

    public NewAuthenticationService(String username, String password) {
        this.mDbHelper = UAAppContext.getInstance().getDBHelper();
        this.mAppPreferences = UAAppContext.getInstance().getAppPreferences();
        this.mRequestParams = createLoginParameters(username, password);
//        this.mListener = listener;
    }

    @Override
    protected AuthenticationResponse doInBackground(Void... voids) {

        AuthenticationResponse authenticationResponse;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        okhttp3.Response response = null;
        RequestBody body = RequestBody.create(JSON, mRequestParams);
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "authenticate")
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR,"Error Authenticating from server");
            return null;
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
                Utils.logInfo(LogTags.AUTHENTICATION, "Authentication successful..." + content.toString());

                // Successful Login
                String username = content.getString("userid");
                String token = content.getString("tokenid");
                long timestamp = content.getLong("currenttime");
                String userDetails = "";
                if(content.has("userdetails")) {
                    userDetails= content.getString("userdetails");
                }

//                Utils.logInfo("USERDETAILS", userDetails);


                // currentTime is used to update user login time
                long currentTime = System.currentTimeMillis();

                UserMetaData userMetaData = new UserMetaData(getPasswordFromParams(),
                        username, token, timestamp, true, currentTime, userDetails);

                // Saving to DB
                saveToDb(userMetaData);

                // Save to AppPreferences
                // TODO : Do this only after AST2 is completed
                saveToAppPreferences(username, token);

                authenticationResponse = new AuthenticationResponse(true, null, userMetaData);
                return authenticationResponse;
//                mListener.onAuthenticationSuccessful(userMetaData);
            } else {
                // Login Unsuccessful
//                mListener.onAuthenticationFailure(resultObject.getString("message"));
                authenticationResponse = new AuthenticationResponse(false
                        , resultObject.getString("message"), null);
                return authenticationResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
//            mListener.onAuthenticationFailure(getResources().getString(R.string.SOMETHING_WENT_WRONG));
            authenticationResponse = new AuthenticationResponse(false
                    , UAAppContext.getInstance().getContext().getResources().getString(R.string.SOMETHING_WENT_WRONG), null);
            Utils.logDebug(UAAppErrorCodes.SERVER_FETCH_ERROR, "Server fetch while authenticating user");
            return authenticationResponse;
        }
    }

    private String getPasswordFromParams() {
        String password;
        try {
            JSONObject jsonObject = new JSONObject(mRequestParams);
            password = jsonObject.getString(Constants.LOGIN_PASSWORD_KEY);
        } catch (JSONException e) {
            password = null;
            e.printStackTrace();
        }
        return password;
    }

    private void saveToDb(UserMetaData userMetaData) {
        ContentValues values = new ContentValues();
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID, userMetaData.mUserId);
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_PASSWORD, userMetaData.mPassword);
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_TOKEN, userMetaData.mToken);
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME, userMetaData.mTimestamp);
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_IS_LOGGEN_IN, 1);
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_LOGIN_TS, System.currentTimeMillis());
        values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_DETAILS, userMetaData.userDetails);

        // Adding to database
        int update = mDbHelper.updateUsermeta(values, getPasswordFromParams());
        if (update == 0) {
            // If the user does'nt exist on the database already
            mDbHelper.addOrUpdateToUserMDTable(values);
        }
    }

    private String createLoginParameters(String username, String password) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.LOGIN_USERNAME_KEY, username);
            jsonObject.put(Constants.LOGIN_PASSWORD_KEY, password);
            jsonObject.put(Constants.LOGIN_SUPER_APP_KEY, Constants.SUPER_APP_ID);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveToAppPreferences(String username, String token) {
        // Making changes to shared preferences
        SharedPreferences.Editor editor = UAAppContext.getInstance().getAppPreferences().edit();
        editor.putString(Constants.USER_ID_PREFERENCE_KEY, username);
        editor.putString(Constants.USER_TOKEN_PREFERENCE_KEY, token);
        editor.apply();
    }
}
