package com.vassar.unifiedapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.model.ActionForms;
import com.vassar.unifiedapp.model.Form;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterScreen extends BaseActivity {

    private RelativeLayout mRootLayout;
    private TextView mNoFilterText;

    private String mAppId;
    private String mUserId;
    private String mGroupingAttributeValue;

    private ProgressBar mProgressBar;

    private LinearLayout mFilteringFieldsLayout;
    private LinearLayout mFilteringButtonsLayout;

    private Map<String, FormField> mKeyToFormFieldMap = new HashMap<>();
    private  Map<String, Object> mKeyToNextObjectMap = new HashMap<>();
    private Map<String, Object> mKeyToFieldObjectMap = new HashMap<>();
    private Map<String, String> mKeyToSelectedValuesMap = new HashMap<>();
    private  ActionForms mFilteringForm = null;
    private List<String> attributes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_screen);

        mProgressBar = (ProgressBar) findViewById(R.id.filter_progress);

        initComponents(this);

        initializeToolbar();

        hideActionBar(this);

        initializeViews();

        if (savedInstanceState == null) {
            mAppId = getIntent().getStringExtra(Constants.FILTER_APP_ID);
            mUserId = getIntent().getStringExtra(Constants.FILTER_USER_ID);
            mGroupingAttributeValue = getIntent().getStringExtra(Constants.PROJECT_GROUPING_ATTRIBUTE);
            ObjectMapper mapper =  new ObjectMapper();
            String keyToValueMapString =   getIntent().getStringExtra(Constants.SELECTED_FILTER_KEY_VALUE_MAP);
            if(keyToValueMapString!=null && !keyToValueMapString.isEmpty()) {
                try {
                    mKeyToSelectedValuesMap = mapper.readValue(keyToValueMapString, new TypeReference<Map<String, String>>() {});
                }   catch ( IOException e) {
                    e.printStackTrace();
                }
            }

            initializeConfigs();
        } else {
            // Loading values from the saved state
            mAppId = savedInstanceState.getString(Constants.FILTER_APP_ID);
            mUserId = savedInstanceState.getString(Constants.FILTER_USER_ID);
            mGroupingAttributeValue = savedInstanceState.getString(Constants.PROJECT_GROUPING_ATTRIBUTE);
        }
    }

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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(Constants.FILTER_APP_ID, mAppId);
        outState.putString(Constants.FILTER_USER_ID, mUserId);
        outState.putString(Constants.PROJECT_GROUPING_ATTRIBUTE, mGroupingAttributeValue);
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.filter_toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
    }

    private void initializeViews() {
        mRootLayout = (RelativeLayout) findViewById(R.id.filter_activity_root);
        mNoFilterText = (TextView) findViewById(R.id.filter_screen_not_available_text);

        mFilteringFieldsLayout = (LinearLayout) findViewById(R.id.filter_form_fields_layout);
        mFilteringButtonsLayout = (LinearLayout) findViewById(R.id.filter_form_buttons_layout);
    }

    private void  noFilterAvailable() {
        mRootLayout.setVisibility(View.GONE);
        mNoFilterText.setVisibility(View.VISIBLE);
    }

    private void initializeConfigs() {

        RootConfig rootConfig = UAAppContext.getInstance().getRootConfig();

        Form initialForm = null;
        List<String> attributesList = new ArrayList<>();

        if (rootConfig != null && mAppId != null && !mAppId.isEmpty()) {

            if (rootConfig.mApplications != null && !rootConfig.mApplications.isEmpty()) {

                // We have a list of applications available
                for (ProjectTypeModel projectTypeModel : rootConfig.mApplications) {
                    if (projectTypeModel.mAppId.equalsIgnoreCase(mAppId)) {
                        // Retrieving filtering form for mAppId
                        attributesList = projectTypeModel.mFilteringAttributes;
                        attributes = projectTypeModel.mFilteringAttributes;
                        mFilteringForm = projectTypeModel.mFilteringForm;
                        break;
                    }
                }

                if (mFilteringForm != null) {

                   String initialId = mFilteringForm.mInitialFormId;

                    if (initialId != null && !initialId.isEmpty() && mFilteringForm.mForms != null && !mFilteringForm.mForms.isEmpty()) {

                        for (Form form : mFilteringForm.mForms) {
                            if (form.mFormId.equalsIgnoreCase(initialId)) {
                                initialForm = form;
                                break;
                            }
                        }
//                        Map<String, String> keyToSelectedValuesMap = new HashMap<>();
			            Map<String,String> keyToSelectedValuesMap = mKeyToSelectedValuesMap ==  null ? new HashMap<>() : mKeyToSelectedValuesMap;
                        if (initialForm != null) {
                            if (initialForm.mFormFields != null && !initialForm.mFormFields.isEmpty()) {
                                keyToSelectedValuesMap = renderFormFields(initialForm.mFormFields, mFilteringFieldsLayout, attributesList);
                            }

                            if (initialForm.mFormButtons != null && !initialForm.mFormButtons.isEmpty()) {
                                renderFormButtons(initialForm.mFormButtons, mFilteringButtonsLayout, attributesList, keyToSelectedValuesMap);
                            }
                        }

                    } else {
                        Utils.logError("FILTER_FORM_ERROR", "Problem loading form- Initial form Id is not present");
                    }
                } else {
                    Utils.logError("FILTER_FORM_ERROR", "Filtering form not present");
                }
            } else {
                Utils.logError("FILTER_FORM_ERROR", "Problem with root config file-Invalid applications");
            }
        } else {
            Utils.logError("FILTER_FORM_ERROR", "Root configuration not present");
        }
    }

    private void renderFormButtons(ArrayList<FormButton> buttons, LinearLayout layout, List<String> attributesList, Map<String, String> keyToSelectedValuesMap) {
        if (buttons != null) {
            for (FormButton formButton : buttons) {
                Button button = new Button(this);
                float heightPixels = Utils.getInstance().dpToPx(this, 48.0f);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                        .LayoutParams.WRAP_CONTENT, (int) heightPixels);
                params.weight = 1;
                button.setLayoutParams(params);
                button.setTextSize(17);
                button.setTextColor(getResources().getColor(R.color.white));
                button.setBackground(getResources()
                        .getDrawable(R.drawable.shape_rectangular_orange_buttons));
                button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                button.setText(formButton.mLabel);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (formButton.mExpandable.mType) {
                            case 15:
                                onSubmitFilters(view, attributesList, keyToSelectedValuesMap);
                                break;

                            default:
                                break;
                        }
                    }
                });
                layout.addView(button);
            }
        }
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

    private Map<String, String> renderFormFields(ArrayList<FormField> formFields, LinearLayout layout, List<String> attributesList) {

//        Map<String, String> keyToSelectedValue = new HashMap<>();
        Map<String, String> keyToSelectedValue = mKeyToSelectedValuesMap == null ? new HashMap<>() :mKeyToSelectedValuesMap;

        String previousKey = null;

        if (formFields.size() > 0) {

            int viewCounter = 0;
            // Iterating through all the fields from the ProjectTypeConfig
            for (FormField formField : formFields) {
                int i= 0;
                for (String key :attributes
                ) {
                    if(key.equalsIgnoreCase(formField.mIdentifier)){
                        viewCounter = i;
                        break;
                    }i++;
                }
                String formLabel = formField.mLabel;

                String key1 = getKeyName(formField.mIdentifier);
                final String key = key1;

                switch (formField.mUiType) {

                    case "dropdown":
                        View spinnerElement = mInflater.inflate(R.layout.form_spinner, layout, false);

                        LinearLayout spinnerRoot = (LinearLayout) spinnerElement.findViewById(R.id.form_spinner_root);
                        RelativeLayout spinnerStaticIconLayout = (RelativeLayout) spinnerElement.findViewById(R.id.form_spinner_static_icon_layout);
                        //TextView spinnerLabel = (TextView) spinnerElement.findViewById(R.id.form_spinner_label);
                        Spinner spinner = (Spinner) spinnerElement.findViewById(R.id.form_spinner);
                        ImageView spinnerStaticIcon = (ImageView) spinnerElement.findViewById(R.id.form_spinner_static_icon);

                        //spinnerLabel.setText(formLabel);

                        spinner.setTag(viewCounter);

                        String values[] = new String[1];
                        values[0] = StringUtils.getTranslatedString(formLabel);
                        String translatedLabel = StringUtils.getTranslatedString(formLabel);
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                                (this, R.layout.form_spinner_item_layout, values);

                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spinnerArrayAdapter);
                        spinnerArrayAdapter.notifyDataSetChanged();

                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Spinner spinner = (Spinner) mKeyToFieldObjectMap.get(formField.mIdentifier);
                                int index = Integer.parseInt(String.valueOf(spinner.getTag()));

                                // Each spinner contains Select keyName in the values
                                if(spinner.getAdapter().getCount() > 1) {
                                    String value = String.valueOf(spinner.getAdapter().getItem(position));
                                    if (value != null && !value.contains("Select") && !value.equalsIgnoreCase(translatedLabel) ) {
                                        keyToSelectedValue.put(formField.mIdentifier, value);
                                        Spinner nextSpinner = (Spinner) mKeyToNextObjectMap.get(formField.mIdentifier);
                                        if (nextSpinner != null) {
                                            List<String> values = new ArrayList<>();
                                            values.add(String.valueOf(nextSpinner.getAdapter().getItem(0)));
                                            index = Integer.parseInt(String.valueOf(nextSpinner.getTag()));
                                            checkForFilteringKeyCondition(attributesList.get(index), keyToSelectedValue, index, nextSpinner,values);
                                        }
                                    }
                                } else {
//                                if (formField.mFilteringKey == null)
                                    List<String> values = new ArrayList<>();
                                    values.add(StringUtils.getTranslatedString(formLabel));
                                    populateDropDownValues("", index, mUserId, mAppId, formField, values,  spinner);
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });

                        layout.addView(spinnerElement);
                        addFormFieldToMap(formField.mIdentifier, formField);
                        addFormFieldToObjectMap(formField.mIdentifier, spinner, previousKey);
                        previousKey = formField.mIdentifier;
                        break;

                    default:
                        break;
                }
//                viewCounter++;
            }
        }
        return keyToSelectedValue;
    }

    private void checkForFilteringKeyCondition(String key, Map<String, String> keyToSelectedValue, int index, Object nextObject, List<String> values) {
        if(key == null) return;
        FormField formField = mKeyToFormFieldMap.get(key);
//        String queryFilter = "";
//        if(formField.mFilteringKey != null) {
//            List<String> filteringKeys = Arrays.asList(formField.mFilteringKey.split("#"));
//            for(String filterVal : filteringKeys) {
//                if(keyToSelectedValue.containsKey(filterVal)) {
//                    if(queryFilter == null) queryFilter = "";
//                    queryFilter += keyToSelectedValue.get(filterVal) + "#";
//                }
//            }
//            if(queryFilter != null && !queryFilter.isEmpty()) {
//                queryFilter = queryFilter.toLowerCase();
//                queryFilter = queryFilter.substring(0, queryFilter.lastIndexOf("#"));
//            }
//        }
//        populateDropDownValues(queryFilter, index, mUserId, mAppId, formField, values, nextObject);
        String query = "";
        if(formField.mFilteringKey != null) {
            List<String> filteringKeys = Arrays.asList(formField.mFilteringKey.split("#"));
            Map<String, String> keyToValueForFilterQuery= new HashMap<>();
            for(String filterVal : filteringKeys) {
                if(keyToSelectedValue.containsKey(filterVal)) {
                    keyToValueForFilterQuery.put(filterVal, keyToSelectedValue.get(filterVal));
                }
            }

            for(String a : attributes) {
                if(keyToValueForFilterQuery.get(a) != null && !keyToValueForFilterQuery.get(a).isEmpty()) {
                    query = query + keyToSelectedValue.get(a) + "#";
                } else {
                    query = query+ "%#";
                }
            }
            if(query != null && !query.isEmpty()) {
                query = query.toLowerCase();
                query = query.substring(0, query.lastIndexOf("#"));
            }
        }
        populateDropDownValues(query, index, mUserId, mAppId, formField, values, nextObject);
    }

    private void populateDropDownValues(String queryFilter, int index, String mUserId, String mAppId, FormField formField, List<String> values, Object nextObject) {

        Set<String> queryResult = mDBHelper.getDimensionValues(queryFilter, index, mUserId, mAppId, mGroupingAttributeValue);
        List<String> dropdownValues = new ArrayList<>(queryResult);
        Collections.sort(dropdownValues);

        String keyValue= mKeyToSelectedValuesMap.get(formField.mIdentifier) ;
        switch (formField.mUiType) {
            case "dropdown":
                    Spinner spinner = (Spinner) nextObject;
                    if(values == null) values = new ArrayList<>();
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

    private void addFormFieldToMap(String key, FormField field) {
        if (mKeyToFormFieldMap == null) {
            mKeyToFormFieldMap = new HashMap<>();
        }
        mKeyToFormFieldMap.put(key, field);
    }

    private void addFormFieldToObjectMap(String key, Object object, String previousKey) {
        if (mKeyToFieldObjectMap == null) {
            mKeyToFieldObjectMap = new HashMap<>();
        }
        if(mKeyToNextObjectMap == null) {
            mKeyToNextObjectMap = new HashMap<>();
        }
        mKeyToFieldObjectMap.put(key, object);
        if(previousKey != null)
            mKeyToNextObjectMap.put(previousKey, object);
    }

    public void onSubmitFilters(View view, List<String> attributesList, Map<String, String> keyToSelectedValuesMap) {
        // Submit Filters
        Intent data = new Intent();
        int index = 0;

        if (keyToSelectedValuesMap == null || keyToSelectedValuesMap.isEmpty()){
            setResult(RESULT_CANCELED, data);
        } else
            if (attributesList != null && !attributesList.isEmpty()) {
            for (String attribute : attributesList) {
                if (keyToSelectedValuesMap.containsKey(attribute)) {
                    String selected = keyToSelectedValuesMap.get(attribute);
                    data.putExtra(attribute, selected + "$$" + index++);
                }
            }
            setResult(RESULT_OK, data);
        }
        finish();
    }
}
