package com.vassar.unifiedapp.synchronization;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.vassar.unifiedapp.api.AuthenticationService;
import com.vassar.unifiedapp.asynctask.PostImageUploadTask;
import com.vassar.unifiedapp.asynctask.SerializeProjectTypeTask;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.listener.AuthenticationServiceListener;
import com.vassar.unifiedapp.listener.OnFormTaskCompletedListener;
import com.vassar.unifiedapp.listener.SubmitProjectServiceListener;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.model.FormImage;
import com.vassar.unifiedapp.model.ProjectSpecificForms;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeConfigurationFetchData;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SynchronizationService {

    private Context mContext;
    private SharedPreferences mAppPreferences;
    private UnifiedAppDBHelper mDbHelper;
    private String mUserId;
    private String mUserToken;
    private RootConfig mRootConfig;
    private int mTotalUnsyncedProjectCount;
    private String mCallingActivity = null;
    private int mProjectSubmissionCounter;
    private ArrayList<ProjectSubmission> mProjectSubmissions = new ArrayList<>();
    private ArrayList<FormImage> mFormImages = new ArrayList<>();
    private ArrayList<ProjectTypeConfigurationFetchData> mProjectTypeFetchData = null;
    private int mProjectTypeToSyncCount = 0;
    private int mCurrentProjectTypeToSync;
    private int mImagesToSubmitCount = 0;

    public static boolean mIsSynchronizationRunning = false;

    public SynchronizationService(Context context, String callingActivity) {
        mContext = context;
        mCallingActivity = callingActivity;
    }

    /** Initiates the sync service */
    public void initiateSynchronizationService() {
        Intent intent = new Intent(Constants.PRE_SYNC_BROADCAST_ACTION);
        mContext.sendBroadcast(intent);

        Utils.getInstance().showLog("SYNC TASK INITIALIZATION STARTED", String.valueOf(System.currentTimeMillis()));
        InitializeSynchronizationDataTask task = new InitializeSynchronizationDataTask(mContext,
                new InitializeSynchronizationDataTaskListener() {
                    @Override
                    public void initializeSynchronizationDataFetched(SharedPreferences sharedPreferences,
                                                                     UnifiedAppDBHelper dbHelper, String userId, String userToken,
                                                                     RootConfig rootConfig, int unsyncedProjectCount,
                                                                     ArrayList<ProjectSubmission> projectSubmissions,
                                                                     ArrayList<FormImage> formImages) {
                        mAppPreferences = sharedPreferences;
                        mDbHelper = dbHelper;
                        mUserId = userId;
                        mUserToken = userToken;
                        mRootConfig = rootConfig;
                        mTotalUnsyncedProjectCount = unsyncedProjectCount;
                        mProjectSubmissions.clear();
                        mProjectSubmissions.addAll(projectSubmissions);
                        mFormImages.clear();
                        mFormImages.addAll(formImages);
                        onDataInitializationComplete();
                    }
                });
        task.execute();
    }

    private void onDataInitializationComplete() {
        Utils.getInstance().showLog("SYNC TASK INITIALIZATION COMPLETED", String.valueOf(System.currentTimeMillis()));
        if (mUserId != null && mUserToken != null && mRootConfig !=null) {
            // Sync all projects and images
            if(mProjectSubmissions.size() > 0) {
                // There are projects to sync
                mProjectSubmissionCounter = 0;
                Utils.getInstance().showLog("SYNC TASK PROJECT STARTED", String.valueOf(System.currentTimeMillis()));
                fireProjectSubmissionService();
            } else {
                // No projects to submit
                if (mFormImages.size() > 0) {
                    // Initiating form images sync
                    initiateImageSync();
                } else {
                    // Initiating root config sync
//                    initiateRootConfigSync();
                }
            }
        } else {
            // Cannot proceed without userId and token
            updateData(false);
        }
    }

    private void fireProjectSubmissionService() {
        if (mProjectSubmissionCounter < mTotalUnsyncedProjectCount) {
            ProjectSubmission projectSubmission = mProjectSubmissions.get(mProjectSubmissionCounter);
            Map<String, String> requestParams = createProjectSubmissionRequestParams(projectSubmission);
            String apiEndpoint = projectSubmission.mApi;
            String appId = projectSubmission.mAppId;
            Long timestamp = projectSubmission.mTimestamp;
            String projectId = projectSubmission.mProjectId;
            ProjectSubmissionService projectSubmissionService = new ProjectSubmissionService(mContext,
                    mUserId, appId, mAppPreferences, mDbHelper, new SubmitProjectServiceListener() {
                @Override
                public void onSubmitTaskCompleted(String submitString) {
//                    mDbHelper.deleteProject(appId, mUserId, timestamp, projectId);
                    mProjectSubmissionCounter ++;
                    fireProjectSubmissionService();
                }

                @Override
                public void onSubmitTaskFailed(String message) {
                    mProjectSubmissionCounter ++;
                    fireProjectSubmissionService();
                }

                @Override
                public void onTokenExpired(Map<String, String> params, FormButton formButton) {
                    Map<String, String> loginParameters = createAuthenticationRequestParams();
                    AuthenticationService authenticationService = new AuthenticationService(
                            mDbHelper, mAppPreferences, new AuthenticationServiceListener() {
                        @Override
                        public void onAuthenticationSuccessful(UserMetaData userMetaData) {
                            if (userMetaData != null) {
                                // Successful Login
                                mUserToken = userMetaData.mToken;
                                fireProjectSubmissionService();
                            }
                        }

                        @Override
                        public void onAuthenticationFailure(String message) {
                            // Token remains null
                            updateData(false);
                        }
                    }, mContext);
                    authenticationService.callAuthenticationService(loginParameters);
                }

                @Override
                public void onValidationException(String message) {
//                    mDbHelper.deleteProject(appId, mUserId, timestamp, projectId);
                    mProjectSubmissionCounter ++;
                    fireProjectSubmissionService();
                }
            });
            projectSubmissionService.callProjectSyncService(requestParams, null, apiEndpoint);
        } else {
            Utils.getInstance().showLog("SYNC TASK PROJECT COMPLETED", String.valueOf(System.currentTimeMillis()));
            // All projects submitted
            if (mFormImages.size() > 0) {
                // Initiating form images sync
                initiateImageSync();
            } else {
                // Initiating root config sync
//                initiateRootConfigSync();
            }
        }
    }

    /** This function is called to fire image upload for all form images. We do not wait
     * for the response and delete the images that are uploaded successfully */
    private void initiateImageSync() {
        mImagesToSubmitCount = mFormImages.size();
        Utils.getInstance().showLog("SYNC TASK IMAGE STARTED", String.valueOf(System.currentTimeMillis()));
        for (FormImage formImage : mFormImages) {
            Utils.getInstance().uploadFormImage(mAppPreferences, formImage,
                    formImage.mAppId, mUserId, new OnFormTaskCompletedListener() {
                        @Override
                        public void onTaskCompleted(String response, String appId,
                                                    String userId, FormImage formImage) {
                            mImagesToSubmitCount --;
                            Utils.getInstance().showLog("SYNC TASK IMAGE ASYNC STARTED", String.valueOf(System.currentTimeMillis()));
                            if (response != null) {
                                PostImageUploadTask task = new PostImageUploadTask(mContext,
                                        mDbHelper, response, appId, userId, formImage);
                                task.execute();
                            }
                            Utils.getInstance().showLog("SYNC TASK IMAGE ASYNC COMPLETED", String.valueOf(System.currentTimeMillis()));
                            if (mImagesToSubmitCount == 0) {
//                                initiateRootConfigSync();
                            }
                        }
                    });
        }
    }

    /** This function is called to refresh the root configuration for the current user */
//    private void initiateRootConfigSync() {
//        Utils.getInstance().showLog("SYNC TASK ROOTCONFIG STARTED", String.valueOf(System.currentTimeMillis()));
//        Map<String, String> rootConfigParams = createRootConfigRequestParams();
//        RootConfigService rootConfigService = new RootConfigService(mDbHelper, new RootConfigServiceListener() {
//            @Override
//            public void onRootConfigTaskCompleted(String rootConfigString) {
//                // Get all images from the Root Config
//                if (rootConfigString != null && !rootConfigString.isEmpty()) {
//                    Gson gson = new Gson();
//                    RootConfig rootConfig = null;
//                    rootConfig = gson.fromJson(rootConfigString, RootConfig.class);
//                    if (rootConfig != null) {
//                        Utils.getInstance().showLog("SYNC TASK ROOTCONFIG COMPLETED", String.valueOf(System.currentTimeMillis()));
//                        // Initiating ProjectTypeConfig
//                        initiateProjectTypeSync();
//                    }
//                } else {
//                    // Sync Failed
//                    updateData(false);
//                }
//            }
//
//            @Override
//            public void onTokenExpired(Map<String, String> params) {
//                // Token has expired on the backend
//                Map<String, String> loginParameters = createAuthenticationRequestParams();
//                AuthenticationService authenticationService = new AuthenticationService(
//                        mDbHelper, mAppPreferences, new AuthenticationServiceListener() {
//                    @Override
//                    public void onAuthenticationSuccessful(UserMetaData userMetaData) {
//                        if (userMetaData != null) {
//                            // Successful Login
//                            mUserToken = userMetaData.mToken;
//                            initiateRootConfigSync();
//                        }
//                    }
//
//                    @Override
//                    public void onAuthenticationFailure(String message) {
//                        // Token remains null
//                        // Sync Failed
//                        updateData(false);
//                    }
//                }, mContext);
//                authenticationService.callAuthenticationService(loginParameters);
//            }
//
//            @Override
//            public void onRootConfigTaskFailed(String message) {
//                // Sync Failed
//                updateData(false);
//            }
//        }, mContext);
//        rootConfigService.callRootConfigService();
//    }

    private void initiateProjectTypeSync() {
        Utils.getInstance().showLog("SYNC TASK PROJECTTYPE STARTED", String.valueOf(System.currentTimeMillis()));
        initializeProjectTypeFetchData();
        // Has network and can get configuration files
        if (mProjectTypeFetchData != null && mUserToken != null) {
            mProjectTypeToSyncCount = mProjectTypeFetchData.size();
            mCurrentProjectTypeToSync = 0;
            Utils.getInstance().showLog("HOMESYNC", "Project Type Count "+ mProjectTypeToSyncCount);
            if (mProjectTypeToSyncCount > 0) {
                callProjectTypeSync();
            } else {
                // TODO: No project type and list to sync. Sync complete, Broadcast message
                // TODO: Remove all projectTypesConfigs and ProjectListConfigs
                updateData(true);
            }
        } else {
            // Sync Failed
            updateData(false);
        }
    }

    private void callProjectTypeSync() {
        if (mCurrentProjectTypeToSync < mProjectTypeToSyncCount) {
            ProjectTypeConfigurationFetchData data = mProjectTypeFetchData.get(mCurrentProjectTypeToSync);
            Map<String, String> projectTypeParams = createProjectTypeRequestParams(data.mProjectTypeId,
                    String.valueOf(data.mFormVersion));
            // TODO: Uncomment
//            ProjectTypeService projectTypeService = new ProjectTypeService(mDbHelper, new ProjectTypeServiceListener() {
//                @Override
//                public void onProjectTypeTaskCompleted(String projectTypeString) {
//                    if (projectTypeString != null) {
//                        ProjectTypeConfiguration projectTypeConfiguration = null;
//                        try {
//                            projectTypeConfiguration = new SerializeProjectTypeTask()
//                                    .execute(projectTypeString).get();
//                        } catch (ExecutionException e) {
//                            e.printStackTrace();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (projectTypeConfiguration != null) {
//                            Map<String, Object> projectListConfigurationParams =
//                                    createProjectListRequestParams(projectTypeConfiguration);
////                            callProjectListSync(projectListConfigurationParams);
//                        }
//                    } else {
//                        // No new version of the form, fetching last version from database and
//                        // getting project lists
//                        Map<String, Object> projectListConfigurationParams =
//                                createProjectListRequestParamsFromExistingProjectType(projectTypeParams);
//                        if (projectListConfigurationParams != null) {
////                            callProjectListSync(projectListConfigurationParams);
//                        } else {
//                            mCurrentProjectTypeToSync ++;
//                            callProjectTypeSync();
//                        }
//                    }
//                }
//
//                @Override
//                public void onProjectTypeTaskFailed(String message) {
//                    mCurrentProjectTypeToSync ++;
//                    callProjectTypeSync();
//                }
//
//                @Override
//                public void onTokenExpired(Map<String, String> params) {
//                    // Token has expired on the backend
//                    Map<String, String> loginParameters = createAuthenticationRequestParams();
//                    AuthenticationService authenticationService = new AuthenticationService(
//                            mDbHelper, mAppPreferences, new AuthenticationServiceListener() {
//                        @Override
//                        public void onAuthenticationSuccessful(UserMetaData userMetaData) {
//                            if (userMetaData != null) {
//                                // Successful Login
//                                mUserToken = userMetaData.mToken;
//                                initiateProjectTypeSync();
//                            }
//                        }
//
//                        @Override
//                        public void onAuthenticationFailure(String message) {
//                            // Token remains null
//                            updateData(false);
//                        }
//                    }, mContext);
//                    authenticationService.callAuthenticationService(loginParameters);
//                }
//            }, mContext);


//            projectTypeService.callProjectTypeConfigService(projectTypeParams);
        } else {
            Utils.getInstance().showLog("SYNC TASK PROJECTTYPE COMPLETED", String.valueOf(System.currentTimeMillis()));
            // All Projects Synced
            updateData(true);
        }
    }

//    private void callProjectListSync(Map<String, Object> params) {
//        ProjectListService projectListService = new ProjectListService(mContext, mDbHelper, new ProjectListServiceListener() {
//            @Override
//            public void onProjectListTaskCompleted(String projectListString) {
//                // Task has been completed
//                mCurrentProjectTypeToSync ++;
//                Utils.getInstance().showLog("HOMESYNC", "Project Type Count " +mCurrentProjectTypeToSync);
//                callProjectTypeSync();
//            }
//
//            @Override
//            public void onProjectListTaskFailed(String message) {
//                mCurrentProjectTypeToSync ++;
//                Utils.getInstance().showLog("HOMESYNC", "Project Type Count " +mCurrentProjectTypeToSync);
//                callProjectTypeSync();
//            }
//
//            @Override
//            public void onTokenExpired(Map<String, Object> params) {
//                mCurrentProjectTypeToSync ++;
//                Utils.getInstance().showLog("HOMESYNC", "Project Type Count " +mCurrentProjectTypeToSync);
//                callProjectTypeSync();
//            }
//        });
//        //TODO : Uncomment
////        projectListService.callProjectListConfigService(params);
//    }

    private void initializeProjectTypeFetchData() {
        // Checking current ProjectType versions
        if (mRootConfig != null) {
            ArrayList<String> appIds = new ArrayList<>();
            mProjectTypeFetchData = new ArrayList<>();
            int numOfProjectTypes = mRootConfig.mApplications.size();
            for (int i=0, k=0; i<numOfProjectTypes; i++) {
                String appId = mRootConfig.mApplications.get(i).mAppId;
                int flag = 0;
                for (int j=0; j<numOfProjectTypes; j++) {
                    String parentAppId = mRootConfig.mApplications.get(j).mParentAppId;
                    if (appId.equals(parentAppId)) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    appIds.add(k, mRootConfig.mApplications.get(i).mAppId);
                    k++;
                    Utils.getInstance().showLog("App without child", mRootConfig.mApplications.get(i).mAppId);
                }
            }

            for (int i=0; i<appIds.size(); i++) {
                mProjectTypeFetchData.add(i, new ProjectTypeConfigurationFetchData
                        (appIds.get(i), Constants.PROJECT_TYPE_FORM_VERSION_DEFAULT));
            }
        } else {
            mProjectTypeFetchData = null;
        }
    }

    private Map<String, String> createProjectSubmissionRequestParams
            (ProjectSubmission projectSubmission) {
        JSONObject requestObject = new JSONObject();
        JSONArray submitArray = new JSONArray();
        Map<String, String> params = new HashMap<>();

        try {
            JSONObject projectObject = new JSONObject();
            projectObject.put(Constants.PROJECT_SUBMIT_FORM_ID_KEY, projectSubmission.mFormId);
            projectObject.put(Constants.PROJECT_SUBMIT_MD_INSTANCE_ID_KEY, projectSubmission.mMdInstanceId);
            projectObject.put(Constants.PROJECT_SUBMIT_PROJECT_ID_KEY, projectSubmission.mProjectId);
            projectObject.put(Constants.PROJECT_SUBMIT_USER_TYPE_KEY, projectSubmission.mUserType);
            projectObject.put(Constants.PROJECT_SUBMIT_INSERT_TS_KEY, projectSubmission.mTimestamp);
            JSONObject jsonObject = new JSONObject(projectSubmission.mFields);
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.PROJECT_SUBMIT_FIELDS_KEY);
            projectObject.put(Constants.PROJECT_SUBMIT_FIELDS_KEY, jsonArray);
            submitArray.put(projectObject);
            requestObject.put(Constants.PROJECT_SUBMIT_SUBMIT_DATA_KEY, submitArray);
            requestObject.put(Constants.PROJECT_SUBMIT_SUPER_APP_KEY, Constants.SUPER_APP_ID);
            requestObject.put(Constants.PROJECT_SUBMIT_USER_ID_KEY, mUserId);
            requestObject.put(Constants.PROJECT_SUBMIT_TOKEN_KEY, mUserToken);
            requestObject.put(Constants.PROJECT_SUBMIT_APP_ID_KEY, projectSubmission.mAppId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.put(Constants.PROJECT_SUBMISSION_PARAMETER_KEY, requestObject.toString());
        Utils.getInstance().showLog("REQUEST OBJECT", requestObject.toString());
        return params;
    }

    private Map<String, String> createAuthenticationRequestParams() {
        UserMetaData userMetaData = mDbHelper.getUserMeta(mUserId);
        Map<String, String> loginParameters = new HashMap<>();
        loginParameters.put(Constants.LOGIN_USERNAME_KEY, userMetaData.mUserId);
        loginParameters.put(Constants.LOGIN_PASSWORD_KEY, userMetaData.mPassword);
        loginParameters.put(Constants.LOGIN_SUPER_APP_KEY, Constants.SUPER_APP_ID);
        return loginParameters;
    }

    private Map<String, String> createRootConfigRequestParams() {
        Map<String, String> rootConfigParameters = new HashMap<>();
        rootConfigParameters.put(Constants.ROOT_CONFIG_USER_ID_KEY, mUserId);
        rootConfigParameters.put(Constants.ROOT_CONFIG_TOKEN_KEY, mUserToken);
        rootConfigParameters.put(Constants.ROOT_CONFIG_SUPER_APP_KEY, Constants.SUPER_APP_ID);
        return rootConfigParameters;
    }

    private Map<String, String> createProjectTypeRequestParams(String projectTypeId,
                                                               String formVersion) {
        Map<String, String> projectTypeConfigurationParams = new HashMap<>();
        projectTypeConfigurationParams.put(Constants.PROJECT_TYPE_CONFIG_USER_ID_KEY, mUserId);
        projectTypeConfigurationParams.put(Constants.PROJECT_TYPE_CONFIG_TOKEN_KEY, mUserToken);
        projectTypeConfigurationParams.put(Constants.PROJECT_TYPE_CONFIG_SUPER_APP_KEY, Constants.SUPER_APP_ID);
        projectTypeConfigurationParams.put(Constants.PROJECT_TYPE_CONFIG_APP_ID_KEY, projectTypeId);
        projectTypeConfigurationParams.put(Constants.PROJECT_TYPE_CONFIG_FORM_VERSION_KEY, formVersion);
        return projectTypeConfigurationParams;
    }

    private Map<String, Object> createProjectListRequestParams
            (ProjectTypeConfiguration projectTypeConfiguration) {
        Map<String, Object> projectListConfigurationParams = new HashMap<>();
        projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_USER_ID_KEY
                , mUserId);
        projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_TOKEN_KEY
                , mUserToken);
        projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_SUPER_APP_KEY
                , Constants.SUPER_APP_ID);
        projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_APP_ID_KEY
                , projectTypeConfiguration.mProjectTypeId);

        // Fetching MetaDataInstanceId
        ArrayList<String> mdInstanceIds = new ArrayList<>();
        Map<String, Map<String, ProjectSpecificForms>> projectSpecificForms
                = projectTypeConfiguration.mContent;
        Iterator iterator = projectSpecificForms.keySet().iterator() ;
        while (iterator.hasNext())   {
            String key =  (String) iterator.next();
            Map<String, ProjectSpecificForms> actionForms = projectSpecificForms.get(key);
            Iterator actionIterator = actionForms.keySet().iterator() ;
            while (actionIterator.hasNext())   {
                String actionKey =  (String) actionIterator.next();
                ProjectSpecificForms form = actionForms.get(actionKey);
                String mdInstanceId = form.mMetaDataInstanceId;
                mdInstanceIds.add(mdInstanceId);
            }
        }
        projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_MD_INSTANCE_ID_KEY
                , mdInstanceIds);

        Utils.getInstance().showLog("HOME"
                ,"PROJECT LIST CONFIG CALLED"
                        + projectTypeConfiguration.mProjectTypeId);
        return projectListConfigurationParams;
    }

    private Map<String, Object> createProjectListRequestParamsFromExistingProjectType
            (Map<String, String> projectTypeParams) {
        String projectTypeConfigurationString = null;
//        String projectTypeConfigurationString = mDbHelper.getConfigFile
//                (Constants.PROJECT_TYPE_CONFIG_DB_NAME
//                        + projectTypeParams.get(Constants.PROJECT_TYPE_CONFIG_APP_ID_KEY)
//                        + projectTypeParams.get(Constants.PROJECT_TYPE_CONFIG_USER_ID_KEY));
        Map<String, Object> projectListConfigurationParams = null;
        if (projectTypeConfigurationString != null) {
            ProjectTypeConfiguration projectTypeConfiguration = null;
            try {
                projectTypeConfiguration = new SerializeProjectTypeTask()
                        .execute(projectTypeConfigurationString).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (projectTypeConfiguration != null) {
                projectListConfigurationParams = new HashMap<>();
                projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_USER_ID_KEY
                        , mUserId);
                projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_TOKEN_KEY
                        , mUserToken);
                projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_SUPER_APP_KEY
                        , Constants.SUPER_APP_ID);
                projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_APP_ID_KEY
                        , projectTypeConfiguration.mProjectTypeId);

                ArrayList<String> mdInstanceIds = new ArrayList<>();
                // Fetching MetaDataInstanceId
                Map<String, Map<String, ProjectSpecificForms>> projectSpecificForms
                        = projectTypeConfiguration.mContent;
                Iterator iterator = projectSpecificForms.keySet().iterator() ;
                while (iterator.hasNext())   {
                    String key =  (String) iterator.next();
                    Map<String, ProjectSpecificForms> actionForms = projectSpecificForms.get(key);
                    Iterator actionIterator = actionForms.keySet().iterator() ;
                    while (actionIterator.hasNext())   {
                        String actionKey =  (String) actionIterator.next();
                        ProjectSpecificForms form = actionForms.get(actionKey);
                        String mdInstanceId = form.mMetaDataInstanceId;
                        mdInstanceIds.add(mdInstanceId);
                    }
                }
                projectListConfigurationParams.put(Constants.PROJECT_LIST_CONFIG_MD_INSTANCE_ID_KEY
                        , mdInstanceIds);

                Utils.getInstance().showLog("HOME"
                        ,"PROJECT LIST CONFIG CALLED"
                                + projectTypeConfiguration.mProjectTypeId);
            }
        }
        return projectListConfigurationParams;
    }

    private void updateData(boolean completedSuccessfully) {
        mIsSynchronizationRunning = false;
        Intent intent = new Intent(Constants.POST_SYNC_BROADCAST_ACTION);
        intent.putExtra("completed", completedSuccessfully);
        mContext.sendBroadcast(intent);
    }
}
