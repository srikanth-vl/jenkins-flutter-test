package com.vassar.unifiedapp.api;

import android.content.SharedPreferences;

import com.vassar.unifiedapp.listener.OnFormTaskCompletedListener;
import com.vassar.unifiedapp.model.FormImage;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadFormImageService {

    private FormImage mFormImage;
    private SharedPreferences mAppPreferences;
    private OnFormTaskCompletedListener mOnTaskCompletedListener;
    private String mAppId;
    private String mUserId;

    public UploadFormImageService(SharedPreferences preferences, FormImage formImage
            , OnFormTaskCompletedListener listener, String appId, String userId) {
        this.mAppPreferences = preferences;
        this.mFormImage = formImage;
        this.mOnTaskCompletedListener = listener;
        this.mAppId = appId;
        this.mUserId = userId;
    }

    protected String doInBackground(Void... voids) {
        try {
            final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
            RequestBody requestBody;

            Utils.getInstance().showLog("IMAGE PATH FROM SERVER", mFormImage.mLocalPath);
            File file = new File(mFormImage.mLocalPath);
            if (mFormImage.hasGeotag()) {
                List<String> latLng = Arrays.asList(mFormImage.mGeotag.split("\\s*,\\s*"));
                if (latLng.size() == 2) {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("superapp", Constants.SUPER_APP_ID)
                            .addFormDataPart("appid", mFormImage.mAppId)
                            .addFormDataPart("projectid", mFormImage.mProjectId)
                            .addFormDataPart("imageid", mFormImage.mUUID)
                            .addFormDataPart("token", mAppPreferences.getString
                                    (Constants.USER_TOKEN_PREFERENCE_KEY
                                            , Constants.USER_TOKEN_PREFERENCE_DEFAULT))
                            .addFormDataPart("userid", mFormImage.mUserId)
                            .addFormDataPart("syncts", String.valueOf(System.currentTimeMillis()))
                            .addFormDataPart("lat", latLng.get(0))
                            .addFormDataPart("lon", latLng.get(1))
                            .addFormDataPart("key", "")
                            .addFormDataPart("image", mFormImage.mUUID, RequestBody.create(MEDIA_TYPE_JPEG, file))
                            .build();
                } else {
                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("superapp", Constants.SUPER_APP_ID)
                            .addFormDataPart("appid", mFormImage.mAppId)
                            .addFormDataPart("projectid", mFormImage.mProjectId)
                            .addFormDataPart("imageid", mFormImage.mUUID)
                            .addFormDataPart("token", mAppPreferences.getString
                                    (Constants.USER_TOKEN_PREFERENCE_KEY
                                            , Constants.USER_TOKEN_PREFERENCE_DEFAULT))
                            .addFormDataPart("userid", mFormImage.mUserId)
                            .addFormDataPart("syncts", String.valueOf(System.currentTimeMillis()))
                            .addFormDataPart("lat", "")
                            .addFormDataPart("lon", "")
                            .addFormDataPart("key", "")
                            .addFormDataPart("image", mFormImage.mUUID, RequestBody.create(MEDIA_TYPE_JPEG, file))
                            .build();
                }
            } else {
                requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("superapp", Constants.SUPER_APP_ID)
                        .addFormDataPart("appid", mFormImage.mAppId)
                        .addFormDataPart("projectid", mFormImage.mProjectId)
                        .addFormDataPart("imageid", mFormImage.mUUID)
                        .addFormDataPart("token", mAppPreferences.getString
                                (Constants.USER_TOKEN_PREFERENCE_KEY
                                        , Constants.USER_TOKEN_PREFERENCE_DEFAULT))
                        .addFormDataPart("userid", mFormImage.mUserId)
                        .addFormDataPart("syncts", String.valueOf(System.currentTimeMillis()))
                        .addFormDataPart("key", "")
                        .addFormDataPart("image", mFormImage.mUUID, RequestBody.create(MEDIA_TYPE_JPEG, file))
                        .build();
            }

            Request request = new Request.Builder()
//                        .url("http://uniapp.vassarlabs.com:9002/api/uniapp/submitimage")
                    .url(Constants.BASE_URL + "submitimage")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            Utils.getInstance().showLog("IMAGE RESPONSE", response.toString());

            if (response != null)
                return response.body().string();
            else
                return null;

        } catch (UnknownHostException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(String s) {
        mOnTaskCompletedListener.onTaskCompleted(s, mAppId, mUserId, mFormImage);
    }
}
