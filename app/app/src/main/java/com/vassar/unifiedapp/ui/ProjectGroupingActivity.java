package com.vassar.unifiedapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.adapter.ProjectGroupingAdapter;
import com.vassar.unifiedapp.asynctask.MediaRequestHandlerTask;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.listener.RecyclerItemClickListener;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.OnStartupAction;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.newflow.AppBackgroundSync;
import com.vassar.unifiedapp.receiver.MediaRequestReceiver;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;
import com.futuremind.recyclerviewfastscroll.FastScroller;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static android.view.View.GONE;

public class ProjectGroupingActivity extends BaseActivity {

    RecyclerView mRootView;
    Spinner mGroupingSpinner;
    private String mAppId;
    private List<String> mGroupingAttributes = new ArrayList<>();
    private List<String> mProjectGroups;
    private String mGroupingAttribute;
    private ProgressBar loadMapProgressBar;
    private ImageView mActionBarRefreshIcon;
    private boolean mIsActionBarAnimationRunning = false;
    private FastScroller mProjectGroupingScroller;
    private String mProjectTypeName;

    private BroadcastReceiver mPostSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Post Sync Update Receiver -- ProjectGrouping -- called");
            if (mIsActionBarAnimationRunning && !MediaRequestReceiver.isMediaThreadRunning) {
                stopSyncAnimation();
            }
            invalidateOptionsMenu();
        }
    };

    private BroadcastReceiver mPreSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Pre Sync Update Receiver -- ProjectGrouping -- called");
            if (!mIsActionBarAnimationRunning) {
                startSyncAnimation();
            }
        }
    };

    private BroadcastReceiver mFailedSyncUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Failed Sync Update Receiver -- " +
                    "ProjectGrouping -- called" + message);
        }
    };

    private BroadcastReceiver mLogoutUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.logInfo(LogTags.APP_BACKGROUND_SYNC, "Logout Sync Update Receiver -- " +
                    "ProjectGrouping -- called");
            startLogoutService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_grouping);

        // 1. Initialize data (local and from intent) and toolbar
        // 2. Reference views (View By spinner and RecyclerView)
        // 3. Get grouping attributes and default grouping attribute (first one in the list of grouping attributes)
        // 4. Populate the spinner with the grouping attributes
        // 5. Create OnItemSelectListener() for the spinner to update when the user selects
        // 6. Load the default value in the spinner, which in turn will automatically populate the recyclerView
        // 7. Save and resume state of the activity

        initComponents(this);

        initializeToolbar();

        mRootView = (RecyclerView) findViewById(R.id.project_grouping_root_view);
        mGroupingSpinner = (Spinner) findViewById(R.id.project_grouping_view_by);
        loadMapProgressBar = (ProgressBar) findViewById(R.id.load_map_progress_bar);
        if (savedInstanceState == null) {
            // The project list is being loaded for the first time
            mAppId = getIntent().getStringExtra(Constants.GROUPING_INTENT_APP_ID);
            mGroupingAttributes.addAll(getIntent().getStringArrayListExtra(Constants.GROUPING_INTENT_GROUPING_PARAMETER));
            mProjectTypeName = getIntent().getStringExtra(Constants.PROJECT_TYPE_NAME);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mProjectTypeName);
            }

            if (mAppId != null && mGroupingAttributes != null && !mGroupingAttributes.isEmpty()) {

                // Can we save the grouping attribute in the shared preference???
                String previousGroupingAttribute = mAppPreferences.getString(Constants.APP_ID_TO_GROUPING_ATTRIBUTE_MAP_PREFERENCE_KEY, null);

                if (previousGroupingAttribute == null || previousGroupingAttribute.isEmpty()) {
                    // No map present
                    // initialize grouping attribute and also the map(appId to groupingAttribute) that needs to be saved to the shared preference
                    // convert the map to json string
                    // save to shared preference
                    initializeAndAddGroupingAttributesToSharedPreferences(mGroupingAttributes.get(0), null);
                } else {
                    // get the map
                    Map<String, String> appIdToGroupingAttribute = null;
                    try {
                        appIdToGroupingAttribute = new ObjectMapper().readValue(previousGroupingAttribute, new TypeReference<HashMap<String, String>>(){});
//                            (previousGroupingAttribute, new TypeToken<HashMap<String, String>>() {}.getType());
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (appIdToGroupingAttribute != null) {
                        // Valid map
                        if (appIdToGroupingAttribute.containsKey(mAppId)) {
                            // Grouping attribute present
                            mGroupingAttribute = appIdToGroupingAttribute.get(mAppId);
                        } else {
                            // Entry not present in map, can continue with default
                            initializeAndAddGroupingAttributesToSharedPreferences(mGroupingAttributes.get(0), appIdToGroupingAttribute);
                        }
                    } else {
                        // Map is null, can continue with the default value
                        // Creating grouping attributes
                        initializeAndAddGroupingAttributesToSharedPreferences(mGroupingAttributes.get(0), null);
                    }
                }

                // Populate spinner, and add an OnItemSelected listener
                initializeViewBySpinner();

                // Populate the groups
                initializeGroupsRecyclerView();

            } else {
                // Something is wrong!
                Utils.logError(LogTags.PROJECT_GROUPING, "Inconsistent project grouping parameters for user after AppStartupThread2" +
                        "-- continuing with no data");
                showErrorMessageAndFinishActivity(getResources().getString(R.string.SOMETHING_WENT_WRONG), true);
            }

        } else {
            // The project grouping page is being restored
            mAppId = savedInstanceState.getString(Constants.PROJECT_GROUPING_APP_ID);
            mGroupingAttribute = savedInstanceState.getString(Constants.PROJECT_GROUPING_ATTRIBUTE);
            mProjectTypeName = savedInstanceState.getString(Constants.PROJECT_TYPE_NAME);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mProjectTypeName);
            }
            mGroupingAttributes = new ArrayList<>();
            if (savedInstanceState.getStringArrayList(Constants.PROJECT_GROUPING_ATTRIBUTES) != null) {
                mGroupingAttributes.addAll(savedInstanceState.getStringArrayList(Constants.PROJECT_GROUPING_ATTRIBUTES));
            }
        }
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
        loadMapProgressBar.setVisibility(GONE);
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(Constants.PROJECT_GROUPING_APP_ID, mAppId);
        outState.putString(Constants.PROJECT_GROUPING_ATTRIBUTE, mGroupingAttribute);
        outState.putStringArrayList(Constants.PROJECT_GROUPING_ATTRIBUTES, (ArrayList<String>) mGroupingAttributes);
        outState.putString(Constants.PROJECT_TYPE_NAME, mProjectTypeName);

    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.project_grouping_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    /**
     * This function is called to create the options menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_grouping_menu, menu);

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

        MenuItem item = menu.findItem(R.id.project_group_sync);
        MenuItemCompat.setActionView(item, R.layout.home_sync_action_button);
        View view = MenuItemCompat.getActionView(item);
        mActionBarRefreshIcon = (ImageView) view.findViewById(R.id.refresh_image_to_rotate);

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
     * This function is called every time the user tries to sync the locally saved projects
     */
