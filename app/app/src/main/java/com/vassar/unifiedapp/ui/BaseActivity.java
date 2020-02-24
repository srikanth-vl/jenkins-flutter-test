package com.vassar.unifiedapp.ui;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.asynctask.MediaRequestHandlerTask;
import com.vassar.unifiedapp.asynctask.TextDataRequestHandler;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.network.APIServices;
import com.vassar.unifiedapp.network.APIUtils;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.receiver.GPSChangeReceiver;
import com.vassar.unifiedapp.receiver.NetworkChangeReceiver;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseActivity extends AppCompatActivity {

    public static final int DIALOG_LOADING = 1;
    public static final int ERROR_MESSAGE_DELAY = 2000;
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected ActionBar mActionBar;
    protected UnifiedAppDBHelper mDBHelper;
    protected APIServices mAPIService;
    protected SharedPreferences mAppPreferences;

    private NetworkChangeReceiver mNetworkChangeReceiver;
    private GPSChangeReceiver mGpsChangeReceiver;

    private AppMetaData appMetaConfig;

    protected void initComponents(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mDBHelper = new UnifiedAppDBHelper(context);
        mAPIService = APIUtils.getAPIService();
        mAppPreferences = getSharedPreferences(Constants.APP_PREFERENCES_KEY
                , MODE_PRIVATE);
    }

    protected void hideActionBar(Context context) {
        mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.hide();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOADING:
                final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.splash_progress_dialog);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                return dialog;

            default:
                return null;
        }
    }

    public boolean checkIfAppSupportsProperty(String propertyName) {
        return PropertyReader.getBooleanProperty(propertyName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                if (Utils.getInstance().isOnline(this)) {
                    callLogoutService();
                } else {
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.NO_OFFLINE_LOGOUT)
                            , false);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void callLogoutService() {
        String userId = mAppPreferences.getString(Constants.USER_ID_PREFERENCE_KEY
                , Constants.USER_ID_PREFERENCE_DEFAULT);
        final UserMetaData userMetaData;
        if (userId != null) {
            userMetaData = mDBHelper.getUserMeta(userId);
            if (userMetaData != null) {
                showDialog(DIALOG_LOADING);
                Map<String, String> logoutParameters = new HashMap<>();
                logoutParameters.put(Constants.LOGOUT_USER_ID_KEY, userMetaData.mUserId);
                logoutParameters.put(Constants.LOGOUT_TOKEN_KEY, userMetaData.mToken);
                logoutParameters.put(Constants.LOGOUT_SUPER_APP_KEY, Constants.SUPER_APP_ID);
                mAPIService.logout(logoutParameters).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
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
                                    long currentTime = System.currentTimeMillis();

                                    ContentValues values = new ContentValues();
                                    values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_USER_ID, userMetaData.mUserId);
                                    values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_PASSWORD, userMetaData.mPassword);
                                    values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_TOKEN, userMetaData.mToken);
                                    values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME, currentTime);
                                    values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_IS_LOGGEN_IN, 0);
                                    values.put(UnifiedAppDbContract.UserMetaEntry.COLUMN_LAST_LOGIN_TS, currentTime);

                                    int update = mDBHelper.addOrUpdateToUserMDTable(values);
                                    SharedPreferences.Editor editor = mAppPreferences.edit();
                                    editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY
                                            , Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);
                                    editor.apply();
                                    dismissDialog(DIALOG_LOADING);
                                    Intent intent = new Intent(mContext, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    dismissDialog(DIALOG_LOADING);
                                    showErrorMessageAndFinishActivity(resultObject.getString("message"), false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                dismissDialog(DIALOG_LOADING);
                                showErrorMessageAndFinishActivity(getResources().getString(R.string.SOMETHING_WENT_WRONG), false);
                            }
                        } else {
                            dismissDialog(DIALOG_LOADING);
                            showErrorMessageAndFinishActivity(getResources().getString(R.string.SOMETHING_WENT_WRONG), false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        dismissDialog(DIALOG_LOADING);
                        showErrorMessageAndFinishActivity(getResources().getString(R.string.NETWORK_ERROR), false);
                    }
                });
            } else {
                showErrorMessageAndFinishActivity(getResources().getString(R.string.DATABASE_ERROR), false);
            }
        } else {
            showErrorMessageAndFinishActivity(getResources().getString(R.string.SOMETHING_WENT_WRONG), false);
        }
    }

    protected void invalidateLogin() {
        SharedPreferences.Editor editor = mAppPreferences.edit();
        editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY
                , Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);
        editor.apply();
    }

    protected void showErrorMessageAndFinishActivity(String message
            , boolean shouldFinishActivity) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        if (shouldFinishActivity) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, ERROR_MESSAGE_DELAY);
        }
    }

    public void moveToLoginScreen() {
        Intent intentLogin = new Intent(mContext, LoginActivity.class);
        startActivity(intentLogin);
        finish();
    }

    protected void moveToHomeScreen() {
        Intent intentHome = new Intent(mContext, HomeActivity.class);
        startActivity(intentHome);
        finish();
    }

    protected void registerReceivers() {
        // Receiver that detects network and syncs projects
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkChangeReceiver, intentFilter);

        // Receiver that detects GPS status change
        mGpsChangeReceiver = new GPSChangeReceiver();
        IntentFilter gpsChangeIntentFilter = new IntentFilter();
        gpsChangeIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mGpsChangeReceiver, gpsChangeIntentFilter);
    }

    @Override
    protected void onDestroy() {
        if(mDBHelper != null) {
            mDBHelper.close();
        }
        super.onDestroy();
    }

    protected void unregisterRecievers() {
        if (mNetworkChangeReceiver != null) {
            try {
                unregisterReceiver(mNetworkChangeReceiver);
            }catch (Exception e){
                //Already registered receiver
                e.printStackTrace();
            }
        }
        if (mGpsChangeReceiver != null) {
            try {
                unregisterReceiver(mGpsChangeReceiver);
            }catch (Exception e){
                //Already registered receiver
                e.printStackTrace();
            }
        }
    }

    public void startLogoutService() {
        Utils.getInstance().cancelAsyncThreads();
        if (Utils.getInstance().isOnline(null)) {
            callLogoutService();
        } else {
            invalidateLogin();
            moveToLoginScreen();
        }
    }

    public List<ProjectTypeModel> getRootProjectTypes(RootConfig rootConfig) {

        List<ProjectTypeModel> rootProjectTypes = new ArrayList<>();

        for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
            if (projectTypeModel.mParentAppId.equals(Constants.SUPER_APP_ID))
                rootProjectTypes.add(projectTypeModel);
        }
        return rootProjectTypes;
    }

    public void showUnsyncedCountWindow(View view) {

        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
        int unsyncedMediaCount = 0;
        int unsyncedProjectCount = 0;
        for (ProjectTypeModel projectTypeModel : getRootProjectTypes(rootConfig)) {
            unsyncedMediaCount += Utils.getInstance().getUnsyncedMediaCount(projectTypeModel.mAppId);
            unsyncedProjectCount += Utils.getInstance().getUnsyncedProjectCount(projectTypeModel.mAppId);
        }

        TooltipWindow popupWindow = new TooltipWindow(getBaseContext());
        popupWindow.showToolTip(view);
        View popupContent = popupWindow.contentView;
        TextView media_count = popupContent.findViewById(R.id.unsync_media_count);
        TextView project_count = popupContent.findViewById(R.id.unsync_project_count);

        media_count.setText(getString(R.string.unsynced_media)+" : " + unsyncedMediaCount);
        project_count.setText(getString(R.string.unsynced_projects)+" : " + unsyncedProjectCount);
    }

    public int getUnsyncedProjectAndMediaCount() {

        int unsyncedProjectCount = 0;
        int unsyncedMediaCount = 0;
        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
        if (rootConfig != null) {
            for (ProjectTypeModel projectTypeModel : getRootProjectTypes(rootConfig)) {
                unsyncedProjectCount += Utils.getInstance().getUnsyncedProjectCount(projectTypeModel.mAppId);
                unsyncedMediaCount += Utils.getInstance().getUnsyncedMediaCount(projectTypeModel.mAppId);
            }
        }
        return unsyncedMediaCount + unsyncedProjectCount;
    }

    private void checkforAppMetaConfig() {
        appMetaConfig = UAAppContext.getInstance().getAppMDConfig();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkforAppMetaConfig();
        try {

            if (appMetaConfig != null && appMetaConfig.mEnableForceUpdate) {
                registerReceivers();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkforAppMetaConfig();
        try {
            if (appMetaConfig != null && appMetaConfig.mEnableForceUpdate) {
                unregisterRecievers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Configuration config = newBase.getResources().getConfiguration();
            //Update your config with the Locale i. e. saved in SharedPreferences
            String locale = UAAppContext.getInstance().getLocale();
            config.setLocale(new Locale(locale));
            newBase = newBase.createConfigurationContext(config);

        }
        super.attachBaseContext(newBase);
    }
    public void initiateManualSync() {

        TextDataRequestHandler textDataRequestHandler = new TextDataRequestHandler();
        textDataRequestHandler.execute();

        AppBackgroundSync appBackgroundSync = new AppBackgroundSync();
        appBackgroundSync.execute();

        MediaRequestHandlerTask mediaRequestHandlerTask = new MediaRequestHandlerTask();
        mediaRequestHandlerTask.execute();
    }
}
