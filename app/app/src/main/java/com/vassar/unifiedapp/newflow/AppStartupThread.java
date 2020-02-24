package com.vassar.unifiedapp.newflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.vassar.unifiedapp.application.helper.AppMDConfigHelper;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.AppCriticalException;
import com.vassar.unifiedapp.err.AppOfflineException;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppStartupThreadResponse;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.ui.LoginActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

public class AppStartupThread extends AsyncTask<Void, Void, AppStartupThreadResponse> {

    @Override
    protected AppStartupThreadResponse doInBackground(Void... voids) {

        Utils.logInfo(LogTags.APP_STARTUP, "Start of App Startup Thread");

        boolean isLoggedIn = false;

        Boolean result = true;
        try {

            initializeAppMDConfig();

            isLoggedIn = initiateLogin();

        } catch (AppCriticalException | AppOfflineException a) {
            result = false;
            a.printStackTrace();
            // Show error message and exit
        }

        AppStartupThreadResponse response = new AppStartupThreadResponse(result, isLoggedIn);

        return response;
    }

    private void initializeAppMDConfig()
            throws AppCriticalException, AppOfflineException {
        // Load App MD Config from DB
        //   If does not exist then read from server --> AppMDConfigService.readFromServer() & Save to DB

        AppMDConfigHelper.getInstance().initAppMDConfig();

        Utils.logInfo(LogTags.APP_STARTUP, "Completed APP MD Config initialization...");
    }

    private boolean initiateLogin() {

        Context context = UAAppContext.getInstance().getContext();

        SharedPreferences appPreferences = context.getSharedPreferences(Constants.APP_PREFERENCES_KEY, context.MODE_PRIVATE);

        boolean isLoggedIn = checkSharedPreference(appPreferences);

        Utils.logInfo(LogTags.APP_STARTUP, "XXXX ... isLoggedIn" + isLoggedIn);

        if (isLoggedIn) {
            // User is logged in
            String mUserId = appPreferences.getString(Constants.USER_ID_PREFERENCE_KEY, Constants.USER_ID_PREFERENCE_DEFAULT);

            UAAppContext.getInstance().setUserID(mUserId);

            Utils.logInfo(LogTags.APP_STARTUP, "XXXX ... UserId" + mUserId);

            if (mUserId == null) {
                // Error - no user id, even after being online
                Utils.logError(LogTags.USER_DATA_ERROR, "No User ID found for the user" +
                        "-- redirecting user to login screen");
                moveToLogin();
            } else {
                // User Id present
                UserMetaData userMetaData = UAAppContext.getInstance().getDBHelper().getUserMeta(mUserId);

                if (userMetaData == null) {
                    // Error - no user meta data for the user
                    Utils.logError(LogTags.USER_DATA_ERROR, "No User Data in DB" +
                            "-- redirecting user to login screen");
                    moveToLogin();
                } else {
                    boolean isValid = isServerSessionValid(userMetaData.mLoginTs,
                            UAAppContext.getInstance().getAppMDConfig().mServerSessionExpiry);
                    // User meta is valid
                    if (!isValid) {
                        // Server session expired
                        logout(appPreferences);
                    } else {
                        // User is logged in, and session is valid (server and client)
                        UAAppContext.getInstance().setUserID(mUserId);

                        //TODO: Initialize App Startup thread 2
//                        moveToHome();
                    }
                }
            }
        } else {
            // User not logged in, move to Login page
            Utils.logInfo(LogTags.APP_STARTUP, "XXXX ... UserNotLoggedIn");
//            moveToLogin();
        }

        return isLoggedIn;
    }

    private boolean checkSharedPreference(SharedPreferences appPreferences) {

        // LOGIN - Check if the user is logged in or not
        boolean isUserLoggedIn = appPreferences.getBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY,
                Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);

        if (!isUserLoggedIn) {
            return false;
        }

        return true;
    }

    private boolean isServerSessionValid(Long lastLoginTs, Long serverSessionExpiry) {
        // The serverSessionExpiry is the number of days the server token expires in
        boolean isValid = true;
        long millisInDay = 86400000;

        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastLoginTs) > (serverSessionExpiry * millisInDay))
            isValid = false;

        return isValid;
    }

    private void logout(SharedPreferences appPreferences) {
        // Set user as logged out
        appPreferences.edit().putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY,
                Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT).apply();

        // Move to login screen
        moveToLogin();
    }

    private void moveToLogin() {
        // Move to Login Screen
        Intent intent = new Intent(UAAppContext.getInstance().getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        UAAppContext.getInstance().getContext().startActivity(intent);
    }
}
