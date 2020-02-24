package com.vassar.unifiedapp.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.LocationServices;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.LatestProjectDataService;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AutoPopulateConfig;
import com.vassar.unifiedapp.model.Entity;
import com.vassar.unifiedapp.model.Form;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.LatestFieldValue;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.model.ProjectSpecificForms;
import com.vassar.unifiedapp.model.ProjectTypeConfiguration;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.internal.Util;

public class TabularFormActivity extends BaseActivity {

    private ProgressBar mProgressBar;
    private FragmentManager mFragmentManager;
    private ProjectTypeConfiguration mProjectTypeConfiguration;
    private ProjectSpecificForms mUpdateForm;
    public Map<String, String> mProjectLatestFieldValues = new HashMap<>();
    Map<String, LatestFieldValue> mLatestFieldValueMapSubmittedByUser = new HashMap<>();
    public boolean allowBackPress = true;
    private Map<String, String> mUserEnteredValues = new HashMap<>();
    private Project mCurrentProject  = null;
    private String mAppId = "";
    private String mProjectName;

    private Map<String, LinkedHashMap<Integer, Map<String, String>>>
            mUserEnteredTableValues = new HashMap<>();

    public Map<String, Integer> newPageTableFieldKeyToLastIndexMap = new HashMap<>();
    Map<String, Map<String, AutoPopulateConfig>> mAutoPopulateSourceKeyToTargetKeyMap = new HashMap<>();

    private List<String> deletedMediaUUIDs = new ArrayList<>();

    private double mUserLatitude = 0.0;
    private double mUserLongitude = 0.0;
    private float mAccuracy = 0.0f;
    // --- Saved instance state == null
    // ------ Initialize views
    // ------ Get project type configuration
    // ------ Get project from the project list
    // ------ Load the fragment
    //
    // --- Saved instance state != null
    // ------ Restore variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabular_form);

        mProgressBar = (ProgressBar) findViewById(R.id.tabular_form_progress);

        initComponents(this);
        initializeToolbar();

        // Get projectId and appId from intent
        String projectId = "";

