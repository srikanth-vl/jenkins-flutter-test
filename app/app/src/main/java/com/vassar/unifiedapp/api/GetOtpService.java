package com.vassar.unifiedapp.api;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.listener.GetOtpListener;
import com.vassar.unifiedapp.network.APIServices;
import com.vassar.unifiedapp.network.APIUtils;
import com.vassar.unifiedapp.utils.Constants;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetOtpService {

    private GetOtpListener mListener;

    public GetOtpService(GetOtpListener listener) {
        mListener = listener;
    }

    public void callGetOtpService(Map<String, String> requestParams) {
        APIServices apiServices;
        apiServices = APIUtils.getAPIService();
        apiServices.getOtp(requestParams).enqueue(new Callback<ResponseBody>() {
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
                            mListener.onGetOtpSuccessful();
                        } else {
                            // Login Unsuccessful
                            mListener.onGetOtpFailure(resultObject.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mListener.onGetOtpFailure(UAAppContext.getInstance().getContext().getResources().getString(R.string.SOMETHING_WENT_WRONG));
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                mListener.onGetOtpFailure(UAAppContext.getInstance().getContext().getResources().getString(R.string.NETWORK_ERROR));
            }
        });
    }
}