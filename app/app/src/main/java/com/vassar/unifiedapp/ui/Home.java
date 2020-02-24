//package com.vassar.unifiedapp.ui;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.view.MenuItemCompat;
//import android.support.v7.widget.Toolbar;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//
//import com.vassar.unifiedapp.R;
//import com.vassar.unifiedapp.application.UnifiedAppApplication;
//import com.vassar.unifiedapp.context.UAAppContext;
//import com.vassar.unifiedapp.log.LogTags;
//import com.vassar.unifiedapp.model.ProjectList;
//import com.vassar.unifiedapp.model.ProjectTypeModel;
//import com.vassar.unifiedapp.model.RootConfig;
//import com.vassar.unifiedapp.newflow.AppBackgroundSync;
//import com.vassar.unifiedapp.synchronization.AlarmHelper;
//import com.vassar.unifiedapp.synchronization.SynchronizationService;
//import com.vassar.unifiedapp.utils.Constants;
//import com.vassar.unifiedapp.utils.Utils;
//
//import java.util.ArrayList;
//
//public class Home extends BaseActivity {
//
//    private ImageView mActionBarRefreshIcon;
//    public boolean mIsActionBarAnimationRunning = false;
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Utils.logInfo(LogTags.HOME_SCREEN, "Saving instance state -- Home");
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dashboard);
//
//        initComponents(this);
//        initializeToolbar();
//
//        if (savedInstanceState == null) {
//            // This screen is being loaded for the first time
//
//            // Job Scheduler for AppBackgroundSync thread
//            AlarmHelper.scheduleAlarm(((UnifiedAppApplication) getApplicationContext()).mContext);
//
//            RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
//            if (rootConfig == null) {
//                // Error - no root config
//                Utils.logError(LogTags.ROOT_CONFIG, "No Root Config for user after AppStartupThread2" +
//                        "-- redirecting user to login screen");
//                invalidateLogin();
//                moveToLoginScreen();
//            }
//            if (rootConfig.mApplications == null) {
//                // Error - no root config
//                Utils.logError(LogTags.ROOT_CONFIG, "Application array in RootConfig is null -- error case" +
//                        "-- redirecting user to login screen");
//                invalidateLogin();
//                moveToLoginScreen();
//            }
//
//            // Loading a list fragment with the root level applications
//            loadGridFragment(rootConfig.mApplications, getRootProjectTypes(rootConfig));
//        }
//    }
//
//    private void initializeToolbar() {
//        Toolbar toolbar = findViewById(R.id.home_toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(getResources().getString(R.string.app_name));
//        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
//    }
//
//    /** This function checks for the super app children and loads them in a grid fragment */
//    private ArrayList<ProjectTypeModel> getRootProjectTypes(RootConfig rootConfig) {
//
//        ArrayList<ProjectTypeModel> rootProjectTypes = new ArrayList<>();
//
//        for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
//            if (projectTypeModel.mParentAppId.equals(Constants.SUPER_APP_ID))
//                rootProjectTypes.add(projectTypeModel);
//        }
//
//        return rootProjectTypes;
//    }
//
//    private void loadGridFragment(ArrayList<ProjectTypeModel> flattenedAppList,
//                                  ArrayList<ProjectTypeModel> rootProjectTypes) {
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        GridFragment fragment = new GridFragment(this, flattenedAppList
//                , rootProjectTypes, UAAppContext.getInstance().getDBHelper()
//                , UAAppContext.getInstance().getUserID());
//        fragmentManager.beginTransaction().add(R.id.home_root_view, fragment).commit();
//    }
//
//    /** This function is called to add a fragment with subapps for an
//     * application to the HomeActivity */
//    public void addNewGridFragment(ArrayList<ProjectTypeModel> projectTypes) {
//        GridFragment gridFragment = new GridFragment(this,
//                UAAppContext.getInstance().getRootConfig().mApplications,
//                projectTypes, UAAppContext.getInstance().getDBHelper(),
//                UAAppContext.getInstance().getUserID());
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.home_root_view, gridFragment)
//                .addToBackStack(null).commit();
//    }
//
//    /** This function is called when an application leads to a project list */
//    public void addNewProjectListActivity(ProjectTypeModel projectTypeModel) {
//        Utils.getInstance().showLog("TIMESTAMP PROJECT LIST INITIATED", String.valueOf(System.currentTimeMillis()));
//
//        showDialog(DIALOG_LOADING);
//        ProjectList projectList = null;
//        Utils.getInstance().showLog("BEFORE ASYNCTASK", String.valueOf(System.currentTimeMillis()));
//        projectList = mDBHelper.getProjectsForUser(mUserId, projectTypeModel.mAppId);
//        Utils.getInstance().showLog("AFTER ASYNCTASK", String.valueOf(System.currentTimeMillis()));
//        removeDialog(DIALOG_LOADING);
//        if (projectList != null) {
//            if (projectList.mUserTypes.size() > 1) {
//                // Has more than one user type (eg : Primary, Secondary, etc)
//                Intent intent = new Intent(HomeActivity.this, ProjectSeparatorActivity.class);
//                intent.putExtra("appId", projectTypeModel.mAppId);
//                intent.putExtra("userId", mUserId);
//                startActivity(intent);
//            } else if (projectList.mUserTypes.size() == 1) {
//                // Has only one user type, can show the list directly
//                Intent intent = new Intent(HomeActivity.this, ProjectListActivity.class);
//                intent.putExtra("userType", projectList.mUserTypes.get(0));
//                intent.putExtra("showMap", projectList.mShouldShowMap);
//                intent.putExtra("appId", projectTypeModel.mAppId);
//                intent.putExtra("userId", mUserId);
//                startActivity(intent);
//            } else {
//                // No user type, which means, no projects assigned
//                Intent intent = new Intent(HomeActivity.this, ProjectListActivity.class);
//                intent.putExtra("userType", "noprojecttype");
//                intent.putExtra("showMap", projectList.mShouldShowMap);
//                intent.putExtra("appId", projectTypeModel.mAppId);
//                intent.putExtra("userId", mUserId);
//                startActivity(intent);
//            }
//        } else {
//            showErrorMessageAndFinishActivity(Constants.INCONSISTENT_DATA, false);
//        }
//    }
//
//    private void initiateManualSync() {
//        AppBackgroundSync appBackgroundSync = new AppBackgroundSync();
//        appBackgroundSync.execute();
//    }
//
//    public void startSyncAnimation() {
//        if (mActionBarRefreshIcon == null)
//            return;
//        Animation rotation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_refresh);
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
//    /** This function is called to create the options menu */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.home_menu, menu);
//
//        MenuItem item = menu.findItem(R.id.home_sync);
//        MenuItemCompat.setActionView(item, R.layout.home_sync_action_button);
//        View view = MenuItemCompat.getActionView(item);
//        mActionBarRefreshIcon = (ImageView) view.findViewById(R.id.refresh_image_to_rotate);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                initiateManualSync();
//            }
//        });
//
//        if (AppBackgroundSync.isSyncInProgress)
//            if (mActionBarRefreshIcon != null)
//                startSyncAnimation();
//        else
//            if (mActionBarRefreshIcon != null)
//                stopSyncAnimation();
//
//        return true;
//    }
//
//    /** This function is called when any item is clicked on the options menu */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_sign_out:
//                if (Utils.getInstance().isOnline(null)) {
//                    callLogoutService();
//                } else {
//                    invalidateLogin();
//                    moveToLoginScreen();
//                }
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        unregisterRecievers();
//        this.unregisterReceiver(mPostSyncUpdateReceiver);
//        this.unregisterReceiver(mPreSyncUpdateReceiver);
//        this.unregisterReceiver(mFailedSyncUpdateReceiver);
//    }
//
//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//
//        registerReceivers();
//        this.registerReceiver(mPostSyncUpdateReceiver,
//                new IntentFilter(Constants.POST_SYNC_BROADCAST_ACTION));
//        this.registerReceiver(mPreSyncUpdateReceiver,
//                new IntentFilter(Constants.PRE_SYNC_BROADCAST_ACTION));
//        this.registerReceiver(mFailedSyncUpdateReceiver,
//                new IntentFilter(Constants.FAILED_SYNC_BROADCAST));
//
//        if (AppBackgroundSync.isSyncInProgress)
//            if (mActionBarRefreshIcon != null)
//                startSyncAnimation();
//        else
//            if (mActionBarRefreshIcon != null)
//                stopSyncAnimation();
//    }
//
//    private BroadcastReceiver mPostSyncUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Post Sync Update Receiver -- Home -- called");
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
