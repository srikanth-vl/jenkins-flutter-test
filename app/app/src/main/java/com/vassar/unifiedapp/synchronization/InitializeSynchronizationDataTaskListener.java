package com.vassar.unifiedapp.synchronization;

import android.content.SharedPreferences;

import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.model.FormImage;
import com.vassar.unifiedapp.model.ProjectSubmission;
import com.vassar.unifiedapp.model.RootConfig;

import java.util.ArrayList;

public interface InitializeSynchronizationDataTaskListener {
    void initializeSynchronizationDataFetched(SharedPreferences sharedPreferences,
                                              UnifiedAppDBHelper dbHelper,
                                              String userId, String userToken,
                                              RootConfig rootConfig, int unsyncedProjectCount,
                                              ArrayList<ProjectSubmission> projectSubmissions,
                                              ArrayList<FormImage> formImages);
}
