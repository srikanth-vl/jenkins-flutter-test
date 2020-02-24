package com.vassar.unifiedapp.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.model.FormImage;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

public class PostImageUploadTask extends AsyncTask<Void, Void, Void> {


    Context mApplicationContext;
    UnifiedAppDBHelper mDbHelper;
    String mResponse;
    String mAppId;
    String mUserId;
    FormImage mFormImage;

    public PostImageUploadTask(Context context, UnifiedAppDBHelper dbHelper, String response, String appId, String userId, FormImage formImage) {
        this.mApplicationContext = context;
        this.mDbHelper = dbHelper;
        this.mResponse = response;
        this.mAppId = appId;
        this.mUserId = userId;
        this.mFormImage = formImage;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (mResponse != null && !mResponse.isEmpty()) {
            String responseString = null;
            JSONObject content = null;
            int code = 0;
            try {
                responseString = mResponse;
                Utils.getInstance().showLog("IMAGE UPLOADER RESPONSE PROJECT SUBMISSION RESPONSE",
                        responseString+String.valueOf(System.currentTimeMillis()));
                JSONObject jsonObject = new JSONObject(responseString);
                JSONObject resultObject = jsonObject.getJSONObject("result");
                code = resultObject.getInt("status");
                if (code == 200) {
                    // Deleting image from DB
                    mDbHelper.deleteFormMedia(mFormImage.mUUID);
                    // Deleting image from Storage
                    Utils.getInstance().deleteImageFromStorage(mApplicationContext, mFormImage.mLocalPath);
                }
                Utils.getInstance().showLog("IMAGE UPLOADER RESPONSE JSON CONVERTED",
                        String.valueOf(System.currentTimeMillis()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
