package com.vassar.unifiedapp.synchronization;

import android.content.Context;
import android.content.SharedPreferences;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.listener.SubmitProjectServiceListener;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.network.APIServices;
import com.vassar.unifiedapp.network.APIUtils;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectSubmissionService {

    private UnifiedAppDBHelper mDbHelper;
    private SubmitProjectServiceListener mListener;
    private String mUserId;
    private String mAppId;
    private SharedPreferences mAppPreferences;
    private Context mContext;

    public ProjectSubmissionService(Context context, String userId, String appId, SharedPreferences appPreferences,
                                UnifiedAppDBHelper dbHelper, SubmitProjectServiceListener listener) {
        mContext = context;
        mUserId = userId;
        mAppId = appId;
        mAppPreferences = appPreferences;
        mDbHelper = dbHelper;
        mListener = listener;
    }

    /** This function is called every time the user tries to sync the locally saved
     *  projects. Only one project type is submitted in one call */
    public void callProjectSyncService(Map<String, String> params, FormButton formButton, String apiEndpoint) {
        // Appending the endpoint to the URL
        String endpoint = null;
        if (formButton != null) {
            // Online Submission from Project Form
            endpoint = formButton.mApi;
        }

        if (apiEndpoint != null && !apiEndpoint.isEmpty()){
            // Offline submission, use apiEndpoint
            endpoint = apiEndpoint;
        }

        if (endpoint != null) {
            APIServices apiServices = APIUtils.getAPIService();
            apiServices.submitProjects(endpoint, params).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()) {
                        // Response code lies within 200-300
                        String responseString = null;
                        JSONObject content = null;
                        int code = 0;
                        try {
                            responseString = response.body().string();
                            Utils.getInstance().showLog("PROJECT SUBMISSION RESPONSE", responseString);
                            JSONObject jsonObject = new JSONObject(responseString);
                            JSONObject resultObject = jsonObject.getJSONObject("result");
                            code = resultObject.getInt("status");
                            if (code == 200) {

                                // Updating user's last network timestamp
                                Utils.getInstance().updateUserLastNetworkConnectionTimestamp(mDbHelper,
                                        mUserId,
                                        System.currentTimeMillis());

                                mListener.onSubmitTaskCompleted(responseString);
                            } else if (code == 350) {
                                // Token has expired
                                mListener.onTokenExpired(params, formButton);
                            } else if (code == 111 || code == 420) {
                                // Data isn't valid
                                mListener.onValidationException(resultObject.getString("message"));
                            } else {
                                mListener.onSubmitTaskFailed(resultObject.getString("message"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mListener.onSubmitTaskFailed(mContext.getResources().getString(R.string.SOMETHING_WENT_WRONG));
                        }
                    } else {
                        mListener.onSubmitTaskFailed(mContext.getResources().getString(R.string.SOMETHING_WENT_WRONG));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    mListener.onSubmitTaskFailed(mContext.getResources().getString(R.string.NETWORK_ERROR));
                }
            });
        }
    }
}
