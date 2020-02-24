package com.vassar.unifiedapp.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.listener.AuthenticationServiceListener;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.network.APIServices;
import com.vassar.unifiedapp.network.APIUtils;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationService {

    private UnifiedAppDBHelper mDbHelper;
    private SharedPreferences mAppPreferences;
    private AuthenticationServiceListener mListener;
    private Context mContext;

    public AuthenticationService(UnifiedAppDBHelper dbHelper, SharedPreferences sharedPreferences,
                                 AuthenticationServiceListener listener, Context context) {
        mDbHelper = dbHelper;
        mAppPreferences = sharedPreferences;
        mListener = listener;
        mContext = context;
    }

    public void callAuthenticationService(Map<String, String> requestParams) {
        APIServices apiServices;
        apiServices = APIUtils.getAPIService();
        apiServices.authenticate(requestParams).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
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

                            Utils.getInstance().showLog("LOGIN SUCCESSFUL :", content.toString());

                            // Successful Login
                            String username = content.getString("userid");
                            String token = content.getString("tokenid");
                            long timestamp = content.getLong("currenttime");
                            String userDetails = content.getString("userdetails");

                            long currentTime = System.currentTimeMillis();

                            UserMetaData userMetaData = new UserMetaData(requestParams.get(Constants.LOGIN_PASSWORD_KEY),
                                    username, token, timestamp, true, currentTime, userDetails);

                            ContentValues values = new ContentValues();
                            values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID, username);
                            values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_PASSWORD, requestParams.get(Constants.LOGIN_PASSWORD_KEY));
                            values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_TOKEN, token);
                            values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME, timestamp);
                            values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_IS_LOGGEN_IN, 1);
                            values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_LOGIN_TS, currentTime);

                            // Adding to database
                            int update = mDbHelper.updateUsermeta(values, requestParams.get(Constants.LOGIN_USERNAME_KEY));
                            if (update == 0) {
                                // If the user does'nt exist on the database already
                                mDbHelper.addToDatabase(UnifiedAppDbContract.UserMetaEntry.TABLE_USER, values);
                            }

                            // Making changes to shared preferences
                            SharedPreferences.Editor editor = mAppPreferences.edit();
                            editor.putString(Constants.USER_ID_PREFERENCE_KEY, username);
                            editor.putString(Constants.USER_TOKEN_PREFERENCE_KEY, token);
                            editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY, true);
                            editor.apply();

                            mListener.onAuthenticationSuccessful(userMetaData);
                        } else {
                            // Login Unsuccessful
                            mListener.onAuthenticationFailure(resultObject.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mListener.onAuthenticationFailure(mContext.getResources().getString(R.string.SOMETHING_WENT_WRONG));
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                mListener.onAuthenticationFailure(mContext.getResources().getString(R.string.NETWORK_ERROR));
            }
        });
    }
}
