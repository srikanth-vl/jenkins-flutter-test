package com.vassar.unifiedapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.peritus.peritusofflinemap.MapInterface;
import com.peritus.peritusofflinemap.OfflineMap;
import com.vassar.unifiedapp.BuildConfig;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.MapConfiguration;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.model.MapInfo;
import com.vassar.unifiedapp.model.OfflineMapFile;
import com.vassar.unifiedapp.model.OnStartupAction;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectIconInfo;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.offlinemaps.CustomMarkerInfoWindow;
import com.vassar.unifiedapp.receiver.MediaRequestReceiver;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.MapUtils;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.Utils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ProjectListActivity extends BaseActivity {

    private static ProjectList mProjectList;
    private static boolean isMapViewOpened = false;
    private static boolean isLegendOpen = false;
    private final int REQUEST_GPS_ACCESS = 921;
    public String mSortType = Constants.PROJECT_LIST_ALPHABETICAL_SORTING;
    public List<String> mFilteringAttributes = null;
    private String mAppId;
    private ProjectTypeModel mProjectTypeModel;
    private long mListCreationTimestamp;
    private ImageView mActionBarRefreshIcon;
    private ImageView mProjectListViewIcon;
    private boolean mIsActionBarAnimationRunning = false;
    private List<Project> mFilteredProjects = new ArrayList<>();
    private boolean filterApplied = false;
    private MapInterface mMapInterface;
    private MapView mMapView;
    private LinearLayout mLegendLayout;
    private boolean showMap;
    private List<OfflineMapFile> mapLayers = new ArrayList<>();
    private List<MapInfo> mapMarkers = new ArrayList<>();

    private ArrayList<String> invisibleMarkers = new ArrayList<>();
    private ArrayList<String> invisibleLayers = new ArrayList<>();
    private Map<String, String> selectedFilterValue = new HashMap<>();
    private RelativeLayout mRootView;

    private String mGroupingAttribute;
    private String mGroupingAttributeValue;

    private FloatingActionButton mFab;
    private FloatingActionButton mCreateNewProjectButton;

    private String mProjectTypeName;

    private boolean openMapView = false;

    private ProgressBar mProgressBar;
    private Boolean createProjectEnable;
    private BroadcastReceiver mLogoutUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startLogoutService();
        }
    };
    private BroadcastReceiver mPostSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Post Sync Update Receiver -- ProjectList -- called");
            if (mIsActionBarAnimationRunning && !MediaRequestReceiver.isMediaThreadRunning) {
                stopSyncAnimation();
            }
            invalidateOptionsMenu();
        }
    };
    private BroadcastReceiver mPreSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Pre Sync Update Receiver -- ProjectList -- called");
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
                    "ProjectList -- called" + message);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        mMapView = findViewById(R.id.project_list_map_view);
        mRootView = findViewById(R.id.project_list_root_view);
        mFab = findViewById(R.id.project_list_fab);
        mLegendLayout = findViewById(R.id.legend_scroll_view_layout);
        mProgressBar = findViewById(R.id.project_list_progress_bar);
        mMapInterface = new OfflineMap(this, this);

        isMapViewOpened = false;
        mLegendLayout.setVisibility(View.GONE);

        initComponents(this);
        initializeToolbar();
        mCreateNewProjectButton = findViewById(R.id.create_new_project_button);

        if (savedInstanceState == null) {
            // The project list is being loaded for the first time
            mListCreationTimestamp = System.currentTimeMillis();
            mAppId = getIntent().getStringExtra(Constants.PROJECT_LIST_INTENT_APP_ID);
            mProjectTypeName = getIntent().getStringExtra(Constants.PROJECT_TYPE_NAME);

            if (getIntent().hasExtra(Constants.PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE)) {
                mGroupingAttribute = getIntent().getStringExtra(Constants.PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE);
                mGroupingAttributeValue = getIntent().getStringExtra(Constants.PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE_VALUE);
            }
            UAAppContext.getInstance().clearProjectListCaches();
        } else {
            // The project list page is being restored
            mListCreationTimestamp = savedInstanceState.getLong(Constants.PROJECT_LIST_CREATION_TIMESTAMP);
            mAppId = savedInstanceState.getString(Constants.PROJECT_LIST_APP_ID);
            mProjectTypeName = getIntent().getStringExtra(Constants.PROJECT_TYPE_NAME);
            mGroupingAttribute = savedInstanceState.getString(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE);
            mGroupingAttributeValue = savedInstanceState.getString(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE);
            mProjectList = UAAppContext.getInstance().getProjectList();
            mFilteredProjects.clear();
            mFilteredProjects.addAll(UAAppContext.getInstance().getFilteredProjectListCache());
            mProjectTypeName = savedInstanceState.getString(Constants.PROJECT_TYPE_NAME);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mProjectTypeName);
        }
        mProjectTypeModel = UAAppContext.getInstance().getProjectTypeModel(mAppId);
        initializeComponents();
        loadFragment();

        createProjectEnable = haveProjectCreationAccess();
        if (createProjectEnable) {
            initializeCreateNewProjectButtonListener();
        } else {
            mCreateNewProjectButton.hide();
        }
    }

    private void causeDelayAndLoadMap() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Map Layers");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.show();

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadMapView();
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
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
                        .getFusedLocationProviderClient(ProjectListActivity.this);
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
                        resolvable.startResolutionForResult(ProjectListActivity.this,
                                REQUEST_GPS_ACCESS);

                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }


                }
            }
        });
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
                    Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();

                }
                break;
            }
        }
    }


    private void initializeFloatingButtonListener() {
        if (mFab != null && mLegendLayout != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFab.getVisibility() == View.VISIBLE) {
                        if (isLegendOpen) {
                            // Legend is open, collapse
                            mLegendLayout.setVisibility(View.GONE);
                            isLegendOpen = false;
                        } else {
                            // Open legend
                            mLegendLayout.setVisibility(View.VISIBLE);
                            isLegendOpen = true;
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.PROJECT_LIST_APP_ID, mAppId);
        outState.putString(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE, mGroupingAttribute);
        outState.putString(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE, mGroupingAttributeValue);
        outState.putLong(Constants.PROJECT_LIST_CREATION_TIMESTAMP, mListCreationTimestamp);
        outState.putString(Constants.PROJECT_TYPE_NAME, mProjectTypeName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isFilteringPresent = false;
        switch (requestCode) {
            case Constants.FILTER:
                if (resultCode == Activity.RESULT_OK) {
                    // Refresh the list with the given parameters
                    List<String> projectIds = new ArrayList<>();
                    isFilteringPresent = true;
                    Bundle extras = data.getExtras();
                    List<String> valuesList = new ArrayList<>();
                    if (mFilteringAttributes != null && !mFilteringAttributes.isEmpty()) {
                        for (String attribute : mFilteringAttributes) {
                            if (extras != null && extras.getString(attribute) != null && !extras.getString(attribute).isEmpty()) {
                                String val = extras.getString(attribute);
                                String[] valArr = val.split("\\$\\$");
                                if (valArr.length == 2) {
                                    int index = Integer.parseInt(val.split("\\$\\$")[1]);
                                    String valueofAttribute = val.split("\\$\\$")[0];
                                    valuesList.add(index, val.split("\\$\\$")[0]);
                                    selectedFilterValue.put(attribute, valueofAttribute);
                                }
                            }
                        }

                        String dimensionValues = TextUtils.join("#", valuesList);
                        if (dimensionValues != null && !dimensionValues.isEmpty()) {
                            projectIds = mDBHelper.getProjectIdsForFilterQuery(UAAppContext.getInstance().getUserID(), mAppId, dimensionValues.toLowerCase());
                        }
                    }
                    if (projectIds == null || projectIds.isEmpty()) {
                        mFilteredProjects = new ArrayList<>();
                        Toast.makeText(this, getResources().getString(R.string.NO_PROJECTS_FOUND), Toast.LENGTH_LONG).show();
                    } else {
                        mFilteredProjects.clear();
                        mFilteredProjects.addAll(mProjectList.mProjects);
                        ListIterator<Project> listIterator = mFilteredProjects.listIterator();
                        while (listIterator.hasNext()) {
                            if (!projectIds.contains(listIterator.next().mProjectId)) {
                                listIterator.remove();
                            }
                        }
                    }

                    UAAppContext.getInstance().getFilteredProjectListCache().clear();
                    UAAppContext.getInstance().getFilteredProjectListCache().addAll(mFilteredProjects);

                    ProjectListFragment projectListFragment;
                    if (mFilteredProjects.size() == 0) {
                        // No result for filtering
                        projectListFragment = new ProjectListFragment(mProjectList.mProjects
                                , null, mListCreationTimestamp, mSortType, mAppId, isFilteringPresent);
                    } else {
                        filterApplied = true;
                        projectListFragment = new ProjectListFragment(mProjectList.mProjects
                                , mFilteredProjects, mListCreationTimestamp, mSortType, mAppId, isFilteringPresent);
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.project_list_root_view
                            , projectListFragment).commitAllowingStateLoss();

                    Utils.getInstance().showLog("FILTERING", "RECEIVED");
                } else {
                    // Filtering cancelled
                    Toast.makeText(this, getResources().getString(R.string.NO_FILTERING_PARAMETERS), Toast.LENGTH_LONG).show();
                }
                break;

            case REQUEST_GPS_ACCESS:
                if (resultCode == Activity.RESULT_OK) {
                    ((UnifiedAppApplication) getApplication()).mFusedLocationClient = LocationServices
                            .getFusedLocationProviderClient(ProjectListActivity.this);
                    ((UnifiedAppApplication) getApplication()).initializeLocation();
                    ((UnifiedAppApplication) getApplication()).startLocationUpdates();
                } else {
                    Toast.makeText(mContext, "Please grant the permission to load maps.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.project_list_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    /**
     * Initializes variables, loads the ProjectListConfig
     */
    private void initializeComponents() {

        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();

        if (rootConfig == null || rootConfig.mApplications == null) {
            // Error - Root Config inconsistencies
            Utils.logError(LogTags.PROJECT_LIST, "No Root Config for user after AppStartupThread2" +
                    "-- redirecting user to login screen");
            invalidateLogin();
            moveToLoginScreen();
        }

        for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
            if (projectTypeModel.mAppId.equals(mAppId)) {
                if (projectTypeModel.mSortType != null && !projectTypeModel.mSortType.isEmpty()) {
                    mSortType = projectTypeModel.mSortType;
                }
                if (projectTypeModel.mFilteringAttributes != null && projectTypeModel.mFilteringAttributes.size() > 0) {
                    mFilteringAttributes = projectTypeModel.mFilteringAttributes;
                }
                // Checking if Map Configuration is null or not
                if (UAAppContext.getInstance().getMapConfig() != null) {
                    Map<String, List<MapInfo>> appSpecificMarkers = UAAppContext.getInstance().getMapConfig().mapMarkers;
                    if (appSpecificMarkers != null && !appSpecificMarkers.isEmpty()) {
                        mapMarkers = appSpecificMarkers.get(mAppId);
                    }
                    if (UAAppContext.getInstance().getMapConfig().files != null) {
                        mapLayers = MapUtils.getInstance().getLayerFiles();
                    }
                }
                break;
            }
        }

        if (mGroupingAttribute != null && !mGroupingAttribute.isEmpty()) {
            // Only show the projects that have this grouping attribute
            UAAppContext.getInstance().setProjectList(UAAppContext.getInstance().getDBHelper().getProjectsForUserForGroupingAttribute(UAAppContext
                    .getInstance().getUserID(), mAppId, mGroupingAttributeValue.toLowerCase()));
        } else {
            UAAppContext.getInstance().setProjectList(UAAppContext.getInstance().getDBHelper().getProjectsForUser(UAAppContext
                    .getInstance().getUserID(), mAppId));
        }
        mProjectList = UAAppContext.getInstance().getProjectList();
    }

    private void loadFragment() {
        ProjectListFragment fragment = new ProjectListFragment(mProjectList.mProjects,
                mFilteredProjects, mListCreationTimestamp, mSortType, mAppId, false);
        getSupportFragmentManager().beginTransaction().add(R.id.project_list_root_view,
                fragment).commit();
    }


    private void loadMapView() {

        if (mProjectList != null && mProjectList.mProjects != null && !mProjectList.mProjects.isEmpty()) {
            double sumLat = 0.0;
            double sumLon = 0.0;
            int length = 0;
            for (Project project : mProjectList.mProjects) {
                if (project != null
                        && project.mLatitude != null
                        && project.mLongitude != null && !project.mLongitude.isEmpty() && !project.mLatitude.isEmpty()) {
                    sumLat += Double.valueOf(project.mLatitude);
                    sumLon += Double.valueOf(project.mLongitude);
                    length++;
                }
            }

            Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
            // Calculating the center of all the project geotag, for loading map
//            int length = mProjectList.mProjects.size();
            double mCenterLat = sumLat / length;
            double mCenterLon = sumLon / length;

            GeoPoint geoPoint = new GeoPoint(mCenterLat, mCenterLon);
            float zoomLevel = 14.0F;

            // Loading maps from zip files
            mMapView.setUseDataConnection(false);
            MapConfigurationV1 mapConfig = UAAppContext.getInstance().getMapConfig();
            String mapSourceName = mapConfig.mapSourceName;

            int minZoom = UAAppContext.getInstance().getMapConfig().getMinZoom();
            int maxZoom = UAAppContext.getInstance().getMapConfig().getMaxZoom();
            if (mapSourceName != null && !mapSourceName.isEmpty()) {
                mMapView.setTileSource(new XYTileSource(mapSourceName, minZoom, maxZoom, 256, ".png", new String[]{""}));
                mMapView.setMultiTouchControls(true);
                IMapController mapController = mMapView.getController();
                mapController.setZoom(zoomLevel);
                mapController.setCenter(geoPoint);
                Utils.logInfo("BOUNDING BOX ", mapConfig.boundingBox);
                MapUtils.getInstance().setScrollableAreaLimit(mMapView, mapConfig.boundingBox);

                renderNewOverlays();

                createNewMarkers(mProjectList.mProjects);

                loadLegend();

                addNewLayerTogglesToLegend();

                initializeFloatingButtonListener();
            } else {
                // Error - Project geolocations inconsistent
                Toast.makeText(this, "No valid map source. Please sync and try again later.", Toast.LENGTH_LONG).show();
                Utils.logError(LogTags.MAP_CONFIG, "No valid map source" +
                        "-- continuing without loading map view");
            }

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            // Error - Project geolocations inconsistent
            Utils.logError(LogTags.PROJECT_LIST, "Project geolocations inconsistent" +
                    "-- continuing without loading map view");
        }
    }

    /**
     * This function is called when a project is clicked
     */
    public void moveToProjectFormActivity(Project project) {
        if (PropertyReader.getProperty("SUPER_APP_ID")
                .equalsIgnoreCase("4ca24624-3841-3d1f-b499-44903cbe829c") || PropertyReader.getProperty("SUPER_APP_ID")
                .equalsIgnoreCase("9e49254b-d85e-11e9-beb7-b5d18d261495") || PropertyReader.getProperty("SUPER_APP_ID")
                .equalsIgnoreCase("6b31aba3-eb49-4fa4-8f3c-3f764b8d17e5")) {
            // APAIMS
            Intent intent = new Intent(this, TabularFormActivity.class);
            intent.putExtra(Constants.PROJECT_FORM_INTENT_APP_ID, mAppId);
            intent.putExtra(Constants.PROJECT_FORM_INTENT_PROJECT_ID, project.mProjectId);
            intent.putExtra(Constants.PROJECT_FORM_INTENT_PROJECT_NAME, project.mProjectName);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ProjectFormActivity.class);
            intent.putExtra(Constants.PROJECT_FORM_INTENT_APP_ID, mAppId);
            intent.putExtra(Constants.PROJECT_FORM_INTENT_PROJECT_ID, project.mProjectId);
            intent.putExtra(Constants.PROJECT_FORM_INTENT_PROJECT_NAME, project.mProjectName);
            if (project.mState.equalsIgnoreCase("New")) {
                intent.putExtra(Constants.FORM_ACTION_TYPE, Constants.INSERT_FORM_KEY);
            } else {
                intent.putExtra(Constants.FORM_ACTION_TYPE, Constants.UPDATE_FORM_KEY);
            }
            intent.putExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE, mGroupingAttribute);
            intent.putExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE, mGroupingAttributeValue);

            startActivity(intent);
        }
    }

    private void initializeCreateNewProjectButtonListener() {
        if (mCreateNewProjectButton != null) {
            mCreateNewProjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), ProjectFormActivity.class);
                    intent.putExtra(Constants.PROJECT_FORM_INTENT_APP_ID, mAppId);
                    intent.putExtra(Constants.FORM_ACTION_TYPE, Constants.INSERT_FORM_KEY);
                    intent.putExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE, mGroupingAttribute);
                    intent.putExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE, mGroupingAttributeValue);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                startLogoutService();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This function is called to create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_list_menu, menu);

        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();

        boolean showProfile = PropertyReader.getBooleanProperty(Constants.MENU_PROFILE_ICON);
        MenuItem profileItem = menu.findItem(R.id.user_info);
        if (showProfile) {
            MenuItemCompat.setActionView(profileItem, R.layout.user_profile_button);
            View profileView = MenuItemCompat.getActionView(profileItem);
            ImageView profileImage = profileView.findViewById(R.id.profile_image);
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callOnStartUp();
                }
            });
        } else {
            profileItem.setVisible(false);
        }

        MenuItem filterItem = menu.findItem(R.id.project_list_filter);
        if (appMetaData == null || appMetaData.mFilterEnabled == null || !appMetaData.mFilterEnabled){
            filterItem.setVisible(false);
        } else{
            MenuItemCompat.setActionView(filterItem, R.layout.project_list_filter_button);
            View filterView = MenuItemCompat.getActionView(filterItem);

            filterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFilteringAttributes != null && mFilteringAttributes.size() > 0) {
                        startFilterForResult();
                    } else {
                        showErrorMessageAndFinishActivity(getResources().getString(R.string.filter_not_available), false);
                    }
                }
            });
        }

        MenuItem mapsItem = menu.findItem(R.id.project_list_map_toggle);
        if (appMetaData == null || appMetaData.mMapEnabled == null || !appMetaData.mMapEnabled) {
            mapsItem.setVisible(false);
        } else {
            mapsItem.setVisible(true);
            MenuItemCompat.setActionView(mapsItem, R.layout.map_view_action_bar_icon_layout);
            View mapView = MenuItemCompat.getActionView(mapsItem);
            mProjectListViewIcon = mapView.findViewById(R.id.map_view_icon);

            mapView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View v) {


                    // TODO : Check if any of the files that the user needs are missing. If they
                    //  are, then ask the user is he wants to download these files???????

                    // Toggle between mapview and list

                    // TODO : Check for filtered projects

                    if (mProjectList != null && mProjectList.mProjects != null && mProjectList.mProjects.size() > 0) {
                        if (isMapViewOpened) {
                            // Close map view
                            mMapView.setVisibility(View.GONE);
                            mFab.hide();
                            mLegendLayout.setVisibility(View.GONE);
                            mRootView.setVisibility(View.VISIBLE);
                            isMapViewOpened = false;
                            mProjectListViewIcon.setImageResource(R.drawable.map_view_action_bar_icon);
                            if (appMetaData != null && appMetaData.mFilterEnabled != null && appMetaData.mFilterEnabled) {
                                filterItem.setVisible(true);
                            }
                            if (createProjectEnable) {
                                mCreateNewProjectButton.show();
                            }
                        } else {
                            if (!openMapView) {
                                causeDelayAndLoadMap();
                                openMapView = true;
                            }
                            // Open map view with the project list
                            mMapView.setVisibility(View.VISIBLE);
                            mFab.show();
                            if (isLegendOpen) {
                                mLegendLayout.setVisibility(View.VISIBLE);
                            }
                            mRootView.setVisibility(View.GONE);
                            mProjectListViewIcon.setImageResource(R.drawable.list_action_bar_icon);
                            isMapViewOpened = true;
                            filterItem.setVisible(false);
                            mCreateNewProjectButton.hide();
                        }
                    }
                }
            });
        }

        MenuItem item = menu.findItem(R.id.project_list_sync);
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

    private void startFilterForResult() {
        Intent filter = new Intent(this, FilterScreen.class);
        filter.putExtra(Constants.FILTER_APP_ID, mAppId);
        filter.putExtra(Constants.FILTER_USER_ID, UAAppContext.getInstance().getUserID());
        filter.putExtra(Constants.PROJECT_GROUPING_ATTRIBUTE, mGroupingAttributeValue);

        if (selectedFilterValue != null && !selectedFilterValue.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String keyToValueMapString = mapper.writeValueAsString(selectedFilterValue);
                filter.putExtra(Constants.SELECTED_FILTER_KEY_VALUE_MAP, keyToValueMapString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        startActivityForResult(filter, Constants.FILTER);
    }

    /**
     * This function is called every time the user tries to sync the locally saved
     * projects
     */
//    private void initiateManualSync() {
//        AppBackgroundSync appBackgroundSync = new AppBackgroundSync();
//        appBackgroundSync.execute();
//
//        MediaRequestHandlerTask mediaRequestHandlerTask = new MediaRequestHandlerTask();
//        mediaRequestHandlerTask.execute();
//    }
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.registerReceiver(mPostSyncUpdateReceiver, new IntentFilter(Constants.POST_SYNC_BROADCAST_ACTION));
        this.registerReceiver(mPreSyncUpdateReceiver, new IntentFilter(Constants.PRE_SYNC_BROADCAST_ACTION));
        this.registerReceiver(mFailedSyncUpdateReceiver, new IntentFilter(Constants.FAILED_SYNC_BROADCAST));
        this.registerReceiver(mLogoutUpdateReceiver, new IntentFilter(Constants.LOGOUT_UPDATE_BROADCAST));

        // Refresh the project list here
        if (AppBackgroundSync.isSyncInProgress || MediaRequestReceiver.isMediaThreadRunning) {
            if (mActionBarRefreshIcon != null) {
                startSyncAnimation();
            }
        } else {
            if (mActionBarRefreshIcon != null) {
                stopSyncAnimation();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterRecievers();
        this.unregisterReceiver(mPostSyncUpdateReceiver);
        this.unregisterReceiver(mPreSyncUpdateReceiver);
        this.unregisterReceiver(mFailedSyncUpdateReceiver);
        this.unregisterReceiver(mLogoutUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
        MenuItem syn_menu_item = menu.findItem(R.id.project_list_sync);
        View view = MenuItemCompat.getActionView(syn_menu_item);
        TextView unsync_count = view.findViewById(R.id.refresh_image_unsync_count);

        int unsyncedProjectCount = getUnsyncedProjectAndMediaCount();
        if (unsyncedProjectCount > 0) {
            unsync_count.setText(String.valueOf(unsyncedProjectCount));
        }
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
        if (appMetaData != null && appMetaData.mFilterEnabled != null && appMetaData.mFilterEnabled) {
            MenuItem filter_menu_item = menu.findItem(R.id.project_list_filter);
            View filter_view = MenuItemCompat.getActionView(filter_menu_item);
            ImageView filter_checked = filter_view.findViewById(R.id.filter_applied);

            if (isMapViewOpened) {
                filter_menu_item.setVisible(false);
                filter_checked.setVisibility(View.INVISIBLE);
                if (mProjectListViewIcon != null) {
                    mProjectListViewIcon.setImageResource(R.drawable.list_action_bar_icon);
                }

                mMapView.setVisibility(View.VISIBLE);
                mFab.show();
                if (isLegendOpen) {
                    mLegendLayout.setVisibility(View.VISIBLE);
                }
                mRootView.setVisibility(View.GONE);
                mProjectListViewIcon.setImageResource(R.drawable.list_action_bar_icon);
                mCreateNewProjectButton.hide();

            } else {
                filter_menu_item.setVisible(true);
                if (mProjectListViewIcon != null) {
                    mProjectListViewIcon.setImageResource(R.drawable.map_view_action_bar_icon);
                }
                if (filterApplied) {
                    filter_checked.setVisibility(View.VISIBLE);
                }
            }
        }
        return true;
    }

    // Render Overlays method
    public void renderNewOverlays() {

        File root = android.os.Environment.getExternalStorageDirectory();

        ArrayList<String> mapLayerFiles = new ArrayList<>();
        ArrayList<String> mapTypes = new ArrayList<>();
        ArrayList<String> layerIndices = new ArrayList<>();

        String storageDir = "";

        MapConfigurationV1 mapConfig = UAAppContext.getInstance().getMapConfig();
        if (mapConfig != null && mapConfig.getFiles() != null) {

            for (OfflineMapFile offlineMapFile : mapLayers) {
                String name = offlineMapFile.getFileName();
                String path = offlineMapFile.getFileStoragePath();
                mapLayerFiles.add(name);
                mapTypes.add(offlineMapFile.getFileAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL));
                layerIndices.add(offlineMapFile.getFileAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX));
                storageDir = root.getAbsolutePath() + path + "/";
                //TODO : Check if file is present in storage
            }

            for (OfflineMapFile mapFile : mapLayers) {
                if (!mapLayerFiles.contains(mapFile.fileName)) {
                    invisibleLayers.add(mapFile.fileName);
                }
            }

            mMapInterface.showGeoJsonObjectsCIP(mMapView, mapLayerFiles, mapTypes, this.getPackageName(), 2, storageDir, layerIndices);

        } else {
            showErrorMessageAndFinishActivity("Cannot load map overlays.", false);
            Utils.logError(LogTags.MAP_CONFIG, "Error getting MapConfig -- null -- continuing without loading overlays");
        }
    }

    // Create markers method
    private void createNewMarkers(List<Project> projects) {

        String defaultIcon = getDefaultMarkerIcon();
        ArrayList<String> markerIcons = new ArrayList<>();
        ArrayList<Marker> markers = new ArrayList<>();
        ArrayList<ArrayList<String>> locations = new ArrayList<>();
        ArrayList<String> markerIndices = new ArrayList<>();
        int i = 0;
        for (Project project : projects) {
            if (project != null && project.mLatitude != null && project.mLongitude != null
                    && !project.mLatitude.isEmpty() && !project.mLongitude.isEmpty()) {

                CustomMarkerInfoWindow cmiw = new CustomMarkerInfoWindow(mMapView, project, mAppId, this);

                Marker marker = new Marker(mMapView);
                marker.setInfoWindow(cmiw);
                markers.add(marker);

                ArrayList<String> locationObject = new ArrayList<>();
                locationObject.add(String.valueOf(i));
                ProjectIconInfo projectIconInfo = null;
                if (mProjectTypeModel != null && mProjectTypeModel.mProjectIconInfo != null) {
                    projectIconInfo = mProjectTypeModel.mProjectIconInfo;
                }
                String iconUrl = Utils.getInstance().getProjectIcon(project, mAppId, projectIconInfo);
                String iconPath = MapUtils.getInstance().getMapElementIcon(iconUrl);

                // Check for marker icon for that project
                if (iconPath != null && !iconPath.isEmpty()) {
                    String markerIndex = getMapElementIndexUsingUrl(iconUrl);
                    markerIcons.add(iconPath);
                    locationObject.add(iconPath);
                    markerIndices.add(markerIndex);
                    mMapInterface.setItemCIP(marker, mMapView, Double.valueOf(project.mLatitude), Double.valueOf(project.mLongitude), project.mProjectName,
                            iconPath, this.getPackageName(), 2, markerIndex);
                } else if (defaultIcon != null && !defaultIcon.isEmpty()) {
                    String defaultMarkerIndex = getMapElementIndexUsingUrl(defaultIcon);
                    markerIcons.add(defaultIcon);
                    locationObject.add(defaultIcon);
                    markerIndices.add(defaultMarkerIndex);
                    mMapInterface.setItemCIP(marker, mMapView, Double.valueOf(project.mLatitude), Double.valueOf(project.mLongitude), project.mProjectName,
                            defaultIcon, this.getPackageName(), 2, defaultMarkerIndex);
                } else {
                    locationObject.add("map_marker_icon");
                    markerIndices.add("15");
                }
                locationObject.add(project.mProjectName);
                locationObject.add(project.mLatitude);
                locationObject.add(project.mLongitude);
                locations.add(locationObject);
            }
            i++;
        }
        // if no marker icons are downloaded -> initiate the download service and set markers in map from drawable
        if (markerIcons.isEmpty()) {
            MapUtils.getInstance().downloadMapMarkers(mapMarkers);
            mMapInterface.setMultipleItemsCIP(mMapView, markers, locations, this.getPackageName(), 1, markerIndices);
        }
    }

    // Add toggle method
    private void addNewLayerTogglesToLegend() {

        List<LinearLayout> legendItemLayouts = new ArrayList<>();
        List<CheckBox> legendCheckboxes = new ArrayList<>();

        legendItemLayouts.addAll(mMapInterface.getLegendItemLayouts());
        legendCheckboxes.addAll(mMapInterface.getLegendItemCheckboxes());

        for (int i = 0; i < legendItemLayouts.size(); i++) {

            CheckBox checkBox = legendCheckboxes.get(i);
            LinearLayout linearLayout = legendItemLayouts.get(i);
            TextView legendText = (TextView) linearLayout.getChildAt(1);

            String index = getInfoFromEntityName(legendText.getText().toString());

            checkBox.setChecked(true);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (index != null && !index.isEmpty()) {
                        mMapInterface.hideOrShowLayer(mProgressBar, mMapView, Integer.parseInt(index), isChecked);
                        mMapView.invalidate();
                    }
                }
            });
        }
    }

    // Helper Methods

    // Get MapInfo from map entity name( for layers only)
    public String getInfoFromEntityName(String entityName) {

        for (OfflineMapFile mapLayer : mapLayers) {
            if (mapLayer.fileAdditionalInfo != null && !mapLayer.fileAdditionalInfo.isEmpty()) {
                if (mapLayer.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL) &&
                        mapLayer.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX)) {
                    if (mapLayer.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL).equalsIgnoreCase(entityName)) {
                        return mapLayer.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX);
                    }
                }
            }
        }

        for (MapInfo mapMarker : mapMarkers) {
            if (mapMarker.getAdditionalInfo() != null && !mapMarker.getAdditionalInfo().isEmpty() &&
                    mapMarker.getAdditionalInfo().containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX)) {
                String markerLabel = mapMarker.getAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL);
                if (markerLabel != null && markerLabel.equalsIgnoreCase(entityName)) {
                    return mapMarker.getAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX);
                }
            }
        }
        return "";
    }


    // Get storage path for default marker icon (if not found, download it)
    public String getDefaultMarkerIcon() {
        String defaultIcon = "";
        for (MapInfo mapMarker : mapMarkers) {
            if (mapMarker.getMapEntityName().equalsIgnoreCase(Constants.PROJECT_LIST_DEFAULT_MAP_ICON)) {
                defaultIcon = MapUtils.getInstance().getMapElementIcon(mapMarker.getIconUrl());
            }
        }
        return defaultIcon;

    }

    public String getMapElementIndexUsingUrl(String url) {

        String view_index = "";
        for (MapInfo mapInfo : mapMarkers) {
            String iconName1 = mapInfo.getIconUrl().substring(mapInfo.getIconUrl().lastIndexOf("/"));
            String iconName2 = url.substring(url.lastIndexOf("/"));
            if (iconName1.equalsIgnoreCase(iconName2)) {
                if (mapInfo.getAdditionalInfo().containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX)) {
                    view_index = mapInfo.getAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX);
                    return view_index;
                }
            }
        }
        for (OfflineMapFile mapInfo : mapLayers) {

            if (mapInfo.fileAdditionalInfo != null && !mapInfo.fileAdditionalInfo.isEmpty() &&
                    mapInfo.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL)) {
                String iconUrl = mapInfo.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL);
                String iconName1 = iconUrl.substring(iconUrl.lastIndexOf("/"));
                String iconName2 = url.substring(url.lastIndexOf("/"));

                if (iconName1.equalsIgnoreCase(iconName2)) {
                    view_index = mapInfo.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_INDEX);
                    return view_index;
                }
            }
        }
        return view_index;
    }

    // Legend methods
    private ArrayList<String> createLegendData() {

        ArrayList<String> icons = new ArrayList<>();
        ArrayList<String> mapLayerName = new ArrayList<>();

        String defaultIcon = getDefaultMarkerIcon();

        for (MapInfo mapMarker : mapMarkers) {
            String markerIconPath = MapUtils.getInstance().getMapElementIcon(mapMarker.getIconUrl());
            if (markerIconPath != null && !markerIconPath.isEmpty()) {
                icons.add(markerIconPath);
            } else if (defaultIcon != null && !defaultIcon.isEmpty()) {
                icons.add(defaultIcon);
            } else {
                invisibleMarkers.add(mapMarker.getMapEntityName());
            }
        }
        for (OfflineMapFile mapLayer : mapLayers) {
            if (!invisibleLayers.contains(mapLayer.fileName) && !mapLayerName.contains(mapLayer.fileName)) {
                String iconPath = "";
                if (mapLayer.fileAdditionalInfo != null && !mapLayer.fileAdditionalInfo.isEmpty() &&
                        mapLayer.fileAdditionalInfo.containsKey(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL)) {
                    iconPath = MapUtils.getInstance().getMapElementIcon(
                            mapLayer.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_ICON_URL));
                }
                if (iconPath != null && !iconPath.isEmpty()) {
                    icons.add(iconPath);
                } else if (defaultIcon != null && !defaultIcon.isEmpty()) {
                    icons.add(defaultIcon);
                } else if (icons.size() > 0) {
                    icons.add(icons.get(0));
                }
                mapLayerName.add(mapLayer.fileName);
            }
        }
        return icons;
    }

    private ArrayList<String> createLegendNamesData() {

        ArrayList<String> names = new ArrayList<>();
        for (MapInfo mapInfo : mapMarkers) {
            if (mapInfo.getAdditionalInfo() != null && !mapInfo.getAdditionalInfo().isEmpty()) {
                String markerLabel = mapInfo.getAdditionalInfo().get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL);
                if (!invisibleMarkers.contains(markerLabel) && !names.contains(markerLabel)) {
                    names.add(markerLabel);
                }
            }
        }
        for (OfflineMapFile mapInfo : mapLayers) {
            if (mapInfo.fileAdditionalInfo != null && !mapInfo.fileAdditionalInfo.isEmpty()) {
                String layerLabel = mapInfo.fileAdditionalInfo.get(Constants.PROJECT_LIST_MAP_ELEMENT_LABEL);
                if (!invisibleLayers.contains(mapInfo.fileName) && !names.contains(layerLabel)) {
                    names.add(layerLabel);
                }
            }
        }
        return names;
    }


    private void loadLegend() {
        if (mLegendLayout != null) {
            mLegendLayout.removeAllViews();
            ArrayList<String> legendData = createLegendData();
            ArrayList<String> legendNames = createLegendNamesData();

            // No marker icons are found in storage
            if (legendData.isEmpty()) {
                ArrayList<String> icons = new ArrayList<>();
                for (int i = 0; i < legendNames.size(); i++) {
                    icons.add("map_marker_icon");
                }
                mMapInterface.showLegendsItemsCIP(icons, legendNames, mLegendLayout, 1);
            } else {
                mMapInterface.showLegendsItemsCIP(legendData, legendNames, mLegendLayout, 2);
            }
            invisibleMarkers.clear();
        } else {
            // Error - No legend view initialized
            Utils.logError(LogTags.PROJECT_LIST, "No legend view initialized" +
                    "-- continuing without loading legend");
        }
    }

    private boolean haveProjectCreationAccess() {
        boolean access = false;
        ProjectTypeConfiguration projectTypeConfiguration = UAAppContext.getInstance().getDBHelper()
                .getProjectFormForApp(UAAppContext.getInstance().getUserID()
                        , mAppId);
        access = projectTypeConfiguration != null && projectTypeConfiguration.mContent != null &&
                projectTypeConfiguration.mContent.get(Constants.DEFAULT_PROJECT_ID) != null &&
                projectTypeConfiguration.mContent.get(Constants.DEFAULT_PROJECT_ID).get(Constants.INSERT_FORM_KEY) != null;
        return access;
    }
}
