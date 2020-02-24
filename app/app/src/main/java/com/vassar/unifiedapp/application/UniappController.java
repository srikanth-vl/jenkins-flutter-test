package com.vassar.unifiedapp.application;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.AuthenticationService;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.listener.AuthenticationServiceListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.ui.LoginActivity;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UniappController extends Application {

    UnifiedAppDBHelper mDbHelper;
    SharedPreferences mAppPreferences;
    UserMetaData mUserMetaData;
    String mUserId;
    AppMetaData mAppMetaConfig;
    RootConfig mRootConfig;
    ObjectMapper mJsonObjectMapper;
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize DBHelper
        mDbHelper = new UnifiedAppDBHelper(this);
        mJsonObjectMapper = new ObjectMapper();
        // Fetch appMetaData from the DB
        ConfigFile appMetaConfig =  mDbHelper.getConfigFile(Constants.DEFAULT_USER_ID,
                Constants.APP_META_CONFIG_DB_NAME);

        if (appMetaConfig == null || appMetaConfig.getConfigContent().isEmpty()) {
            Toast.makeText(this, "APP META NOT AVAILABLE", Toast.LENGTH_SHORT).show();
            // Fetching the app meta config from the server
            fetchAppMetaData();
        } else {
            Toast.makeText(this, "APP META AVAILABLE", Toast.LENGTH_SHORT).show();
            // Initializing singleton class
            try {
                mAppMetaConfig = mJsonObjectMapper.readValue(appMetaConfig.getConfigContent(), AppMetaData.class);
            } catch (IOException e) {
                Utils.logError(LogTags.UNIAPP_CONTROLLER, "Could not create AppMetaData object from Json Object: "+ appMetaConfig.getConfigContent());
                e.printStackTrace();
            }
        }

        // LOGIN - Check if the user is logged in or not
        mAppPreferences = getSharedPreferences(Constants.APP_PREFERENCES_KEY
                , MODE_PRIVATE);

        boolean isUserLoggedIn = mAppPreferences.getBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY,
                Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT);

        if (isUserLoggedIn) {
            // User is logged in
            mUserId = mAppPreferences.getString(Constants.USER_ID_PREFERENCE_KEY, Constants.USER_ID_PREFERENCE_DEFAULT);

            if (mUserId == null) {
                // Error - no user id, even after being online
                Utils.logError(LogTags.USER_DATA_ERROR, "No User ID found for the user" +
                         "-- cannot continue");
            } else {
                // User Id present
                mUserMetaData = mDbHelper.getUserMeta(mUserId);
                /** TODO : Not accounted for fetching the RootConfig file here which is needed to get
                 * the expiry intervals (client and server). Confirm with Sir
                 */
                if (mUserMetaData == null) {
                    // Error - no user meta data for the user
                    Utils.logError(LogTags.USER_DATA_ERROR, "No User Data in DB" +
                            "-- cannot continue");

                    //TODO : Add toast and should we redirect user to Login Screen?
                } else {
                    ConfigFile rootConfigFile = mDbHelper.getConfigFile(mUserId, Constants.ROOT_CONFIG_DB_NAME);
                    try{
                    mRootConfig = mJsonObjectMapper.readValue(rootConfigFile.getConfigContent(), RootConfig.class);
                    } catch (IOException e) {
                        Utils.logError(LogTags.UNIAPP_CONTROLLER, "Could not create RootConfig object from Json Object: "+ rootConfigFile.getConfigContent());
                        e.printStackTrace();
                    }

                    // User meta is valid
                    // TODO: Should we get this server expiry in the root config????

                    // TODO : Uncomment
                    if (true) {
//                    if (daysSinceTokenGenerated > 30) {
                        // Server session expired
                        logout();

                    } else if (!isOfflineSessionValid(mUserMetaData, mRootConfig)) {
                        // User has been using the app in offline mode for more than the client expiry interval
                        // Show warning, allow him to move to login page. DO NOT let him submit projects
                        Toast.makeText(this, "You can only view the projects but not " +
                                "submit till you use the app in online mode", Toast.LENGTH_SHORT).show();
                        moveToHome();
                    } else {
                        // User is logged in, and session is valid (server and client)
                        moveToHome();
                    }
                }
            }
        } else {
            // User not logged in, move to Login page
            moveToLogin();
        }


        // CALLED IF THE USER IS NOT LOGGED IN AND IS REDIRECTED TO THE LOGIN SCREEN
        // LOGIN FUNCTIONALITY
        // TODO: Move to constants file
        String username = "apiiatp_pmu_admin";
        String password = "vassar";

        if (Utils.getInstance().isOnline(this)) {
            // User is online
            onlineLogin(username, password);
        } else {
            // User is offline
            offlineLogin(username, password, mUserMetaData);
        }


        // POST LOGIN FUNCTIONALITY
        ConfigFile rootConfigFile = mDbHelper.getConfigFile(mUserId, Constants.ROOT_CONFIG_DB_NAME);
        if (rootConfigFile != null && rootConfigFile.getConfigContent() != null && !rootConfigFile
                .getConfigContent().isEmpty()) {
            // User has a valid root config file


        } else {
            if (!Utils.getInstance().isOnline(this)) {
                // TODO: Show error message - network down, and show logout button(Can we log him out. Confirm with sir)
                // Error - No root config and no internet to fetch
                Utils.logError(LogTags.CONFIG_ERROR, "No RootConfig in DB and no internet conn" +
                        "-- cannot continue");
            } else {
                // The user is online, call RootConfig service
                String token = mUserMetaData.mToken;
                Map<String, String> params = createRootConfigParameters(username, token);
//                RootConfigService rootConfigService = new RootConfigService(mDbHelper, new RootConfigServiceListener() {
//                    @Override
//                    public void onRootConfigTaskCompleted(String rootConfigString) {
//                        // Get all images from the Root Config
//                        if (rootConfigString != null && !rootConfigString.isEmpty()) {
//                            // Root Config has been successfully downloaded and saved to DB
//
//                            // Proceed to ProjectType
//
//                        } else {
//                            // Error - No valid root config returned
//                            Utils.logError(LogTags.CONFIG_ERROR, "No RootConfig returned from server" +
//                                    "-- cannot continue");
//                        }
//                    }
//
//                    @Override
//                    public void onTokenExpired(Map<String, String> params) {
//                        // Token has expired on the backend
//
//                    }
//
//                    @Override
//                    public void onRootConfigTaskFailed(String message) {
//
//                    }
//                }, this);
//                rootConfigService.callRootConfigService();
            }
        }


        // Post RootConfig fetch, move to checking for ProjectType
        // Assumption : A valid rootConfig has been fetched from the DB

        if(mRootConfig != null) {
            // Root Config exists for the user
            if (mRootConfig.mApplications != null && mRootConfig.mApplications.size() > 0) {
                // User has Project Types assigned to him

                // Iterating through all the project types assigned to the user
                for (ProjectTypeModel projectTypeModel : mRootConfig.mApplications) {
                    // Read all project type configurations from DB

                    if (projectTypeModel.mAppId != null && !projectTypeModel.mAppId.isEmpty()) {
                        ProjectTypeConfiguration projectFormConfig = mDbHelper.getProjectFormForApp(mUserId, projectTypeModel.mAppId);

                        if (projectFormConfig == null) {
                            // No project type exists

                            if (!Utils.getInstance().isOnline(this)) {
                                // TODO: Show error message - network down, and show logout button(Can we log him out. Confirm with sir)
                                // Error - No root config and no internet to fetch
                                Utils.logError(LogTags.CONFIG_ERROR, "No ProjectType in DB and no internet conn" +
                                        "-- cannot continue");
                            } else {
                                // Have internet connection, can fetch ProjectType
//                                ProjectTypeService projectTypeService = new ProjectTypeService(mDbHelper, new ProjectTypeServiceListener() {
//                                    @Override
//                                    public void onProjectTypeTaskCompleted(String projectTypeString) {
//                                        // TODO: Initialize in Application Singleton
//                                    }
//
//                                    @Override
//                                    public void onProjectTypeTaskFailed(String message) {
//                                        // TODO : How do we handle this case??
//                                    }
//
//                                    @Override
//                                    public void onTokenExpired(Map<String, String> params) {
//                                        // TODO : How do we handle this case??
//                                    }
//                                }, this);
                                // The second parameter is null as it is the first time that we are requesting for the ProjectType
//                                projectTypeService.callProjectTypeConfigService(projectTypeModel.mAppId, null);
                            }

                        } else {
                            // Project type present
                            // TODO: Load projectType in memory
                        }

                    } else {
                        // Error - No app Id present for a project Type
                        Utils.logError(LogTags.CONFIG_ERROR, "No App Id returned for a ProjectType" +
                                "-- cannot continue");
                    }
                }

            } else {
                // No applications for this user
                Toast.makeText(this, getResources().getString(R.string.NO_APPLICATIONS_FOR_USER), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Error - No valid root config returned
            Utils.logError(LogTags.CONFIG_ERROR, "No RootConfig for user" +
                    "-- cannot continue");
        }


        // ProjectList
        // Assumption : A valid rootConfig has been fetched from the DB
        if(mRootConfig != null) {
            // Root Config exists for the user
            if (mRootConfig.mApplications != null && mRootConfig.mApplications.size() > 0) {
                // User has Project Types assigned to him

                // Iterating through all the project types assigned to the user
                for (ProjectTypeModel projectTypeModel : mRootConfig.mApplications) {
                    // Read all project type configurations from DB

                    if (projectTypeModel.mAppId != null && !projectTypeModel.mAppId.isEmpty()) {

                        ProjectList projectList = mDbHelper.getProjectsForUser(mUserId, projectTypeModel.mAppId);

                        // TODO : Null will never be returned, find a way for this

                        if (projectList == null) {
                            // No project type exists
                            if (!Utils.getInstance().isOnline(this)) {
                                // TODO: Show error message - network down, and show logout button(Can we log him out. Confirm with sir)
                                // Error - No root config and no internet to fetch
                                Utils.logError(LogTags.CONFIG_ERROR, "No ProjectType in DB and no internet conn" +
                                        "-- cannot continue");
                            } else {
                                // Have internet connection, can fetch ProjectType
//                                ProjectTypeService projectTypeService = new ProjectTypeService(mDbHelper, new ProjectTypeServiceListener() {
//                                    @Override
//                                    public void onProjectTypeTaskCompleted(String projectTypeString) {
//                                        // TODO: Initialize in Application Singleton
//                                    }
//
//                                    @Override
//                                    public void onProjectTypeTaskFailed(String message) {
//                                        // TODO : How do we handle this case??
//                                    }
//
//                                    @Override
//                                    public void onTokenExpired(Map<String, String> params) {
//                                        // TODO : How do we handle this case??
//                                    }
//                                }, this);
                                // The second parameter is null as it is the first time that we are requesting for the ProjectType
//                                projectTypeService.callProjectTypeConfigService(projectTypeModel.mAppId, null);
                            }

                        } else {
                            // Project list present
                            // TODO: Load projectList in memory
                        }

                    } else {
                        // Error - No app Id present for a project Type
                        Utils.logError(LogTags.CONFIG_ERROR, "No App Id returned for a ProjectType" +
                                "-- cannot continue");
                    }
                }

            } else {
                // No applications for this user
                Toast.makeText(this, getResources().getString(R.string.NO_APPLICATIONS_FOR_USER), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Error - No valid root config returned
            Utils.logError(LogTags.CONFIG_ERROR, "No RootConfig for user" +
                    "-- cannot continue");
        }
    }

    // TODO : Uncomment
    public void fetchAppMetaData() {
//        AppMetaConfigService appMetaConfigService = new AppMetaConfigService(mDbHelper,
//                new AppMetaConfigServiceListener() {
//            @Override
//            public void AppMetaConfigTaskCompleted(String appMetaConfigString) {
               //  TODO : Store in DB
//            }
//
//            @Override
//            public void AppMetaConfigTaskFailed(String message) {
               //  TODO : Can we retry?????
//            }
//        }, this);
//        appMetaConfigService.callAppMetaDataService();
    }

    public void moveToLogin() {
        // Move to Login Screen
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void moveToHome() {
        // Move to Login Screen
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public boolean isOfflineSessionValid(UserMetaData userMetaData, RootConfig rootConfig) {
//        boolean isValid = false;
//
//        long lastServerTimestamp = userMetaData.mTimestamp;
//        long currentTimestamp = System.currentTimeMillis();
//
//        String expiryDaysStr = null;
//
//        // TODO: Get clarity on what level should the client expiry be.
//        // App level or project type level
//
//        if (rootConfig != null) {
//            for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
//                if (projectTypeModel.mAppId.equals(appId)) {
//                    expiryDaysStr = projectTypeModel.mClientExpiryDays;
//                    break;
//                }
//            }
//        }
//
//        if (expiryDaysStr != null) {
//            if (currentTimestamp < (lastServerTimestamp + (Integer.parseInt(expiryDaysStr) * 86400000))) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return true;
//        }

        return true;
    }

    private void logout() {
        // Set user as logged out
        mAppPreferences.edit().putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY,
                Constants.USER_IS_LOGGED_IN_PREFERENCE_DEFAULT).apply();

        // Move to login screen
        moveToLogin();
    }

    private void onlineLogin(String username, String password) {
        if (!username.isEmpty() && !password.isEmpty()) {
            // The user is online, network login
            Map<String, String> loginParameters = createLoginParameters(username, password);
            AuthenticationService authenticationService = new AuthenticationService(mDbHelper,
                    mAppPreferences, new AuthenticationServiceListener() {
                @Override
                public void onAuthenticationSuccessful(UserMetaData userMetaData) {
                    if (userMetaData != null) {
                        // Successful Login

                        String newToken = userMetaData.mToken;

                        // Updation of the SharedPreferences happens in the AuthenticationService

                        // Updating the usermetaData of this class
                        mUserMetaData = userMetaData;
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.DATA_INITIALIZATION_FAILED),
                                Toast.LENGTH_SHORT).show();
                        // Error - no user meta data for successful login
                        Utils.logError(LogTags.USER_DATA_ERROR, "No User Data after successful login" +
                                "-- cannot continue");
                    }
                }

                @Override
                public void onAuthenticationFailure(String message) {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }, this);
            authenticationService.callAuthenticationService(loginParameters);
        } else {
            if(username.isEmpty())
                Toast.makeText(this, getResources().getString(R.string.MISSING_USERNAME), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.MISSING_PASSWORD), Toast.LENGTH_SHORT).show();
        }
    }

    private void offlineLogin(String username, String password, UserMetaData userMetaData) {
        if (userMetaData != null) {
            // Offline data available
            if (username.equals(userMetaData.mUserId) && password.equals(userMetaData.mPassword)) {
                // Offline credentials match
                SharedPreferences.Editor editor = mAppPreferences.edit();
                editor.putString(Constants.USER_ID_PREFERENCE_KEY, username);
                editor.putBoolean(Constants.USER_IS_LOGGED_IN_PREFERENCE_KEY, true);
                editor.apply();

                moveToHome();
            } else {
                // Offline credentials do not match
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.INCORRECT_CREDENTIALS),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Offline data does'nt exist for this user
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.NO_OFFLINE_USER_DATA),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Map<String, String> createLoginParameters(String username, String password) {
        Map<String, String> loginParameters = new HashMap<>();
        loginParameters.put(Constants.LOGIN_USERNAME_KEY, username);
        loginParameters.put(Constants.LOGIN_PASSWORD_KEY, password);
        loginParameters.put(Constants.LOGIN_SUPER_APP_KEY, Constants.SUPER_APP_ID);
        return loginParameters;
    }

    private Map<String, String> createRootConfigParameters(String username, String token) {
        // TODO : Make sure this token is always picked from the UserMeta DB
        Map<String, String> rootConfigParameters = new HashMap<>();
        rootConfigParameters.put(Constants.ROOT_CONFIG_USER_ID_KEY, username);
        rootConfigParameters.put(Constants.ROOT_CONFIG_TOKEN_KEY, token);
        rootConfigParameters.put(Constants.ROOT_CONFIG_SUPER_APP_KEY, Constants.SUPER_APP_ID);
        return rootConfigParameters;
    }
}
