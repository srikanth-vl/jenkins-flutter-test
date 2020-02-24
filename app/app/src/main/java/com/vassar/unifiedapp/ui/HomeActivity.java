package com.vassar.unifiedapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.OnStartupAction;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.receiver.MediaRequestReceiver;
import com.vassar.unifiedapp.synchronization.AlarmHelper;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private final int REQUEST_GPS_ACCESS = 925;
    public boolean mIsActionBarAnimationRunning = false;
    private ImageView mActionBarRefreshIcon;
    private BroadcastReceiver mPostSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Post Sync Update Receiver -- Home -- called");
            if (mIsActionBarAnimationRunning && !MediaRequestReceiver.isMediaThreadRunning) {
                stopSyncAnimation();
            }

            // Update the values on the list
            RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
            updateGridFragment(getRootProjectTypes(rootConfig));
            invalidateOptionsMenu();
        }
    };
    private BroadcastReceiver mPreSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Pre Sync Update Receiver -- Home -- called");
            if (!mIsActionBarAnimationRunning) {
                startSyncAnimation();
            }
        }
    };
    private BroadcastReceiver mFailedSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Post Sync Update Receiver -- " +
                    "Home -- called" + message);
        }
    };
    private BroadcastReceiver mLogoutUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startLogoutService();
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Utils.logInfo(LogTags.HOME_SCREEN, "Saving instance state -- Home");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initComponents(this);
        initializeToolbar();

        if (savedInstanceState == null) {
            // This screen is being loaded for the first time

            // Job Scheduler for AppBackgroundSync thread and Media thread
            AlarmHelper.beginAllTextDataRequest(UAAppContext.getInstance().getContext());
            AlarmHelper.scheduleAlarm(UAAppContext.getInstance().getContext());
            AlarmHelper.beginMediaRequest(UAAppContext.getInstance().getContext());

            RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();

            if (rootConfig == null) {
                // Error - no root config
                Utils.logError(LogTags.ROOT_CONFIG, "No Root Config for user after AppStartupThread2" +
                        "-- redirecting user to login screen");
                invalidateLogin();
                moveToLoginScreen();
                return;
            }

            if (rootConfig == null || rootConfig.mApplications == null) {
                // Error - no root config applications
                Utils.logError(LogTags.ROOT_CONFIG, "Application array in RootConfig is null -- error case" +
                        "-- redirecting user to login screen");
                invalidateLogin();
                moveToLoginScreen();
                return;
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            }

            // Loading a list fragment with the root level applications
            loadGridFragment(getRootProjectTypes(rootConfig));
        }

        initializeLocation();
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private void loadGridFragment(List<ProjectTypeModel> rootProjectTypes) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GridFragment fragment = new GridFragment(rootProjectTypes);
        fragmentManager.beginTransaction().add(R.id.home_root_view, fragment).commit();
    }

    private void updateGridFragment(List<ProjectTypeModel> rootProjectTypes) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GridFragment fragment = new GridFragment(rootProjectTypes);
        fragmentManager.beginTransaction().replace(R.id.home_root_view, fragment).commit();
    }

    /**
     * This function is called to add a fragment with subapps for an
     * application to the HomeActivity
     */
    public void addNewGridFragment(ArrayList<ProjectTypeModel> projectTypes) {
        GridFragment gridFragment = new GridFragment(projectTypes);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_root_view, gridFragment)
                .addToBackStack(null).commit();
    }

    /**
     * This function is called when an application leads to a project list
     */
    public void addNewProjectListActivity(ProjectTypeModel projectTypeModel) {
        // Check if the projectTypeModel has a grouping attribute
        // If it does, launch the ProjectGroupingActivity
        // If not, continue as before
        if (projectTypeModel != null && projectTypeModel.mGroupingAttributes != null && !projectTypeModel.mGroupingAttributes.isEmpty()) {
            Intent intent = new Intent(this, ProjectGroupingActivity.class);
            intent.putExtra(Constants.GROUPING_INTENT_APP_ID, projectTypeModel.mAppId);
            intent.putStringArrayListExtra(Constants.GROUPING_INTENT_GROUPING_PARAMETER, (ArrayList<String>) projectTypeModel.mGroupingAttributes);
            intent.putExtra(Constants.PROJECT_TYPE_NAME, projectTypeModel.mName);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ProjectListActivity.class);
            intent.putExtra(Constants.PROJECT_LIST_INTENT_APP_ID, projectTypeModel.mAppId);
            intent.putExtra(Constants.PROJECT_TYPE_NAME, projectTypeModel.mName);
            startActivity(intent);
        }
    }

    public void startSyncAnimation() {
        if (mActionBarRefreshIcon == null) {
            return;
        }
        Animation rotation = AnimationUtils.loadAnimation(UAAppContext.getInstance().getContext(), R.anim.rotate_refresh);
        rotation.setRepeatCount(Animation.INFINITE);
        mActionBarRefreshIcon.startAnimation(rotation);
        mIsActionBarAnimationRunning = true;
    }

    public void stopSyncAnimation() {
        if (mActionBarRefreshIcon == null) {
            return;
        }
        mActionBarRefreshIcon.clearAnimation();
        mIsActionBarAnimationRunning = false;
    }

    /**
     * This function is called to create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();

        boolean showEditableProfile = PropertyReader.getBooleanProperty(Constants.MENU_PROFILE_ICON);
        MenuItem editableProfileItem = menu.findItem(R.id.user_info);
        if (showEditableProfile) {
            MenuItemCompat.setActionView(editableProfileItem, R.layout.user_profile_button);
            View profileView = MenuItemCompat.getActionView(editableProfileItem);
            ImageView profileImage = profileView.findViewById(R.id.profile_image);
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callOnStartUp();
                }
            });
        } else {
            editableProfileItem.setVisible(false);
        }

        MenuItem profileItem = menu.findItem(R.id.action_profile);
        if (appMetaData == null || appMetaData.mProfileConfig == null || appMetaData.mProfileConfig.isEmpty()) {
            profileItem.setVisible(false);
        }

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        if (appMetaData == null || appMetaData.mSettingsPageEnabled == null || !appMetaData.mSettingsPageEnabled) {
            settingsItem.setVisible(false);
        }

        MenuItem mapsItem = menu.findItem(R.id.action_download_map);
        if (appMetaData == null || appMetaData.mMapEnabled == null || !appMetaData.mMapEnabled) {
            mapsItem.setVisible(false);
        }

        MenuItem item = menu.findItem(R.id.home_sync);
        MenuItemCompat.setActionView(item, R.layout.home_sync_action_button);
        View view = MenuItemCompat.getActionView(item);
        mActionBarRefreshIcon = view.findViewById(R.id.refresh_image_to_rotate);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.getInstance().isOnline(null)) {
                    initiateManualSync();
                } else {
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CHECK_INTERNET_CONNECTION), false);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
                if (rootConfig != null) {
                    showUnsyncedCountWindow(view);
                }
                return true;
            }
        });

        if (AppBackgroundSync.isSyncInProgress || MediaRequestReceiver.isMediaThreadRunning) {
            if (mActionBarRefreshIcon != null) {
                startSyncAnimation();
            }
        } else {
            if (mActionBarRefreshIcon != null) {
                stopSyncAnimation();
            }
        }
        return true;
    }

    /**
     * This function is called when any item is clicked on the options menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                startLogoutService();
                return true;
            case R.id.action_download_map:
                if (UAAppContext.getInstance().getMapConfig() != null) {
                    downloadMapData();
                } else {
                    Toast.makeText(this, "Maps are not required for this application!", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;

            case R.id.action_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadMapData() {
        Intent intent = new Intent(this, MapDownloadActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mPostSyncUpdateReceiver);
        this.unregisterReceiver(mPreSyncUpdateReceiver);
        this.unregisterReceiver(mFailedSyncUpdateReceiver);
        this.unregisterReceiver(mLogoutUpdateReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.registerReceiver(mPostSyncUpdateReceiver,
                new IntentFilter(Constants.POST_SYNC_BROADCAST_ACTION));
        this.registerReceiver(mPreSyncUpdateReceiver,
                new IntentFilter(Constants.PRE_SYNC_BROADCAST_ACTION));
        this.registerReceiver(mFailedSyncUpdateReceiver,
                new IntentFilter(Constants.FAILED_SYNC_BROADCAST));

        this.registerReceiver(mLogoutUpdateReceiver,
                new IntentFilter(Constants.LOGOUT_UPDATE_BROADCAST));

        if (AppBackgroundSync.isSyncInProgress || MediaRequestReceiver.isMediaThreadRunning) {
            if (mActionBarRefreshIcon != null) {
                startSyncAnimation();
            }
        } else {
            if (mActionBarRefreshIcon != null) {
                stopSyncAnimation();
            }
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();
        updateGridFragment(getRootProjectTypes(rootConfig));
        invalidateOptionsMenu();
    }

    private void initializeLocation() {
        // If the SDK is >= Marshmello, runtime permissions required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.PERMISSION_FINE_LOCATION);
            } else {
                createLocationRequest();
            }
        } else {
            createLocationRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions
            , int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    createLocationRequest();
                } else {
                    // permission denied!
                }
                break;
            }
        }
    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here
                ((UnifiedAppApplication) getApplication()).mFusedLocationClient = LocationServices
                        .getFusedLocationProviderClient(HomeActivity.this);
                ((UnifiedAppApplication) getApplication()).initializeLocation();
                ((UnifiedAppApplication) getApplication()).startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(HomeActivity.this,
                                REQUEST_GPS_ACCESS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GPS_ACCESS:
                if (resultCode == Activity.RESULT_OK) {
                    ((UnifiedAppApplication) getApplication()).mFusedLocationClient = LocationServices
                            .getFusedLocationProviderClient(HomeActivity.this);
                    ((UnifiedAppApplication) getApplication()).initializeLocation();
                    ((UnifiedAppApplication) getApplication()).startLocationUpdates();
                }
            default:
                break;
        }
    }

    public void callOnStartUp() {

        if (UAAppContext.getInstance().getAppMDConfig().onStartUp != null &&
                UAAppContext.getInstance().getAppMDConfig().onStartUp.size() > 0) {

            List<OnStartupAction> startUpData = UAAppContext.getInstance()
                    .getAppMDConfig().onStartUp;

            for (OnStartupAction onStartupAction : startUpData) {
                String type = onStartupAction.getType();
                switch (type) {
                    case Constants.ON_STARTUP_ACTIVITY:
                        Intent i = new Intent(this, DynamicFormActivity.class);
                        i.putExtra(Constants.FORM_SHOULD_SHOW_TOOLBAR, false);
                        startActivity(i);
                        break;

                    default:
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        MenuItem syn_menu_item = menu.findItem(R.id.home_sync);
        View view = MenuItemCompat.getActionView(syn_menu_item);
        TextView unsync_count = view.findViewById(R.id.refresh_image_unsync_count);

        int unsyncedProjectCount = getUnsyncedProjectAndMediaCount();
        if (unsyncedProjectCount > 0) {
            unsync_count.setText(String.valueOf(unsyncedProjectCount));
        }
        return true;
    }
}