//    private void initiateManualSync() {
//        AppBackgroundSync appBackgroundSync = new AppBackgroundSync();
//        appBackgroundSync.execute();
//
//        MediaRequestHandlerTask mediaRequestHandlerTask = new MediaRequestHandlerTask();
//        mediaRequestHandlerTask.execute();
//    }

    /**
     * Returns a list of dimensionValues
     * @param groupingAttribute
     * @param appId
     * @return
     */
    private List<String> getProjectGroups(String groupingAttribute, String appId) {

        ProjectList projectList;

        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();

        if (rootConfig == null || rootConfig.mApplications == null) {
            // Error - Root Config inconsistencies
            Utils.logError(LogTags.PROJECT_GROUPING, "No Root Config for user after AppStartupThread2 -- redirecting user to login screen");
            invalidateLogin();
            moveToLoginScreen();
        }

        UAAppContext.getInstance().setProjectList(UAAppContext.getInstance().getDBHelper().getProjectsForUser(UAAppContext
                .getInstance().getUserID(), appId));

        projectList = UAAppContext.getInstance().getProjectList();

        // Extract the groups from the projects
        Set<String> projectGroups = new TreeSet<>();

        if (projectList != null && projectList.mProjects != null && !projectList.mProjects.isEmpty()) {
            for (Project project : projectList.mProjects) {
                if (project.mAttributes != null && !project.mAttributes.isEmpty() && project.mAttributes.containsKey(groupingAttribute)) {
                    String projectGroup = project.mAttributes.get(groupingAttribute);
                    projectGroups.add(Constants.capitalize(projectGroup));
                }
            }

        } else {
            // Case where project list content is null
            Utils.logError(LogTags.PROJECT_LIST, "Inconsistent project list for user after AppStartupThread2 -- continuing with no data");
            return new ArrayList<>(projectGroups);
        }

        return new ArrayList<>(projectGroups);
    }

    /**
     * Assigns grouping attribute, creates or adds to map, and saves it to the Shared Preferences
     * @param groupingAttribute
     * @param appIdToGroupingAttributeMap
     */
    private void initializeAndAddGroupingAttributesToSharedPreferences(String groupingAttribute, Map<String, String> appIdToGroupingAttributeMap) {

        // No map present
        // initialize grouping attribute and also the map(appId to groupingAttribute) that needs to be saved to the shared preference
        // convert the map to json string
        // save to shared preference

        mGroupingAttribute = groupingAttribute;

        if (appIdToGroupingAttributeMap == null) {
            appIdToGroupingAttributeMap = new HashMap<>();
        }

        appIdToGroupingAttributeMap.put(mAppId, mGroupingAttribute);

        JSONObject appIdToGroupingAttributeJson = new JSONObject(appIdToGroupingAttributeMap);

        SharedPreferences.Editor editor = UAAppContext.getInstance().getAppPreferences().edit();
        editor.putString(Constants.APP_ID_TO_GROUPING_ATTRIBUTE_MAP_PREFERENCE_KEY, appIdToGroupingAttributeJson.toString());
        editor.apply();
    }

    private void initializeViewBySpinner() {

        ArrayAdapter<String> spinnerArrayAdapter;

        String[] values = new String[mGroupingAttributes.size()];
        for (int i = 0; i < mGroupingAttributes.size(); i++) {
            values[i] = StringUtils.getTranslatedString(Constants.capitalize(mGroupingAttributes.get(i)));
        }

        spinnerArrayAdapter = new ArrayAdapter<String>
                (this, R.layout.form_spinner_item_layout, values);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGroupingSpinner.setAdapter(spinnerArrayAdapter);

        mGroupingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Item Selected
                // Update grouping attribute in the SharedPreferences
                String previousGroupingAttribute = mAppPreferences.getString(Constants.APP_ID_TO_GROUPING_ATTRIBUTE_MAP_PREFERENCE_KEY, null);

                if (previousGroupingAttribute == null || previousGroupingAttribute.isEmpty()) {
                    // No map present
                    // initialize grouping attribute and also the map(appId to groupingAttribute) that needs to be saved to the shared preference
                    // convert the map to json string
                    // save to shared preference
                    initializeAndAddGroupingAttributesToSharedPreferences(mGroupingAttributes.get(position), null);
                } else {
                    // get the map
                    Map<String, String> appIdToGroupingAttribute = null;
                    ObjectMapper mapper = new ObjectMapper();
//                            new Gson().fromJson(previousGroupingAttribute, new TypeToken<HashMap<String, String>>() {}.getType());
                    try {
                        appIdToGroupingAttribute = mapper.readValue(previousGroupingAttribute, new TypeReference<HashMap<String, String>>() {});
                    } catch (IOException e) {
                        Utils.logError(LogTags.PROJECT_GROUPING_ACTIVITY,"Failed to parse json String :: " + previousGroupingAttribute);
                        e.printStackTrace();
                    }

                    if (appIdToGroupingAttribute != null) {
                        // Valid map
                        initializeAndAddGroupingAttributesToSharedPreferences(mGroupingAttributes.get(position), appIdToGroupingAttribute);
                    } else {
                        // Map is null, can continue with the default value
                        // Creating grouping attributes
                        initializeAndAddGroupingAttributesToSharedPreferences(mGroupingAttributes.get(position), null);
                    }
                }
                initializeGroupsRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        // Set value for spinner
        // Either defualt(first) or previously selected
        int position = 0;
        for (int i = 0; i < mGroupingAttributes.size(); i++) {
            if (mGroupingAttribute.equalsIgnoreCase(mGroupingAttributes.get(i))) {
                position = i;
                break;
            }
        }

        mGroupingSpinner.setSelection(position);
    }

    private void initializeGroupsRecyclerView() {

        mRootView.setLayoutManager(new LinearLayoutManager(this));

        // Set adapter
        mProjectGroups = getProjectGroups(mGroupingAttribute, mAppId);
        if (mProjectGroups == null || mProjectGroups.isEmpty()) {
            TextView no_project_available = (TextView) findViewById(R.id.no_project_available_grouping);
            no_project_available.setText(getResources().getString(R.string.NO_PROJECTS_AVAILABLE));
            no_project_available.setVisibility(View.VISIBLE);

            RelativeLayout project_grouping_view_by_layout = findViewById(R.id.project_grouping_view_by_layout);
            project_grouping_view_by_layout.setVisibility(View.GONE);

            FrameLayout projectGroupingScrollLayout = findViewById(R.id.project_grouping_scroll_layout);
            projectGroupingScrollLayout.setVisibility(GONE);

            mRootView.setVisibility(GONE);

        } else {
            ProjectGroupingAdapter groupingAdapter = new ProjectGroupingAdapter(this, mProjectGroups);
            mProjectGroupingScroller = (FastScroller) findViewById(R.id.project_grouping_scrollbar);

            if (mProjectGroupingScroller != null && mRootView != null) {
                mRootView.setAdapter(groupingAdapter);
                mProjectGroupingScroller.setRecyclerView(mRootView);
            }
            mRootView.addOnItemTouchListener(
                    new RecyclerItemClickListener(this, mRootView,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    // Group has been clicked, populate project list

                                    loadMapProgressBar.setVisibility(View.VISIBLE);
                                    Intent intent = new Intent(ProjectGroupingActivity.this, ProjectListActivity.class);
                                    intent.putExtra(Constants.PROJECT_LIST_INTENT_APP_ID, mAppId);
                                    intent.putExtra(Constants.PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE, mGroupingAttribute);
                                    intent.putExtra(Constants.PROJECT_LIST_INTENT_GROUPING_ATTRIBUTE_VALUE, mProjectGroups.get(position));
                                    intent.putExtra(Constants.PROJECT_TYPE_NAME, mProjectTypeName);
                                    startActivity(intent);
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {
                                    // do whatever
                                }
                            }
                    )
            );
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        MenuItem syn_menu_item = menu.findItem(R.id.project_group_sync);
        View view = MenuItemCompat.getActionView(syn_menu_item);
        TextView unsync_count = (TextView) view.findViewById(R.id.refresh_image_unsync_count);

        int unsyncedProjectCount = getUnsyncedProjectAndMediaCount();
        if (unsyncedProjectCount > 0) {
            unsync_count.setText(String.valueOf(unsyncedProjectCount));
        }

        return true;
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
}
