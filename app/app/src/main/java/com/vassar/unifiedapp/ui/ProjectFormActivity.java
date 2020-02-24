package com.vassar.unifiedapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.LatestProjectDataService;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.AutoPopulateConfig;
import com.vassar.unifiedapp.model.Entity;
import com.vassar.unifiedapp.model.Form;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.LatestFieldValue;
import com.vassar.unifiedapp.model.MultipleValues;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.model.ProjectSpecificForms;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.Validation;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProjectFormActivity extends BaseActivity {

    public String mForm_action_type;
    public String mAppId;
    public Map<String, JSONObject> mSubmittedFields = new HashMap<>();
    public Map<String, Validation> mFieldValidations = new HashMap<>();
    public Map<String, String> mFieldValues = new HashMap<>();
    public String mFormBridgeKey = null;
    public String mFormBridgeValue = null;
    public ArrayList<String> mFormBridgeMandatoryFields = new ArrayList<>();
    public double mUserLatitude = 0.0;
    public double mUserLongitude = 0.0;
    public float mAccuracy = 0.0f;
    public JSONArray mSubmissionArray = new JSONArray();
    public boolean allowBackPress = true;
    public ProjectTypeConfiguration mProjectTypeConfiguration = null;
    public ProjectSpecificForms mProjectSpecificUpdateForm = null;
    public Project mProject = null;
    public long mProjectFormInitializeTimestamp;
    public String mGroupingAttribute;
    public String mGroupingAttributeValue;
    public boolean displayCreatedProjectData = false;
    Map<String, LatestFieldValue> mLatestFieldValueMapSubmittedByUser = new HashMap<>();
    Map<String, Map<String, AutoPopulateConfig>> mAutoPopulateSourceKeyToTargetKeyMap = new HashMap<>();
    private String mProjectId;
    private Map<String, String> mFlatFormStructure = new HashMap<>();
    private String mProjectLatitude;
    private String mProjectLongitude;
    private Form mInitialForm = null;
    private LocationRequest mLocationRequest;
    private FragmentManager mFragmentManager;
    private ProgressBar mProgressBar;
    private String mProjectName;
    private List<String> deletedMediaUUIDs = new ArrayList<>();
    private BroadcastReceiver mLogoutUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startLogoutService();
        }
    };

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_form);

        mProgressBar = findViewById(R.id.form_progress);

        initComponents(this);
        initializeToolbar();

        mProjectId = getIntent().getStringExtra(Constants.PROJECT_FORM_INTENT_PROJECT_ID);
        mAppId = getIntent().getStringExtra(Constants.PROJECT_FORM_INTENT_APP_ID);
        mForm_action_type = getIntent().getStringExtra(Constants.FORM_ACTION_TYPE);
        mProjectName = getIntent().getStringExtra(Constants.PROJECT_FORM_INTENT_PROJECT_NAME);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mProjectName);
        }

        // Initialize timestamp
        mProjectFormInitializeTimestamp = System.currentTimeMillis();

        mGroupingAttribute = getIntent().getStringExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE);
        mGroupingAttributeValue = getIntent().getStringExtra(Constants.PROJECT_LIST_GROUPING_ATTRIBUTE_VALUE);
        initializeConfigs();

        if (mProjectTypeConfiguration != null && mInitialForm != null) {
            createFlatFormStructure(mInitialForm.mFormId, mInitialForm.mFormId);
            loadInitialForm();
        } else {
            showErrorMessageAndFinishActivity(getResources().getString(R.string.COULD_NOT_LOAD_FORM), true);
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.project_form_toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mLogoutUpdateReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.registerReceiver(mLogoutUpdateReceiver, new IntentFilter(Constants.LOGOUT_UPDATE_BROADCAST));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions
            , int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    // TODO: Handle this case
                } else {
                    // permission denied!
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CANNOT_USE_WITHOUT_PERMISSION), false);
                }
                break;
            }

            case Constants.PERMISSION_VIDEO:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    // TODO: Handle this case
                } else {
                    // permission denied!
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CANNOT_USE_WITHOUT_PERMISSION), false);
                }
                break;

            case Constants.PERMISSION_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted!
                    ((UnifiedAppApplication) getApplication()).mFusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(this);
                    ((UnifiedAppApplication) getApplication()).initializeLocation();
                    ((UnifiedAppApplication) getApplication()).startLocationUpdates();
                } else {
                    // permission denied!
                    showErrorMessageAndFinishActivity(getResources().getString(R.string.CANNOT_USE_WITHOUT_PERMISSION), false);
                }
                break;
            }
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
        inflater.inflate(R.menu.project_form_menu, menu);
        return true;
    }

    /**
     * This function initializes the configuration files fetched from the database.
     * It also looks for the project from the ProjectList configuration and assigns
     * the project latitude and longitude, if present
     */
    private void initializeConfigs() {
        mFragmentManager = getSupportFragmentManager();

        // ProjectType & ProjectList Configurations
        showDialog(DIALOG_LOADING);

        mProjectTypeConfiguration = UAAppContext.getInstance().getDBHelper()
                .getProjectFormForApp(UAAppContext.getInstance().getUserID()
                        , mAppId);
        Project getCurrentProject = null;
        if (mProjectId != null) {
            getCurrentProject = UAAppContext.getInstance().getProjectFromProjectList(mProjectId);
        }

        if (getCurrentProject != null) {
            mProject = getCurrentProject;
            LatestProjectDataService latestProjectDataService = LatestProjectDataService.getInstance();
            mLatestFieldValueMapSubmittedByUser = latestProjectDataService
                    .getKeyLatestValuesMapForProject(UAAppContext.getInstance().getUserID(), mAppId, mProjectId);
            if (getCurrentProject.mLatitude != null && getCurrentProject.mLongitude != null) {
                mProjectLatitude = getCurrentProject.mLatitude;
                mProjectLongitude = getCurrentProject.mLongitude;
            }
        }
        dismissDialog(DIALOG_LOADING);

        if (mProject != null) {
            // Adding all the project field values to the map that stored raw key and entered values
            for (ProjectListFieldModel fieldModel : mProject.mFields) {
                String key = fieldModel.mIdentifier;
                if (fieldModel.mProjectListFieldValue != null) {
                    if (fieldModel.mProjectListFieldValue.mValue != null) {
                        mFieldValues.put(key, fieldModel.mProjectListFieldValue.mValue);
                    }
                }
                LatestFieldValue fieldValue = mLatestFieldValueMapSubmittedByUser.get(key);
                if (fieldValue != null) {
                    if (fieldValue.getValue() != null) {
                        mFieldValues.put(key, fieldValue.getValue());
                    }
                }
            }

        }
        // First form in UpdateForms
        Map<String, Map<String, ProjectSpecificForms>> mContent = mProjectTypeConfiguration.mContent;
        Map<String, ProjectSpecificForms> forms = null;
        if (mProjectId != null && mContent.containsKey(mProjectId)) {
            // Project Id has a form
            forms = mContent.get(mProjectId);
        } else {
            // Pick default form
            forms = mContent.get(Constants.DEFAULT_PROJECT_ID);
        }

        if (forms != null) {
            mProjectSpecificUpdateForm = forms.get(mForm_action_type);
            if (mProjectSpecificUpdateForm != null) {
                String initialFormId = mProjectSpecificUpdateForm.mActionForms.mInitialFormId;
                for (Form form : mProjectSpecificUpdateForm.mActionForms.mForms) {
                    if (form.mFormId.equals(initialFormId)) {
                        mInitialForm = form;
                        break;
                    }
                }
            }
        }

    }

    /**
     * Creates a flat structure of the form
     */
    private void createFlatFormStructure(String formId, String tag) {
        for (Form form : mProjectSpecificUpdateForm.mActionForms.mForms) {
            if (formId.equals(form.mFormId)) {
                // Iterating through form fields
                if (form.mFormFields != null) {
                    for (FormField formField : form.mFormFields) {
                        String key = "";
                        key = formField.mIdentifier;
                        if (tag != null)
                            key = tag + "#" + key;
                        switch (formField.mUiType) {
                            case "date":
                            case "textbox":
                            case "edittext":
                            case "geotag":
                            case "geotagimage":
                            case "image":
                            case "timepicker":
                                mFlatFormStructure.put(key, "");
                                break;

                            case "textview":
                                if (formField.mExpandable != null && formField.mExpandable.mSubForm != null)
                                    createFlatFormStructure(formField.mExpandable.mSubForm
                                            , tag + "#" + formField.mExpandable.mSubForm);
                                break;

                            case "dropdown":
                            case "radio":
                            case "checkbox":
                                if (formField.mMultipleValues != null) {
                                    for (MultipleValues value : formField.mMultipleValues) {
                                        if (value.mExpandable != null) {
                                            mFlatFormStructure.put(key, "");
                                            createFlatFormStructure(value.mExpandable.mSubForm
                                                    , tag + "#" + value.mExpandable.mSubForm);
                                        } else {
                                            mFlatFormStructure.put(key, "");
                                        }
                                    }
                                }

                                break;
                        }
                    }
                }

                // Iterating through form buttons
                if (form.mFormButtons != null) {
                    for (FormButton formButton : form.mFormButtons) {
                        if (formButton.mExpandable.mType == 11) {
                            if (formButton.mExpandable.mSubForm != null)
                                createFlatFormStructure(formButton.mExpandable.mSubForm
                                        , tag + "#" + formButton.mExpandable.mSubForm);
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * Load initial form from the ProjectType configuration
     */
    private void loadInitialForm() {
        if (mProjectId == null && mForm_action_type.equals(Constants.INSERT_FORM_KEY)) {
            mProject = new Project();
            mProject.mProjectId = String.valueOf(UUID.randomUUID());
            mProject.mFields = null;
            mProject.mLatitude = "0";
            mProject.mLongitude = "0";
            mProject.mProjectIcon = null;
            mProject.mState = "New";
            mProject.mUserType = "DEFAULT";
            mProject.mAssigned = true;
            mProject.mExpired = false;
            mProject.mLastSubDate = "0";
            mProject.mLastSyncTimestamp = 0;
            mProject.mProjectName = "";

        }
        if (mProjectId != null && mForm_action_type.equals(Constants.INSERT_FORM_KEY)) {
            displayCreatedProjectData = true;
        }
        ProjectFormFragment fragment = new ProjectFormFragment(mInflater, mInitialForm, mProject, mInitialForm.mFormId, mLatestFieldValueMapSubmittedByUser);
        mFragmentManager.beginTransaction().add(R.id.project_form_activity_root, fragment).commit();
    }

    /**
     * This function is called to add a fragment with subform to the ProjectFormActivity
     */
    public void addNewFormFragment(String subformId, String tag) {

        Form subform = null;

        // Getting subform from the id in the expandable object of the view clicked
        for (Form form : mProjectSpecificUpdateForm.mActionForms.mForms) {
            if (subformId.equals(form.mFormId)) {
                subform = form;
                break;
            }
        }

        if (subform != null) {
            ProjectFormFragment fragment = new ProjectFormFragment(mInflater, subform, mProject
                    , tag + "#" + subformId, mLatestFieldValueMapSubmittedByUser);
            mFragmentManager.beginTransaction().replace(R.id.project_form_activity_root
                    , fragment).addToBackStack(null).commit();
        } else {
            // Sub form not present in the
            showErrorMessageAndFinishActivity(getResources().getString(R.string.SOMETHING_WENT_WRONG), false);
        }
    }

    /**
     * Returns a form using the id sent from the ProjectFormFragment
     */
    public Form getFormFromId(String id) {
        if (id == null || id.isEmpty())
            return null;
        Form subform = null;
        for (Form form : mProjectSpecificUpdateForm.mActionForms.mForms) {
            if (id.equals(form.mFormId)) {
                subform = form;
                break;
            }
        }
        return subform;
    }

    /**
     * This function is called when the cancel button is pressed on the fragment
     */
    public void popCurrentFragment() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            mFragmentManager.popBackStackImmediate();
        }
    }

    /**
     * Add fields to the data structure containing the submitted fields
     */
    public void addFormFieldsSaved(String key, JSONObject jsonObject) {
        mSubmittedFields.put(key, jsonObject);
    }

    public JSONObject getSubmittedField(String key) {
        return mSubmittedFields.get(key);
    }

    /**
     * Creates a tag if the initial form does not have a preview button(prefix
     * to UI element keys)
     */
    public String createTagToIdentifyFields() {
        String tag = "";
        tag += getSubFormIdFromEditButton(mInitialForm, "");
        return tag;
    }

    private String getSubFormIdFromEditButton(Form form, String tag) {
        tag += form.mFormId;
        if (form.mFormButtons != null) {
            for (FormButton formButton : form.mFormButtons) {
                if (formButton.mExpandable.mType == 11) {
                    tag = getSubFormIdFromEditButton(getFormFromId(formButton.mExpandable.mSubForm), tag + "#");
                }
            }
        }
        return tag;
    }

    /**
     * Adding value to submission array. It overwrites any objects that have the same key
     */
    public void addToSubmissionArray(JSONObject jsonObject) {
        try {
            if (mSubmissionArray != null && jsonObject != null) {
                for (int i = 0; i < mSubmissionArray.length(); i++) {
                    JSONObject savedJson = mSubmissionArray.getJSONObject(i);
                    if (savedJson != null && savedJson.get("key").equals(jsonObject.get("key"))) {
                        mSubmissionArray.remove(i);
                        break;
                    }
                }
                String value = String.valueOf(jsonObject.get("val"));
                if (value != null && !value.isEmpty())
                    mSubmissionArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.logError("JSONException", "Exception adding value to JSON array");
        }
    }

    public void clearSubmissionArray() {
        if (mSubmissionArray != null) {
            mSubmissionArray = new JSONArray();
        }
    }

    public void printSubmissionArray() {
        if (mSubmissionArray != null) {
            for (int i = 0; i < mSubmissionArray.length(); i++) {
                try {
                    JSONObject jsonObject = mSubmissionArray.getJSONObject(i);
                    Utils.getInstance().showLog("SUBMISSION ARRAY ", jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void printSubmittedFields() {
        if (mSubmittedFields != null) {
            for (Map.Entry<String, JSONObject> entry : mSubmittedFields.entrySet()) {
                Utils.getInstance().showLog("KEY : " + entry.getKey(), "VALUE : " + entry.getValue().toString());
            }
        }
    }

    public Form getInitialForm() {
        return mInitialForm;
    }

    public String getProjectLatitude() {
        if (mProjectLatitude != null)
            return mProjectLatitude;
        else
            return "";
    }

    public String getProjectLongitude() {
        if (mProjectLongitude != null)
            return mProjectLongitude;
        else
            return "";
    }

    public JSONArray getSubmissionArray() {
        return mSubmissionArray;
    }

    public void disableUI() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void enableUI() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            boolean hasFragment = mFragmentManager.popBackStackImmediate();
            if (!hasFragment) {
                super.onBackPressed();
                finish();
            }
        }
    }

    public void addToAutoPopulateKeysMap(String sourceKey, String targetKey, AutoPopulateConfig autoPopulateConfig) {
        if (mAutoPopulateSourceKeyToTargetKeyMap == null) {
            mAutoPopulateSourceKeyToTargetKeyMap = new HashMap<>();
        }
        if (sourceKey != null && !sourceKey.isEmpty() && targetKey != null && !targetKey.isEmpty() && autoPopulateConfig != null) {
            if (mAutoPopulateSourceKeyToTargetKeyMap.get(sourceKey) == null) {
                mAutoPopulateSourceKeyToTargetKeyMap.put(sourceKey, new HashMap<>());
            }
            mAutoPopulateSourceKeyToTargetKeyMap.get(sourceKey).put(targetKey, autoPopulateConfig);
        }
    }

    public void autoPopulateDependentFields(String sourceKey) {
        if (mAutoPopulateSourceKeyToTargetKeyMap != null && sourceKey != null && !sourceKey.isEmpty()) {
            Map<String, AutoPopulateConfig> keyToAutoPopulateConfigMap = mAutoPopulateSourceKeyToTargetKeyMap.get(sourceKey);
            if (mAutoPopulateSourceKeyToTargetKeyMap.get(sourceKey) == null || mAutoPopulateSourceKeyToTargetKeyMap.get(sourceKey).isEmpty()) {
                return;
            }
            for (String key : keyToAutoPopulateConfigMap.keySet()) {
                AutoPopulateConfig config = keyToAutoPopulateConfigMap.get(key);
                if (config == null) {
                    return;
                }
                switch (config.getResourceLocation()) {
                    case "GROUPBY_ATTRIBUTE": {
                        String targetKey = key;
                        if (mGroupingAttribute != null && mGroupingAttribute.equalsIgnoreCase(sourceKey) && mGroupingAttributeValue != null) {
                            ((EditText) config.mView).setText(mGroupingAttributeValue);
                        }
                    }
                    break;
                    case "FORM_SUBMISSION": {
                        String sourceKeyValue = getSubmittedFieldFromKey(sourceKey);
                        String targetKey = key;
                        if (config != null && sourceKeyValue != null && !targetKey.isEmpty() && config.mView != null) {
                            ((EditText) config.mView).setText(sourceKeyValue);
                            if (targetKey.equalsIgnoreCase("proj_name")) {
                                mProject.mProjectName = sourceKeyValue;
                            }
                        }

                    }
                    break;
                    case "IMAGE_GEOTAG": {
                        String sourceKeyValue = getSubmittedFieldFromKey(sourceKey);
                        String[] UUIDListWithGeotag = sourceKeyValue.split("\\s*,\\s*");
                        String lat = "";
                        String lon = "";
                        if (sourceKeyValue == null || sourceKeyValue.isEmpty()) {
                            break;
                        }
                        for (String uuidWithGeotag : UUIDListWithGeotag) {
                            List<String> uuidAndlatLon = StringUtils.getStringListFromDelimiter(Constants.IMAGE_UUID_LONG_LAT_SEPARATOR, uuidWithGeotag);
                            if (uuidAndlatLon.size() == 3) {
                                lat = uuidAndlatLon.get(2);
                                lon = uuidAndlatLon.get(1);
                            }
                            break;
                        }
                        String targetKeyValue = lat + "," + lon;
                        String targetKey = key;
                        if (config != null && sourceKeyValue != null && !targetKey.isEmpty() && config.mView != null) {
                            ((TextView) config.mView).setText(targetKeyValue);
                        }
                    }
                    break;
                    case "ENTITY_CONFIG": {


                        String parentkeyValue = getEntityParentValue(config);
                        List<Entity> entityList = mDBHelper.getEntityList(Constants.SUPER_APP_ID, Constants.DEFAULT_PROJECT_ID, Constants.DEFAULT_PROJECT_ID, null, parentkeyValue, config.mEntityName);
                        List<String> dropdownValues = new ArrayList<>();
                        for (Entity entity : entityList) {
                            dropdownValues.add(entity.getName());
                        }
                        populateValues(config, dropdownValues);
                    }
                    break;
                }
            }
        }

    }

    public void autoPopulateField(String targetKey) {
        if (mAutoPopulateSourceKeyToTargetKeyMap != null && targetKey != null && !targetKey.isEmpty()) {
            for (String sourceKey : mAutoPopulateSourceKeyToTargetKeyMap.keySet()) {
                Map<String, AutoPopulateConfig> keyToAutoPopulateConfigMap = mAutoPopulateSourceKeyToTargetKeyMap.get(sourceKey);
                AutoPopulateConfig config = keyToAutoPopulateConfigMap.get(targetKey);
                if (config == null) {
                    return;
                }

                String sourceKeyValue = getSubmittedFieldFromKey(sourceKey);
                switch (config.getResourceLocation()) {
                    case "ROOTCONFIG_GROUPING_ENTITY": {

                        ProjectTypeModel projectTypeModel = UAAppContext.getInstance().getProjectTypeModel(mAppId);
                        if (projectTypeModel.mGroupEntitiesList != null) {
                            for (Map<String, String> attributeMap : projectTypeModel.mGroupEntitiesList) {
                                if (attributeMap != null && !attributeMap.isEmpty()) {

                                    String groupKey = mGroupingAttribute;
                                    String groupKeyValue = mGroupingAttributeValue;
                                    if (attributeMap.containsKey(targetKey) && attributeMap.get(targetKey) != null &&
                                            groupKey != null && !groupKey.isEmpty() && mGroupingAttributeValue != null && !mGroupingAttributeValue.isEmpty()) {
                                        if (attributeMap.containsKey(groupKey) && groupKeyValue.equalsIgnoreCase(attributeMap.get(groupKey))) {
                                            ((EditText) config.mView).setText(attributeMap.get(targetKey));
                                            if (projectTypeModel.mGroupingAttributes != null || projectTypeModel.mFilteringAttributes != null) {
                                                mProject.setAttributes(attributeMap);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    break;
                    case "GROUPBY_ATTRIBUTE": {
                        if (mGroupingAttribute != null && mGroupingAttribute.equalsIgnoreCase(sourceKey) && mGroupingAttributeValue != null) {
                            ((EditText) config.mView).setText(mGroupingAttributeValue);
                        }
                    }
                    break;
                    case "ENTITY_CONFIG": {

                        Spinner nextSpinner = (Spinner) config.mView;
                        String parentkeyValue = getEntityParentValue(config);
                        List<Entity> entityList = mDBHelper.getEntityList(Constants.SUPER_APP_ID, Constants.DEFAULT_PROJECT_ID, Constants.DEFAULT_PROJECT_ID, null, parentkeyValue, config.mEntityName);
                        List<String> dropdownValues = new ArrayList<>();
                        for (Entity entity : entityList) {
                            dropdownValues.add(entity.getName());
                        }
                        populateValues(config, dropdownValues);
                    }
                }
            }

        }
    }

    private String getEntityParentValue(AutoPopulateConfig config) {
        String parent = "DEFAULT";
        if (config != null) {
            String dimensionName = config.mDimensionName;
            List<String> nameList = StringUtils.getStringListFromDelimiter("##", dimensionName);
            for (String name : nameList
            ) {
                List<String> entityTypeValue = StringUtils.getStringListFromDelimiter("\\$\\$", name);
                String value = getSubmittedFieldFromKey(entityTypeValue.get(1));
                parent = parent + "##" + entityTypeValue.get(0) + "$$" + value;

            }
        }
        return parent;
    }

    private void populateValues(AutoPopulateConfig config, List<String> dropdownValues) {
        Collections.sort(dropdownValues);
        switch (config.mUiType) {
            case "dropdown":
                Spinner spinner = (Spinner) config.mView;
                List<String> values = new ArrayList<>();
                values.add("Select");
                values.addAll(dropdownValues);

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                        (this, R.layout.form_spinner_item_layout, values);

                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinnerArrayAdapter.notifyDataSetChanged();

                break;

            default:
                break;
        }
    }

    public String getSubmittedFieldFromKey(String sskey) {
        String val = null;
        for (String key : mSubmittedFields.keySet()) {
            System.out.println("getKeys" + key);
            JSONObject valueObject = mSubmittedFields.get(key);
            String[] keys = key.split("#");
            String finalKey = getKeyName(keys[keys.length - 1]);
            if (finalKey.equalsIgnoreCase(sskey)) {
                try {
                    val = valueObject.getString("val");
                    if (val == null || val.isEmpty()) {
                        val = getFieldvalue(sskey);
                    }
                    return val;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return val;
    }

    private String getKeyName(String keyName) {
        List<String> keys = Arrays.asList
                (keyName.trim().split("\\$\\$"));
        String key = null;
        if (keys != null && !keys.isEmpty()) {
            key = keys.get(keys.size() - 1);
        }
        return key;
    }

    private String getFieldvalue(String key) {
        return mFieldValues.get(key);
    }

    public void deletedMediaFromDBAndStorage() {
        for (String uuid : deletedMediaUUIDs) {
            FormMedia formImage = mDBHelper.getFormMedia(uuid, mAppId, UAAppContext.getInstance().getUserID());
            UAAppContext.getInstance().getDBHelper().deleteFormMedia(formImage.getmUUID());

            File imgFile = new File(formImage.getLocalPath());
            String fileName = imgFile.getName().substring(0, imgFile.getName().indexOf("."));
            if (formImage.getmUUID().equals(fileName)) {
                Utils.getInstance().deleteImageFromStorage(this, formImage.getLocalPath());
            }
        }
    }

    public void addToDeletedMediaUUIDs(String uuid){
        if (uuid != null && !uuid.isEmpty()){
            deletedMediaUUIDs.add(uuid);
        }
    }
}