        projectId = getIntent().getStringExtra(Constants.PROJECT_FORM_INTENT_PROJECT_ID);
        mAppId = getIntent().getStringExtra(Constants.PROJECT_FORM_INTENT_APP_ID);
        mProjectName = getIntent().getStringExtra(Constants.PROJECT_FORM_INTENT_PROJECT_NAME);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mProjectName);
        }

        System.out.println("APAIMS XXXXX PROJECT ID : " + projectId);
        System.out.println("APAIMS XXXXX APP ID : " + mAppId);

        // Fetch the project type configuration and the project list configuration
        mProjectTypeConfiguration = UAAppContext.getInstance().getDBHelper()
                .getProjectFormForApp(UAAppContext.getInstance().getUserID()
                        , mAppId);
        if (mProjectTypeConfiguration == null || mProjectTypeConfiguration.mContent == null) {
            showErrorMessageAndFinishActivity(getResources().getString(R.string.COULD_NOT_LOAD_FORM), true);
            return;
        }
        Project currentProject = UAAppContext.getInstance().getProjectFromProjectList(projectId);
        mCurrentProject = currentProject;

        if (mCurrentProject == null){
            return;
        }
        fetchLatestProjectFieldsValue() ;
        // First form in UpdateForms
        Map<String, Map<String, ProjectSpecificForms>> mContent = mProjectTypeConfiguration.mContent;
        Map<String, ProjectSpecificForms> forms = null;
        if (mContent.containsKey(projectId)) {
            // Project Id has a form
            forms = mContent.get(projectId);
        } else {
            // Pick default form
            forms = mContent.get(Constants.DEFAULT_PROJECT_ID);
        }

        Form initialForm = null;

        if (forms != null) {
            mUpdateForm = forms.get(Constants.UPDATE_FORM_KEY);
            if (mUpdateForm != null) {
                String initialFormId = mUpdateForm.mActionForms.mInitialFormId;
                for (Form form : mUpdateForm.mActionForms.mForms) {
                    if (form.mFormId.equals(initialFormId)) {
                        initialForm = form;
                        break;
                    }
                }
            }
        }

        if(initialForm != null) {

            mFragmentManager = getSupportFragmentManager();

            TabularFormFragment tabularFormFragment = new TabularFormFragment(initialForm, currentProject, null, null);
            mFragmentManager.beginTransaction().add(R.id.tabular_form_activity_root, tabularFormFragment).commit();
        } else {
            showErrorMessageAndFinishActivity(getResources().getString(R.string.COULD_NOT_LOAD_FORM), true);
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tabular_form_toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            //to make background not clickable
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public Form getFormFromConfiguration(String formId) {
        for (Form form : mUpdateForm.mActionForms.mForms) {
            if (form.mFormId.equals(formId)) {
                return form;
            }
        }

        return null;
    }

    /*
    User entered values that are a part of the table widget or a sub-form
    of any of the table widget components.

    This map is consolidated with the map of the non-table widget values and sent as
    the submission array.
     */
    public void addToUserEnteredValuesMap(String fieldKey, int index, String cellKey, String value) {
        if(value != null && (value.equalsIgnoreCase("null") || value.equalsIgnoreCase("-")))
            return;
        if (mUserEnteredTableValues != null) {

            if (mUserEnteredTableValues.containsKey(fieldKey)) {

                if (mUserEnteredTableValues.get(fieldKey).containsKey(index)) {

//                    mUserEnteredTableValues.get(fieldKey).get(index).put(cellKey, value);

                } else {
                    // Create new entry for index
                    Map<String, String> indexEntry = new HashMap<>();
//                    indexEntry.put(cellKey, value);
                    mUserEnteredTableValues.get(fieldKey).put(index, indexEntry);
                }

            } else {
                // Create new entry for fieldKey
                LinkedHashMap<Integer, Map<String, String>> fieldEntry = new LinkedHashMap<>();

                Map<String, String> indexEntry = new HashMap<>();
//                indexEntry.put(cellKey, value);

                fieldEntry.put(index, indexEntry);

                mUserEnteredTableValues.put(fieldKey, fieldEntry);
            }
            if(value == null || value.isEmpty()) {
                if(mUserEnteredTableValues.get(fieldKey).get(index).containsKey(cellKey)) {
                    mUserEnteredTableValues.get(fieldKey).get(index).remove(cellKey);
                }
            } else {
                mUserEnteredTableValues.get(fieldKey).get(index).put(cellKey, value);
            }
        } else {
            Utils.logError(UAAppErrorCodes.NULL_POINTER, "Exception while adding" +
                    "value to table submission map -- map is null");
        }
    }

    /*
    User entered values that are not a part of the table widget or a sub-form
    of any of the table widget components.

    This map is consolidated with the map of the table widget values and sent as
    the submission array.
     */
    public void addToUserEnteredValues(String key, String value) {
        if(value == null || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("-"))
            return;
        if (mUserEnteredValues != null) {
            mUserEnteredValues.put(key, value);
        } else {
            Utils.logError(UAAppErrorCodes.NULL_POINTER, "Exception while adding" +
                    "value to submission map -- map is null");
        }
    }

    /*
    Consolidates the values from the table and non-table widget maps and makes a
    submission array
     */
    public JSONArray createSubmissionArray() {

        JSONArray jsonArray = new JSONArray();

        if (mUserEnteredTableValues != null && mUserEnteredValues != null) {

            // Adding elements of the non-table values to the submission array

            for (String key : mUserEnteredValues.keySet()) {
                JSONObject jsonObject = createJSONObjectForSubmissionValue(key, mUserEnteredValues.get(key), "");
                jsonArray.put(jsonObject);
            }

            for (Map.Entry fieldKeyEntry : mUserEnteredTableValues.entrySet()) {
                JSONArray fieldJSONArray = new JSONArray();
                if (mUserEnteredTableValues.get(fieldKeyEntry.getKey()).entrySet() != null) {
                    for (Map.Entry indexEntry : mUserEnteredTableValues.get(fieldKeyEntry.getKey()).entrySet()) {
                        if (mUserEnteredTableValues.get(fieldKeyEntry.getKey()).get(indexEntry.getKey()).entrySet() != null) {
                            JSONObject jsonObject = new JSONObject();
                            for (Map.Entry cellKeyEntry : mUserEnteredTableValues.get(fieldKeyEntry.getKey()).get(indexEntry.getKey()).entrySet()) {
                                try {
                                    jsonObject.put(cellKeyEntry.getKey().toString(), mUserEnteredTableValues
                                            .get(fieldKeyEntry.getKey()).get(indexEntry.getKey()).get(cellKeyEntry.getKey()));
                                } catch (JSONException e) {
                                    Utils.logError(UAAppErrorCodes.JSON_ERROR, "Exception while creating JSON object" +
                                            "for submission array -- not stopping execution -- sending incomplete JSON object", e);
                                }
                            }
                            fieldJSONArray.put(jsonObject);
                        }
                    }
                }
                JSONObject tableJSONObject = createJSONObjectForSubmissionValue(fieldKeyEntry.getKey().toString(),
                        fieldJSONArray.toString(), "");
                jsonArray.put(tableJSONObject);
            }

        } else {
            Utils.logError(UAAppErrorCodes.NULL_POINTER, "Exception while adding" +
                    "value to submission map -- one or both of the maps is null");
        }

        return jsonArray;
    }

    /*
    Creates a JSON Object for the submission value, its datatype and the corresponding field key (identifier)
     */
    private JSONObject createJSONObjectForSubmissionValue(String key, String value, String datatype) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.SUBMISSION_OBJECT_KEY, key);
            jsonObject.put(Constants.SUBMISSION_OBJECT_DT, datatype);
            jsonObject.put(Constants.SUBMISSION_OBJECT_VAL, value);
        } catch (JSONException e) {
            Utils.logError(UAAppErrorCodes.JSON_ERROR, "Exception while creating JSON object" +
                    "for submission array -- not stopping execution -- sending incomplete JSON object", e);
        }

        return jsonObject;
    }

    public String getAppId() {
        return mAppId;
    }

    public ProjectSpecificForms getUpdateForm() {
        return mUpdateForm;
    }

    public void disableUI() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void enableUI() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public Map<String, LinkedHashMap<Integer, Map<String, String>>> getUserEnteredTableValues() {
        return mUserEnteredTableValues;
    }

    public void initializeUserEnteredTableValues() {
        mUserEnteredTableValues = new HashMap<>();
    }

    public void addToNewPageSubformTableValuesMap(String key, LinkedHashMap<Integer, Map<String, String>> linkedHashMap) {
        mUserEnteredTableValues.put(key, linkedHashMap);
    }

    public Map<String, String> getmUserEnteredValues() {
        return mUserEnteredValues;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[]
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
        }
    }

    public double getUserLatitude() {
        return mUserLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        this.mUserLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return mUserLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        this.mUserLongitude = userLongitude;
    }

    public float getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(float accuracy) {
        this.mAccuracy = accuracy;
    }

    public String getValueForKey(String key) {

        if(mUserEnteredValues == null || mUserEnteredValues.get(key) == null) {
            return getValueFromProjectMetaData(key);
        }
        return mUserEnteredValues.get(key);
    }
    public String getValueForKeyWithTag(String key, String tag) {
        if(tag ==  null) {
            return getValueForKey(key);
        }
        List<String> fieldInfo = Arrays.asList
                (tag.trim().split("##"));

        String value = getValueForKey(fieldInfo.get(0),
                        Integer.parseInt(fieldInfo.get(1)),
                key);
        return  value;
    }

    public String getValueForKey(String fieldKey, int index, String cellKey) {

        if (mUserEnteredTableValues  == null ) {
            return null;
        }
        if (mUserEnteredTableValues.containsKey(fieldKey)
                && mUserEnteredTableValues.get(fieldKey) != null
                && mUserEnteredTableValues.get(fieldKey).containsKey(index)
                && mUserEnteredTableValues.get(fieldKey).get(index) != null
                && mUserEnteredTableValues.get(fieldKey).get(index).get(cellKey) != null
        ) {
            return mUserEnteredTableValues.get(fieldKey).get(index).get(cellKey);
        } else {
            return getValueFromProjectMetaData(fieldKey, index, cellKey);
        }


    }

    public String getValueFromProjectMetaData(String fieldKey, int index, String cellKey) {
        String value = null;
        String jsonArrayString =  getValueFromProjectMetaData(fieldKey);
        if(jsonArrayString != null && !jsonArrayString.isEmpty()) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(jsonArrayString);
                if (jsonArray.length() > 0 && jsonArray.length() > index) {
                    // For each JSON object, render a row and fetch the values from the object
                    JSONObject jsonObject = jsonArray.getJSONObject(index);
                    if(jsonObject.has(cellKey)) {
                        value = jsonObject.getString(cellKey);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public String getValueFromProjectMetaData(String fieldKey) {
        String value = null;
        value = mProjectLatestFieldValues.get(fieldKey);
        return  value;
    }

    public List<String> getImageUUIDsFromSubmissionArray(JSONArray submissionArray) {
        List<String> uuids = new ArrayList<>();

        for (int i = 0; i < submissionArray.length(); i++) {

            try {
                JSONObject jsonObject = submissionArray.getJSONObject(i);

                String val = jsonObject.getString(Constants.SUBMISSION_OBJECT_VAL);

                if (val != null && !val.isEmpty()) {

                    if (Utils.getInstance().isJSONValid(val)) {
                        // Is a valid JSON and
                        if(val.startsWith("\"[") || val.startsWith("[")) {
                            JSONArray jsonArray = new JSONArray(val);
                            for(int index = 0 ; index < jsonArray.length() ; index++) {
                                JSONObject jsonObject1 = jsonArray.optJSONObject(index);
                                Type type = new TypeToken<Map<String, String>>(){}.getType();
                                Map<String, String> map = new HashMap<>();
//                                new Gson().fromJson(jsonObject1.toString(), type);
                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    map = mapper.readValue(jsonObject1.toString(), new TypeReference<Map<String, String>>(){});
                                } catch (IOException e) {
                                    Utils.logError(LogTags.TABULAR_FORM_ACTIVITY, "Failed to parse Json String :: " + jsonObject1.toString());
                                    e.printStackTrace();
                                }
                                for(String key : map.keySet()) {
                                   extractUUIDs(map.get(key), uuids);
                                }
                            }
                        }

                    } else {
                        // Is not a JSON
                        extractUUIDs(val, uuids);
                    }
                }

            } catch (JSONException e) {
                Utils.logError(UAAppErrorCodes.JSON_ERROR, "Exception while getting uuids" +
                        "from submission array -- ignoring uuid ", e);
            }
        }
        return uuids;
    }

    private void extractUUIDs(String val, List<String> UUIDList) {

        List<String> innerUUids = Arrays.asList
                (val.trim().split(","));

        for (String uuidWithLatLong : innerUUids) {
            String uuid = uuidWithLatLong.split("##")[0];
            if (Utils.getInstance().isValidUUID(uuid)) {
                UUIDList.add(uuid);
            }
        }
    }
    public void fetchLatestProjectFieldsValue() {
        if(mCurrentProject == null) {return;}
        LatestProjectDataService latestProjectDataService = LatestProjectDataService.getInstance();
        mLatestFieldValueMapSubmittedByUser = latestProjectDataService
                .getKeyLatestValuesMapForProject(UAAppContext.getInstance().getUserID(), mAppId, mCurrentProject.mProjectId);
        if(mCurrentProject !=  null && mCurrentProject.mFields != null) {
            for (ProjectListFieldModel fieldModel : mCurrentProject.mFields) {
                String key = fieldModel.mIdentifier;
                if (fieldModel.mProjectListFieldValue != null) {
                    if (fieldModel.mProjectListFieldValue.mValue != null) {
                        mProjectLatestFieldValues.put(key, fieldModel.mProjectListFieldValue.mValue);
                    }
                }
                LatestFieldValue fieldValue = mLatestFieldValueMapSubmittedByUser.get(key);
                if(fieldValue != null ) {
                    if(fieldValue.getValue() != null) {
                        mProjectLatestFieldValues.put(key, fieldValue.getValue());
                    }
                };
            }
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    /** This function is called to add a fragment with subform to the TabularFormActivity */
    public void addNewFormFragment(Form form, String tag, Map<String, String> fragmentTransitionInfo) {

        if (form != null) {
            TabularFormFragment tabularFormFragment = new TabularFormFragment(form, mCurrentProject, fragmentTransitionInfo, tag);
            mFragmentManager.beginTransaction().replace(R.id.tabular_form_activity_root,
                    tabularFormFragment).addToBackStack(null).commit();
        } else {
            // Sub form not present in the
            showErrorMessageAndFinishActivity(getResources().getString(R.string.SOMETHING_WENT_WRONG), false);
        }
    }

    /** This function is called when the cancel button is pressed on the fragment */
    public void popCurrentFragment() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            mFragmentManager.popBackStackImmediate();
        }
    }
    public void  addToTableValueMap(String fieldKey, LinkedHashMap<Integer, Map<String, String>> indexToKeyToValueMap) {
        if(mUserEnteredTableValues == null ) {
            mUserEnteredTableValues =  new HashMap<>();
        }
           mUserEnteredTableValues.put(fieldKey, indexToKeyToValueMap);

    }

    public void removeKeyToValueFromUserEnteredValuesMap(String key) {
        if (mUserEnteredValues != null && mUserEnteredValues.get(key) != null) {
            mUserEnteredValues.remove(key);
        }
    }
    public void removeKeyFromUserEnteredTableValues(String parentKey, int index, String key) {
        if(mUserEnteredTableValues == null || mUserEnteredTableValues.get(parentKey) == null
                ||  mUserEnteredTableValues.get(parentKey).get(index) == null || mUserEnteredTableValues.get(parentKey).get(index).get(key) == null ) {
            return;
        } else {
            mUserEnteredTableValues.get(parentKey).get(index).remove(key);
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
                   /* case "GROUPBY_ATTRIBUTE": {
                        String targetKey = key;
                        if (mGroupingAttribute != null && mGroupingAttribute.equalsIgnoreCase(sourceKey) && mGroupingAttributeValue != null) {
                            ((EditText) config.mView).setText(mGroupingAttributeValue);
                        }
                    }
                    break;*/
                    case "FORM_SUBMISSION": {
                        String sourceKeyValue = getValueForKey(sourceKey);
                        String targetKey = key;
                        if (config != null && sourceKeyValue != null && !targetKey.isEmpty() && config.mView != null) {
                            ((EditText) config.mView).setText(sourceKeyValue);
                            if (targetKey.equalsIgnoreCase("proj_name")) {
                                mCurrentProject.mProjectName = sourceKeyValue;
                            }
                        }

                    } break;
                    case "IMAGE_GEOTAG" : {
                        String sourceKeyValue = getValueForKey(sourceKey);
                        List<String> UUIDListWithGeotag = Arrays.asList(sourceKeyValue.split("\\s*,\\s*"));
                        String lat = "";
                        String lon = "";
                        if(sourceKeyValue == null || sourceKeyValue.isEmpty() ) {
                            break;
                        }
                        for ( String uuidWithGeotag: UUIDListWithGeotag) {
                            List<String> uuidAndlatLon = StringUtils.getStringListFromDelimiter( Constants.IMAGE_UUID_LONG_LAT_SEPARATOR ,uuidWithGeotag);
                            if(uuidAndlatLon.size() == 3) {
                                lat = uuidAndlatLon.get(2);
                                lon = uuidAndlatLon.get(1);
                            }
                            break;
                        }
                        String targetKeyValue  = lat + "," + lon;
                        String targetKey = key;
                        if (config != null && sourceKeyValue != null && !targetKey.isEmpty() && config.mView != null) {
                            ((TextView) config.mView).setText(targetKeyValue);
                        }
                    }
                    break;
                    case "ENTITY_CONFIG": {


                        String parentkeyValue  = getEntityParentValue(config);
                        List<Entity> entityList =  mDBHelper.getEntityList(Constants.SUPER_APP_ID, Constants.DEFAULT_PROJECT_ID,Constants.DEFAULT_PROJECT_ID, null, parentkeyValue, config.mEntityName);
                        List<String> dropdownValues = new ArrayList<>();
                        for (Entity entity: entityList) {
                            dropdownValues.add(entity.getName());
                        }
                        populateValues(config, dropdownValues, key);
                    } break;
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
                String sourceKeyValue = null;
                List<String> sourceKeyInfo = StringUtils.getStringListFromDelimiter("\\$\\$", sourceKey);
                String tag = null;
                if(sourceKeyInfo.size() > 1) {
                    tag = sourceKeyInfo.get(1);
                }
                if(tag != null && !tag.isEmpty()) {

                    sourceKeyValue = getValueForKeyWithTag(sourceKey, tag);
                } else {
                    sourceKeyValue = getValueForKey(sourceKey);
                }
                switch (config.getResourceLocation()) {
                   /* case "ROOTCONFIG_GROUPING_ENTITY": {

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
                    } break;*/
                    case "ENTITY_CONFIG": {

                        String parentKeyValue  = getEntityParentValue(config);
                        List<Entity> entityList =  mDBHelper.getEntityList(Constants.SUPER_APP_ID, Constants.DEFAULT_PROJECT_ID,Constants.DEFAULT_PROJECT_ID, null, parentKeyValue, config.mEntityName);
                        List<String> dropdownValues = new ArrayList<>();
                        for (Entity entity: entityList) {
                            dropdownValues.add(entity.getName());
                        }
                        populateValues(config, dropdownValues, targetKey);
                    }
                }
            }

        }
    }
    private  String getEntityParentValue(AutoPopulateConfig config) {
        String parent = "DEFAULT";
        if (config != null) {
            String dimensionName = config.mDimensionName;
            List<String> nameList = StringUtils.getStringListFromDelimiter("##", dimensionName);
            for (String name : nameList
            ) {
                List<String> entityTypeValue = StringUtils.getStringListFromDelimiter("\\$\\$", name);
                String value = getValueForKeyWithTag(entityTypeValue.get(1), config.mTag);
                parent = parent +"##"+ entityTypeValue.get(0)+ "$$"+ value;
            }
        }
        return  parent;
    }
    private void populateValues(AutoPopulateConfig config, List<String> dropdownValues, String key) {
        Collections.sort(dropdownValues);
        String keyValue= getValueForKeyWithTag(key, config.mTag);
        switch (config.mUiType) {
            case "dropdown":
                Spinner spinner = (Spinner) config.mView;
                List<String>  values = new ArrayList<>();
//                if(!dropdownValues.isEmpty()) {
//                    values.add("Select");
//                }
                values.addAll(dropdownValues);

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                        (this, R.layout.form_spinner_item_layout, values);

                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinnerArrayAdapter.notifyDataSetChanged();
                if(keyValue != null && !keyValue.isEmpty()){
                    int indexOfValue = values.indexOf(keyValue);
                    if(indexOfValue >= 0)
                        spinner.setSelection(indexOfValue);
                }


                break;

            default:
                break;
        }
    }

    public void deletedMediaFromDBAndStorage() {
        for (String uuid : deletedMediaUUIDs) {
            FormMedia formImage = mDBHelper.getFormMedia(uuid, mAppId, UAAppContext.getInstance().getUserID());
            if (formImage != null) {
                UAAppContext.getInstance().getDBHelper().deleteFormMedia(formImage.getmUUID());
                File imgFile = new File(formImage.getLocalPath());
                if (imgFile.exists()){
                    String fileName = imgFile.getName().substring(0, imgFile.getName().indexOf("."));
                    if (formImage.getmUUID().equals(fileName)) {
                        Utils.getInstance().deleteImageFromStorage(this, formImage.getLocalPath());
                    }
                }
            }
        }
    }

    public void addToDeletedMediaUUIDs(String uuid){
        if (uuid != null && !uuid.isEmpty()){
            deletedMediaUUIDs.add(uuid);
        }
    }
}
