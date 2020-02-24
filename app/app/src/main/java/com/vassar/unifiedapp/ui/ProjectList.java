package com.vassar.unifiedapp.ui;

//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.view.MenuItemCompat;
//import android.support.v7.widget.Toolbar;
//import android.text.TextUtils;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import com.google.gson.Gson;
//import com.vassar.unifiedapp.R;
//import com.vassar.unifiedapp.application.UnifiedAppApplication;
//import com.vassar.unifiedapp.context.UAAppContext;
//import com.vassar.unifiedapp.log.LogTags;
//import com.vassar.unifiedapp.model.Project;
//import com.vassar.unifiedapp.model.ProjectTypeModel;
//import com.vassar.unifiedapp.model.RootConfig;
//import com.vassar.unifiedapp.newflow.AppBackgroundSync;
//import com.vassar.unifiedapp.synchronization.SynchronizationService;
//import com.vassar.unifiedapp.synchronization.SynchronizationTask;
//import com.vassar.unifiedapp.utils.Constants;
//import com.vassar.unifiedapp.utils.Utils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.ListIterator;
//
//public class ProjectList extends BaseActivity {
//
//    private String mAppId;
//    private String mUserType;
//    private long mListCreationTimestamp;
//
//    public String mSortType = Constants.PROJECT_LIST_ALPHABETICAL_SORTING;
//    public List<String> mFilteringAttributes = null;
//
//    private ImageView mActionBarRefreshIcon;
//    private boolean mIsActionBarAnimationRunning = false;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_project_list);
//
//        initComponents(this);
//
//        initializeToolbar();
//
//        if (savedInstanceState == null) {
//            // The project list is being loaded for the first time
//            mListCreationTimestamp = System.currentTimeMillis();
//            mAppId = getIntent().getStringExtra("appId");
//            mUserType = getIntent().getStringExtra("userType");
//
//            initializeComponents();
//            loadFragment();
//        } else {
//            // The project list page is being restored
//            mListCreationTimestamp = savedInstanceState.getLong(Constants.PROJECT_LIST_CREATION_TIMESTAMP);
//            mAppId = savedInstanceState.getString(Constants.PROJECT_LIST_APP_ID);
//            mUserType = savedInstanceState.getString(Constants.PROJECT_LIST_USER_TYPE);
//
//            initializeComponents();
//        }
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString(Constants.PROJECT_LIST_APP_ID, mAppId);
//        outState.putString(Constants.PROJECT_LIST_USER_TYPE, mUserType);
//        outState.putLong(Constants.PROJECT_LIST_CREATION_TIMESTAMP, mListCreationTimestamp);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case Constants.FILTER :
//                if (resultCode == Activity.RESULT_OK) {
//                    // Refresh the list with the given parameters
//                    List<String> projectIds = null;
//
//                    Bundle extras = data.getExtras();
//                    List<String> valuesList = new ArrayList<>();
//                    if (mFilteringAttributes != null && mFilteringAttributes.size() > 0) {
//                        for (String attribute : mFilteringAttributes) {
//                            if (extras.getString(attribute) != null && !extras.getString(attribute).isEmpty()) {
//                                String val = extras.getString(attribute);
//                                String[] valArr = val.split("\\$\\$");
//                                if(valArr.length == 2) {
//                                    int index = Integer.parseInt(val.split("\\$\\$")[1]);
//                                    valuesList.add(index, val.split("\\$\\$")[0]);
//                                }
//                            }
//                        }
//
//                        String dimensionValues = TextUtils.join("#", valuesList);
//                        dimensionValues = dimensionValues.toLowerCase();
//                        if (dimensionValues != null && dimensionValues.length() > 0) {
//                            //dimensionValues = dimensionValues.substring(0, dimensionValues.length() - 1);
//                            projectIds = mDBHelper.getProjectIdsForFilterQuery(mUserId, mAppId, dimensionValues);
//                        }
//                    }
//
//                    if (projectIds != null && projectIds.size() > 0) {
//                        // We have filtered projects
//                        mFilteredProjects.clear();
//                        mFilteredProjects.addAll(mProjects);
//                        ListIterator<Project> listIterator = mFilteredProjects.listIterator();
//                        while(listIterator.hasNext()) {
//                            if(!projectIds.contains(listIterator.next().mProjectId)) {
//                                listIterator.remove();
//                            }
//                        }
//                    }
//
//                    if (mFilteredProjects.size() == 0) {
//                        // No result for filtering
//                        mFragmentTransaction = mFragmentManager.beginTransaction();
//                        ProjectListFragment fragment = new ProjectListFragment(mProjects, null, mListCreationTimestamp, mSortType);
//                        mFragmentTransaction.replace(R.id.project_list_root_view, fragment);
//                        mFragmentTransaction.commitAllowingStateLoss();
//                    } else {
//                        mFragmentTransaction = mFragmentManager.beginTransaction();
//                        ProjectListFragment fragment = new ProjectListFragment(mProjects, mFilteredProjects, mListCreationTimestamp, mSortType);
//                        mFragmentTransaction.replace(R.id.project_list_root_view, fragment);
//                        mFragmentTransaction.commitAllowingStateLoss();
//                    }
//
//                    Utils.getInstance().showLog("FILTERING", "RECEIVED");
//                } else {
//                    // Filtering cancelled
//                    Toast.makeText(this, getResources().getString(R.string.NO_FILTERING_PARAMETERS), Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
//    }
//
//    private void initializeToolbar() {
//        Toolbar toolbar = findViewById(R.id.project_list_toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(getResources().getString(R.string.app_name));
//        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
//    }
//
//    /** Initializes variables, loads the ProjectListConfig */
//    private void initializeComponents() {
//        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
//
//        if (rootConfig == null || rootConfig.mApplications == null) {
//            // Error - Root Config inconsistencies
//            Utils.logError(LogTags.PROJECT_LIST, "No Root Config for user after AppStartupThread2" +
//                    "-- redirecting user to login screen");
//            invalidateLogin();
//            moveToLoginScreen();
//        }
//
//        for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
//            if (projectTypeModel.mAppId.equals(mAppId)) {
//                if (projectTypeModel.mSortType != null && !projectTypeModel.mSortType.isEmpty()) {
//                    mSortType = projectTypeModel.mSortType;
//                }
//                if (projectTypeModel.mFilteringAttributes != null && projectTypeModel.mFilteringAttributes.size() > 0) {
//                    mFilteringAttributes = projectTypeModel.mFilteringAttributes;
//                }
//                break;
//            }
//        }
//
//        mProjectListConfigString = "";
//
//        showDialog(DIALOG_LOADING);
//        Utils.getInstance().showLog("BEFORE ASYNCTASK", String.valueOf(System.currentTimeMillis()));
//        mProjectListConfig = mDBHelper.getProjectsForUser(mUserId, mAppId);
//        Utils.getInstance().showLog("AFTER ASYNCTASK", String.valueOf(System.currentTimeMillis()));
//        removeDialog(DIALOG_LOADING);
//        if (mProjectListConfig != null) {
//            mProjects.clear();
//            for (Project project : mProjectListConfig.mProjects) {
//                if (mUserType.equalsIgnoreCase(project.mUserType))
//                    mProjects.add(project);
//            }
//        }
//    }
//
//    private void loadFragment() {
//        ProjectListFragment fragment = new ProjectListFragment(mProjects,
//                mFilteredProjects, mListCreationTimestamp, mSortType);
//        getSupportFragmentManager().beginTransaction().add(R.id.project_list_root_view,
//                fragment).commit();
//    }
//
//    /** This function is called when a project is clicked */
//    public void moveToProjectFormActivity(Project project) {
//        Intent intent = new Intent(this, ProjectFormActivity.class);
//        intent.putExtra("appId", mAppId);
//        intent.putExtra("userId", UAAppContext.getInstance().getUserID());
//        intent.putExtra("projectId", project.mProjectId);
//        startActivity(intent);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.action_sign_out:
//                if (Utils.getInstance().isOnline(this)) {
//                    callLogoutService();
//                } else {
//                    showErrorMessageAndFinishActivity(getResources().getString(R.string.NO_OFFLINE_LOGOUT)
//                            , false);
//                }
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//    }
//
//    /** This function is called to create the options menu */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.project_list_menu, menu);
//
//        MenuItem item = menu.findItem(R.id.project_list_sync);
//        MenuItemCompat.setActionView(item, R.layout.home_sync_action_button);
//        View view = MenuItemCompat.getActionView(item);
//        mActionBarRefreshIcon = (ImageView) view.findViewById(R.id.refresh_image_to_rotate);
//
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                initiateSync();
//            }
//        });
//
//        MenuItem filterItem = menu.findItem(R.id.project_list_filter);
//        MenuItemCompat.setActionView(filterItem, R.layout.project_list_filter_button);
//        View filterView = MenuItemCompat.getActionView(filterItem);
//
//        filterView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mFilteringAttributes != null && mFilteringAttributes.size() > 0) {
//                    startFilterForResult();
//                } else {
//                    showErrorMessageAndFinishActivity(getResources().getString(R.string.filter_not_available), false);
//                }
//            }
//        });
//
//        if (SynchronizationService.mIsSynchronizationRunning) {
//            if (mActionBarRefreshIcon != null) {
//                startSyncAnimation();
//            }
//        } else {
//            if (mActionBarRefreshIcon != null) {
//                stopSyncAnimation();
//            }
//        }
//
//        return true;
//    }
//
//    private void startFilterForResult() {
//        Gson gson = new Gson();
//
//        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
//
//        if (rootConfig == null || rootConfig.mApplications == null) {
//            // Error - Root Config inconsistencies
//            Utils.logError(LogTags.PROJECT_LIST, "No Root Config for user after AppStartupThread2" +
//                    "-- redirecting user to login screen");
//            invalidateLogin();
//            moveToLoginScreen();
//        }
//
//        if (rootConfig != null) {
//            Intent filter = new Intent(this, FilterScreenOriginal.class);
//            filter.putExtra(Constants.FILTER_APP_ID, mAppId);
//            filter.putExtra(Constants.FILTER_USER_ID, UAAppContext.getInstance().getUserID());
//            filter.putExtra(Constants.FILTER_ROOT_CONFIG, gson.toJson(rootConfig));
//            startActivityForResult(filter, Constants.FILTER);
//        } else {
//            showErrorMessageAndFinishActivity(Constants.TRY_AGAIN_LATER, false);
//        }
//    }
//
//    /** This function is called every time the user tries to sync the locally saved
//     *  projects */
//    private void initiateSync() {
//        AppBackgroundSync appBackgroundSync = new AppBackgroundSync();
//        appBackgroundSync.execute();
//    }
//
//    public void startSyncAnimation() {
//        if (mActionBarRefreshIcon == null)
//            return;
//        Animation rotation = AnimationUtils.loadAnimation(UAAppContext.getInstance().getContext(), R.anim.rotate_refresh);
//        rotation.setRepeatCount(Animation.INFINITE);
//        mActionBarRefreshIcon.startAnimation(rotation);
//        mIsActionBarAnimationRunning = true;
//    }
//
//    public void stopSyncAnimation() {
//        if (mActionBarRefreshIcon == null)
//            return;
//        mActionBarRefreshIcon.clearAnimation();
//        mIsActionBarAnimationRunning = false;
//    }
//
//    private void updateFragment() {
//        mListCreationTimestamp = System.currentTimeMillis();
//        ProjectListFragment fragment = new ProjectListFragment(mProjects, mFilteredProjects, mListCreationTimestamp, mSortType);
//        getSupportFragmentManager().beginTransaction().replace(R.id.project_list_root_view, fragment).commit();
//    }
//
//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//        // Refresh the project list here
//        if (AppBackgroundSync.isSyncInProgress) {
//            if (mActionBarRefreshIcon != null) {
//                startSyncAnimation();
//            }
//        } else {
//            if (mActionBarRefreshIcon != null) {
//                stopSyncAnimation();
//            }
//        }
//        this.registerReceiver(mPostSyncUpdateReceiver, new IntentFilter(Constants.POST_SYNC_BROADCAST_ACTION));
//        this.registerReceiver(mPreSyncUpdateReceiver, new IntentFilter(Constants.PRE_SYNC_BROADCAST_ACTION));
//        this.registerReceiver(mFailedSyncUpdateReceiver, new IntentFilter(Constants.FAILED_SYNC_BROADCAST));
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterRecievers();
//        this.unregisterReceiver(mPostSyncUpdateReceiver);
//        this.unregisterReceiver(mPreSyncUpdateReceiver);
//        this.unregisterReceiver(mFailedSyncUpdateReceiver);
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
//    }
//
//    private BroadcastReceiver mPostSyncUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
////            if (intent.hasExtra("completed")) {
////                if (intent.getBooleanExtra("completed", false)) {
////                    initializeComponents();
////                    updateFragment();
////                } else {
////                    if (Utils.getInstance().isOnline(context)) {
////                        showErrorMessageAndFinishActivity(Constants.SYNC_FAILED, false);
////                    } else {
////                        showErrorMessageAndFinishActivity(getResources().getString(R.string.CHECK_INTERNET_CONNECTION), false);
////                    }
////                }
////            } else {
////                initializeComponents();
////                updateFragment();
////            }
//
//            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Post Sync Update Receiver -- ProjectList -- called");
//            if (mIsActionBarAnimationRunning) {
//                stopSyncAnimation();
//            }
//        }
//    };
//
//    private BroadcastReceiver mPreSyncUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Pre Sync Update Receiver -- Home -- called");
//            if (!mIsActionBarAnimationRunning) {
//                startSyncAnimation();
//            }
//        }
//    };
//
//    private BroadcastReceiver mFailedSyncUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String message = intent.getStringExtra("message");
//            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Post Sync Update Receiver -- " +
//                    "Home -- called" + message);
//        }
//    };
//}
