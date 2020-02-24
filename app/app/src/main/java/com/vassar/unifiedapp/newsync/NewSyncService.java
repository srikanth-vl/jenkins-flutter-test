package com.vassar.unifiedapp.newsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;

public class NewSyncService extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private String mCallingActivity = null;
    private SharedPreferences mAppPreferences;
    private UnifiedAppDBHelper mDbHelper;
    private ObjectMapper mJsonObjectMapper;
    private String mUserId;
    private String mUserToken;
    private AppMetaData mAppMetaData;

    public NewSyncService(Context context, String callingActivity) {
        mContext = context;
        mCallingActivity = callingActivity;
        mJsonObjectMapper = new ObjectMapper();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        initializeData();

//        callAppMetaConfig()

        return null;
    }

    private void initializeData() {
        mDbHelper = new UnifiedAppDBHelper(mContext);
        mAppPreferences = mContext.getSharedPreferences(Constants.APP_PREFERENCES_KEY
                , Context.MODE_PRIVATE);
        mUserId = mAppPreferences.getString(Constants.USER_ID_PREFERENCE_KEY,
                Constants.USER_ID_PREFERENCE_DEFAULT);
        mUserToken = mAppPreferences.getString(Constants.USER_TOKEN_PREFERENCE_KEY,
                Constants.USER_TOKEN_PREFERENCE_DEFAULT);
        ConfigFile appMetaConfigFile = mDbHelper.getConfigFile(Constants.DEFAULT_USER_ID,
                Constants.APP_META_CONFIG_DB_NAME);
        if (appMetaConfigFile != null && appMetaConfigFile.getConfigContent() != null &&
                !appMetaConfigFile.getConfigContent().isEmpty()) {
            try {
                mAppMetaData = mJsonObjectMapper.readValue(appMetaConfigFile.getConfigContent(), AppMetaData.class);
            } catch ( IOException e) {
                Utils.logError(LogTags.APP_MD_CONFIG, "failed to create AppMetaData object from config json :: " + appMetaConfigFile.getConfigContent());
                e.printStackTrace();

            }
        }
    }
}
