package com.vassar.unifiedapp.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.media.ExifInterface;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.err.ServerFetchException;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.err.UAException;
import com.vassar.unifiedapp.listener.MediaRequestListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//

public class MediaActionService {

    private Context mContext;
    private FormMedia formMedia;
    private UnifiedAppDBHelper mDBHelper;
    private MediaRequestListener mMediaRequestListener;
    private SharedPreferences mAppPreferences;

    public MediaActionService(FormMedia formMedia, MediaRequestListener mMediaRequestListener) {
        this.mContext = UAAppContext.getInstance().getContext();
        this.mDBHelper = UAAppContext.getInstance().getDBHelper();
        this.mAppPreferences = UAAppContext.getInstance().getAppPreferences();
        this.formMedia = formMedia;
        this.mMediaRequestListener = mMediaRequestListener;
    }

    public static int getRotation(String filePath) {

        ExifInterface exif = null;
        int rotate = 0;

        try {
            if (filePath != null && !filePath.isEmpty()) {
                exif = new ExifInterface(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = ExifInterface.ORIENTATION_ROTATE_270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = ExifInterface.ORIENTATION_ROTATE_180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
            }
        }
        return rotate;
    }

    public void uploadMedia() {

        if (!Utils.getInstance().isOnline(null)) {
            Utils.logInfo(LogTags.MEDIA_THREAD, "Internet connection lost, cannot sync media to server");
            return;
        }

        if (formMedia != null) {
            File file = new File(formMedia.getLocalPath());

            if (file != null && file.exists()) {

                Utils.logInfo("media service ", "upload called");
                Map<String, String> additionalProps = formMedia.getAdditionalProps();
                if (formMedia.getMediaType() == 0) {
                    int rotation = getRotation(formMedia.getLocalPath());

                    if (additionalProps == null) {
                        additionalProps = new HashMap<>();
                    }
                    additionalProps.put("orientation", String.valueOf(rotation));
                }
                UserMetaData userMetaData = mDBHelper.getUserMeta(formMedia.getmUserId());
                String token = null;
                if (additionalProps != null && additionalProps.get(Constants.USER_TOKEN) != null && !additionalProps.get(Constants.USER_TOKEN).isEmpty()) {
                    token = additionalProps.get(Constants.USER_TOKEN);
                    additionalProps.remove(Constants.USER_TOKEN);
                } else {
                    if (userMetaData == null) {
                        Utils.logError("MEDIA SERVICE ", "USER META DATA IS NULL");
                        return;
                    }
                    if (userMetaData != null) {
                        token = userMetaData.mToken;
                    }
                }
                if (token == null) {
                    Utils.logError("MEDIA SERVICE ", "TOKEN IS NULL FOR THE USER");
                    return;
                }

                String mediaType = formMedia.getMediaTypeName(formMedia.getMediaType());

                Utils.logInfo(LogTags.MEDIA_THREAD, "form media info : media type = " + mediaType + "project id = " + formMedia.getmProjectId() + " uuid " + formMedia.getmUUID() + " userid " + formMedia.getmUserId());

                String mediaTypeAndExt = mediaType + "/" + formMedia.getMediaFileExtension();

                final MediaType MEDIA_TYPE = MediaType.parse(mediaTypeAndExt); // iF ERROR:: HANDLE


                double latitude = Constants.DEFAULT_LATITUDE;
                double longitude = Constants.DEFAULT_LONGITUDE;

                if (formMedia.ismHasGeotag()) {
                    latitude = formMedia.getLatitude();
                    longitude = formMedia.getLongitude();
                }
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String additionalProperties = objectMapper.writeValueAsString(additionalProps);
//                new Gson().toJson(formMedia.getAdditionalProps())
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("superapp", Constants.SUPER_APP_ID)
                            .addFormDataPart("appid", formMedia.getmAppId())
                            .addFormDataPart("projectid", formMedia.getmProjectId())
                            .addFormDataPart("imageid", formMedia.getmUUID())
                            .addFormDataPart("token", token)
                            .addFormDataPart("userid", formMedia.getmUserId())
                            .addFormDataPart("syncts", String.valueOf(System.currentTimeMillis()))
                            .addFormDataPart("insert_ts", String.valueOf(formMedia.getFormSubmissionTimestamp()))
                            .addFormDataPart("lat", String.valueOf(latitude))
                            .addFormDataPart("lon", String.valueOf(longitude))
                            .addFormDataPart("key", "")
                            .addFormDataPart("gps_accuracy", String.valueOf(formMedia.getAccuracy()))
                            .addFormDataPart("mediatype", mediaType)
                            .addFormDataPart("media_subtype", formMedia.getMediaSubTypeName(formMedia.getMediaSubType()))
                            .addFormDataPart("media_ext", formMedia.getMediaFileExtension())
                            .addFormDataPart("additional_props", additionalProperties)
                            .addFormDataPart("media", formMedia.getmUUID(), RequestBody.create(MEDIA_TYPE, file))
                            .build();

                    Request request = new Request.Builder()
                            .url(Constants.BASE_URL + Constants.UPLOAD_SERVICE_URL)
                            .post(requestBody)
                            .build();

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(50, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();

                    Response response = client.newCall(request).execute();

                    Utils.getInstance().showLog("IMAGE RESPONSE", response.toString());

                    if (response == null || !response.isSuccessful()) {
                        Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error uploading Media to server -- " + formMedia.toString());
                        throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error uploading Media to server -- " + formMedia.toString());
                    }
                    // Response code lies within 200-300
                    String responseString = null;
                    try {
                        responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        JSONObject resultObject = jsonObject.getJSONObject("result");
                        boolean isSucccess;
                        if (resultObject == null || resultObject.toString().isEmpty() || resultObject.toString().equals("{}")) {
                            // proper response not received from server
                            Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error uploading Media to server -- " + formMedia.toString()
                                    + " -- received responseString -- " + responseString);

                            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error uploading Media to server -- " + formMedia.toString()
                                    + " -- received responseString -- " + responseString);
                        } else {
                            isSucccess = resultObject.getBoolean(Constants.PROJECT_RESPONSE_IS_SUCCESSFUL);
                            int statusCode = resultObject.getInt(Constants.PROJECT_RESPONSE_STATUS_CODE);

                            if (isSucccess && statusCode == 200) {
                                mMediaRequestListener.onRequestSuccessful();
                            } else if (statusCode == 350) {

                                Utils.logError(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Token is expired for this user");

                                if (AppBackgroundSync.isSyncInProgress) {
                                    Thread.currentThread().interrupt();
                                }

                                mMediaRequestListener.onRequestFailed();
                                SharedPreferences.Editor editor = mAppPreferences.edit();
                                editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY
                                        , Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);
                                editor.apply();

                                Intent intent = new Intent(Constants.LOGOUT_UPDATE_BROADCAST);
                                UAAppContext.getInstance().getContext().sendBroadcast(intent);

                            } else {
                                mMediaRequestListener.onRequestFailed();
                            }
                        }

                    } catch (Exception e) {
                        if (e instanceof UAException) {
                            throw (UAException) e;
                        }
                        Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error uploading Media to server -- " + formMedia.toString()
                                + " -- received responseString -- " + responseString);

                        throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error uploading Media to server -- " + formMedia.toString()
                                + " -- received responseString -- " + responseString);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void downloadMedia() throws Exception {

//        FormImage mFormImage = mDBHelper.getFormImage(mediaRequest.getUuid());
//
//        String mediaType = mediaRequest.getMediaTypeName(mediaRequest.getMediaType());
//
//        UserMetaData userMetaData = mDBHelper.getUserMeta(mFormImage.getUserId());
//        if (userMetaData == null) {
//            Log.e("MEDIA SERVICE ", "USER META DATA IS NULL");
//            return;
//        }
//        String token = userMetaData.mToken;
//        if(token == null)
//        {
//            Log.e("MEDIA SERVICE ", "TOKEN IS NULL FOR THE USER");
//            return;
//        }
//
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("superapp", Constants.SUPER_APP_ID)
//                .addFormDataPart("appid", mFormImage.mAppId)
//                .addFormDataPart("projectid", mFormImage.mProjectId)
//                .addFormDataPart("mediaid", mFormImage.mUUID)
//                .addFormDataPart("token", token)
//                .addFormDataPart("mediatype", mediaType)
//                .addFormDataPart("userid", mFormImage.getUUID())
//                .build();
//
//        Request request = new Request.Builder()
//                .url(Constants.BASE_URL + Constants.DOWNLOAD_SERVICE_URL)
//                .post(requestBody)
//                .build();
//
//        OkHttpClient client = new OkHttpClient();
//        Response response = client.newCall(request).execute();
//
//        if (!response.isSuccessful()) {
//            mMediaRequestListener.onRequestFailed();
//            throw new IOException("Failed to download file: " + response);
//        } else{
//
//            if(response.body()!=null) {
//                File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//                FileOutputStream fos = new FileOutputStream(storageDir.getAbsolutePath() + mFormImage.mUUID + ".png");
//                fos.write(response.body().bytes());
//                fos.close();
//                mMediaRequestListener.onRequestSuccessful();
//            } else {
//                Log.v("media download service", "media file is empty");
//            }
//        }
//
    }
}