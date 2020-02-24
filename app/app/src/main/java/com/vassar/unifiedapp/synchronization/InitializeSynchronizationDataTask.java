package com.vassar.unifiedapp.synchronization;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.FormImage;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.ProjectSubmissionUploadStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InitializeSynchronizationDataTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private InitializeSynchronizationDataTaskListener mListener;

    public InitializeSynchronizationDataTask(Context context,
                   InitializeSynchronizationDataTaskListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        SharedPreferences appPreferences;
        UnifiedAppDBHelper dbHelper;
        String userId;
        String userToken;
        RootConfig rootConfig = null;
        int totalUnsyncedProjectCount = 0;
        ArrayList<ProjectSubmission> projectSubmissions = new ArrayList<>();
        ArrayList<FormImage> formImages = new ArrayList<>();

        dbHelper = new UnifiedAppDBHelper(mContext);
        appPreferences = mContext.getSharedPreferences(Constants.APP_PREFERENCES_KEY
                , Context.MODE_PRIVATE);
        userId = appPreferences.getString(Constants.USER_ID_PREFERENCE_KEY,
                Constants.USER_ID_PREFERENCE_DEFAULT);
        userToken = appPreferences.getString(Constants.USER_TOKEN_PREFERENCE_KEY,
                Constants.USER_TOKEN_PREFERENCE_DEFAULT);

        if (userId != null && userToken != null) {

            // Getting a count for total number of projects to submit and the projects
            String rootConfigString = null;
            ConfigFile configFile = dbHelper.getConfigFile(userId,Constants.
                    ROOT_CONFIG_DB_NAME);
            if (configFile != null && configFile.getConfigContent() != null &&
                    !configFile.getConfigContent().isEmpty()) {
                rootConfigString = configFile.getConfigContent();
            }
            if (rootConfigString != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    rootConfig = objectMapper.readValue(rootConfigString, RootConfig.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (rootConfig != null) {
                    projectSubmissions.clear();
                    for (ProjectTypeModel projectTypes : rootConfig.mApplications) {
                        List<ProjectSubmissionUploadStatus> uploadStatusList = new ArrayList<>();
                        uploadStatusList.add(ProjectSubmissionUploadStatus.UNSYNCED);
                        uploadStatusList.add(ProjectSubmissionUploadStatus.SERVER_ERROR);
                        List<ProjectSubmission> submissions = dbHelper
                                .getProjectsToSubmit(userId, projectTypes.mAppId, uploadStatusList);
                        if (submissions != null && submissions.size() > 0) {
                            totalUnsyncedProjectCount += submissions.size();
                            projectSubmissions.addAll(submissions);
                        }
                    }
                }
            }
        }

        mListener.initializeSynchronizationDataFetched(appPreferences, dbHelper, userId,
                userToken, rootConfig, totalUnsyncedProjectCount, projectSubmissions, formImages);

        return null;
    }
}
