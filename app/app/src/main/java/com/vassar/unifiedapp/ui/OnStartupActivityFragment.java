package com.vassar.unifiedapp.ui;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ActionForms;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.Entity;
import com.vassar.unifiedapp.model.Form;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.OnStartupAction;
import com.vassar.unifiedapp.model.UserMetaData;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.ServerConstants;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.addAll;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnStartupActivityFragment extends Fragment {

    private LinearLayout mHeaderLinearLayout;
    private LinearLayout mUIElementsLinearLayout;
    private LinearLayout mButtonsLinearLayout;
    private FrameLayout mFormLayout;
    private String user_name;
    private String user_designation;
    private String user_mobile_number;
    private Map<String, String> userDetails = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private ActionForms mOnStartupForm;

    private int mCountForId = 0;

    private Map<String, Spinner> mFormSpinners = new HashMap<>();

    public OnStartupActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_on_startup_activity, container, false);

        getStartUpData();
        initializeViews(view);

        List<OnStartupAction> startupActions = new ArrayList<>();
        Form initialForm = null;
        String metadata = null;

        // TODO : Initialize mOnStartupForm from the AppMDConfig
        AppMetaData appMetaData = UAAppContext.getInstance().getAppMDConfig();
        if (appMetaData != null) {
            if (appMetaData.onStartUp != null && !appMetaData.onStartUp.isEmpty()) {
                startupActions.addAll(appMetaData.onStartUp);
            }
        } else {
            // TODO : Finish activity and move to the home screen
        }

        if (startupActions.size() > 0) {
            for (OnStartupAction onStartupAction : startupActions) {
                if (onStartupAction.getType().equals("ACTIVITY")) {
                    metadata = (String) onStartupAction.getMetadata();
                    break;
                }
            }
        }
//        JsonNode jsonElement = mapper.readTree(metadata);
//        JsonElement jsonElement = gson.toJsonTree(metadata);
        //MyPojo pojo = gson.fromJson(jsonElement, MyPojo.class);
//        ActionForms actionForms = gson.fromJson(jsonElement, ActionForms.class);
        ActionForms actionForms = null;
        try {
            actionForms = mapper.readValue(metadata, ActionForms.class);
        } catch (IOException e) {
            Utils.logError(LogTags.APP_STARTUP, "Failed to parse on startup activity form");
            e.printStackTrace();
        }
        if (actionForms != null) {
            // TODO : Use this form to populate the fields

            String initialId = actionForms.mInitialFormId;

            List<Form> forms = new ArrayList<>();
            forms.addAll(actionForms.mForms);

            for (Form form : forms) {
                if (form.mFormId.equals(initialId)) {
                    initialForm = form;
                    break;
                }
            }

            // TODO : Load form using initialForm
            if (initialForm != null) {

                if (initialForm.mHeaders != null) {
                    // Load headers
                }

                if (initialForm.mFormFields != null) {
                    // Load form fields
                    loadFormFields(inflater, initialForm.mFormFields, mUIElementsLinearLayout);
                }

                if (initialForm.mFormButtons != null) {
                    // Load form buttons
                    loadButtons(initialForm.mFormButtons, mButtonsLinearLayout, initialForm.mFormFields);
                }
            }

        } else {
            // TODO : Finish activity and move to the home screen
        }

        return view;
    }

    private void initializeViews(View view) {
        mHeaderLinearLayout = (LinearLayout) view.findViewById(R.id.dynamic_form_fragment_header_layout);
        mUIElementsLinearLayout = (LinearLayout) view.findViewById(R.id.dynamic_form_fragment_fields_layout);
        mButtonsLinearLayout = (LinearLayout) view.findViewById(R.id.dynamic_form_fragment_buttons_layout);
        mFormLayout = (FrameLayout) view.findViewById(R.id.dynamic_form_fragment_layout);
    }

    /** Renders all the form fields according to their UI type */
    private void loadFormFields(LayoutInflater inflater, ArrayList<FormField> formFields, LinearLayout layout) {

        if (formFields.size() > 0) {

            // Iterating through all the fields from the ProjectTypeConfig
            for (final FormField formField : formFields) {

                String formLabel = formField.mLabel;

                String key = null;
                List<String> keys = Arrays.asList
                        (formField.mIdentifier.trim().split("\\$\\$"));

                if (keys != null) {
                    int lengthOfKeysList = keys.size();
                    if (lengthOfKeysList > 0) {
                        key = keys.get((lengthOfKeysList - 1));
                    }
                }

                // Adding * to the label for a mandatory field
                if (formField.mValidation != null) {
                    if (formField.mValidation.mMandatory) {
                        formLabel += " *";
                    }
                }

                switch (formField.mUiType) {

                    case "label":
                        View labelElement = inflater.inflate(R.layout.form_label_view, layout, false);
                        TextView labelText = labelElement.findViewById(R.id.form_label_text);

                        if (formField.mAligned != null) {
                            switch (formField.mAligned) {
                                case "left":
                                    labelText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                    break;
                                case "center":
                                    labelText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    break;
                                case "right":
                                    labelText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                                    break;
                            }
                        }

                        labelText.setTextSize(20);

                        labelText.setText(formField.mLabel);
                        layout.addView(labelElement);
                        break;

                    case "edittext":
                        String datatype = formField.mDatatype;
                        View editTextElement = inflater.inflate(R.layout.form_edit_text, layout, false);

                        String selection = formField.mSelection;

                        LinearLayout editTextRoot = (LinearLayout) editTextElement.findViewById(R.id.form_edit_text_root);
                        RelativeLayout editTextExpandableLayout = (RelativeLayout) editTextElement.findViewById(R.id.form_edit_text_expandable_layout);
                        RelativeLayout editTextStaticIconLayout = (RelativeLayout) editTextElement.findViewById(R.id.form_edit_text_static_icon_layout);
                        TextView editTextLabel = (TextView) editTextElement.findViewById(R.id.form_edit_text_label);
                        EditText editTextValue = (EditText) editTextElement.findViewById(R.id.form_edit_text_value);
                        ImageView editTextStaticIcon = (ImageView) editTextElement.findViewById(R.id.form_edit_text_static_icon);
                        ImageView editTextExpandableIcon = (ImageView) editTextElement.findViewById(R.id.form_edit_text_expandable_icon);
                        TextView editTextExpandableText = (TextView) editTextElement.findViewById(R.id.form_edit_text_expandable_text);

                        editTextLabel.setText(formLabel);
                        editTextValue.setTag(key);

                        editTextValue.setId(mCountForId);

                        mCountForId++;

                        switch (datatype) {
                            case "int":
                                editTextValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                                break;
                            case "double":
                                editTextValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                break;
                            case "email":
                                editTextValue.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                                break;
                            case "string":
                                editTextValue.setInputType(InputType.TYPE_CLASS_TEXT);
                                break;
                            case "multiplelines":
                                editTextValue.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                                break;
                            case "password":
                                editTextValue.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                break;
                        }

                        String finalKey = key;

                        if (userDetails.containsKey(finalKey)) {
                            editTextValue.setText(userDetails.get(finalKey));
                            addJSONToSubmissionArray(formField, finalKey, userDetails.get(finalKey));
                        }

                        editTextValue.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                                JSONObject formFieldJsonToSave = new JSONObject();
                                try {
                                    formFieldJsonToSave.put("key", finalKey);
                                    formFieldJsonToSave.put("dt", formField.mDatatype);
                                    if (editable.length() > 0) {
                                        formFieldJsonToSave.put("val", editable.toString());
                                    } else if (editable.length() == 0) {
                                        formFieldJsonToSave.put("val", "");
                                    }
                                    formFieldJsonToSave.put("ui", formField.mUiType);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                ((DynamicFormActivity) getActivity()).addToSubmissionArray(finalKey,
                                        formFieldJsonToSave);
                            }
                        });

                        if (selection != null && !selection.isEmpty()) {
                            String phoneNumber = null;
                            switch (selection) {
                                case "autopicked":

                                    editTextValue.setInputType(InputType.TYPE_CLASS_PHONE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                        if (((DynamicFormActivity) getActivity()).checkForReadPhoneStatePermission()) {

                                            List<String> number = new ArrayList<>();
                                            number.addAll(getPhoneNumberForAndroidFiveAndAbove());

                                            if (number.size() == 1) {
                                                editTextValue.setText(number.get(0));
                                                editTextValue.setEnabled(false);

                                            } else if (number.size() == 2) {
                                                showSimSelectionDialog(number, editTextValue);

                                            } else if (number.size() == 0) {

                                                TelephonyManager tMgr = (TelephonyManager) getActivity()
                                                        .getSystemService(Context.TELEPHONY_SERVICE);
                                                phoneNumber = tMgr.getLine1Number();

                                                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                                    // We have obtained a value for the phone number
                                                    // Check if it is a valid phone number
                                                    if (phoneNumber.matches("^([0]|\\+?91)?\\d{10}")) {
                                                        // Valid phone number
                                                        editTextValue.setText(phoneNumber);
                                                        editTextValue.setEnabled(false);
                                                    }
                                                }
                                            }

                                        } else {

                                        }

                                    } else {

                                        TelephonyManager tMgr = (TelephonyManager) getActivity()
                                                .getSystemService(Context.TELEPHONY_SERVICE);
                                        phoneNumber = tMgr.getLine1Number();

                                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                            // We have obtained a value for the phone number
                                            // Check if it is a valid phone number
                                            if (phoneNumber.matches("^([0]|\\+?91)?\\d{10}")) {
                                                // Valid phone number
                                                editTextValue.setText(phoneNumber);
                                                editTextValue.setEnabled(false);
                                            }
                                        }
                                    }
                            }
                        }

                        layout.addView(editTextElement);
                        break;

                    case "dropdown":
                        View spinnerElement = inflater.inflate(R.layout.form_spinner, layout, false);

                        LinearLayout spinnerRoot = (LinearLayout) spinnerElement.findViewById(R.id.form_spinner_root);
                        RelativeLayout spinnerStaticIconLayout = (RelativeLayout) spinnerElement.findViewById(R.id.form_spinner_static_icon_layout);
                        TextView spinnerLabel = (TextView) spinnerElement.findViewById(R.id.form_spinner_label);
                        Spinner spinner = (Spinner) spinnerElement.findViewById(R.id.form_spinner);
                        ImageView spinnerStaticIcon = (ImageView) spinnerElement.findViewById(R.id.form_spinner_static_icon);

                        spinnerLabel.setText(formLabel);
                        spinner.setTag(key);

                        List<Entity> elements = new ArrayList<>();

                        String parent = null;
                        String parentValue = null;
                        if (formField.mParent != null && !formField.mParent.isEmpty()) {
                            parent = formField.mParent;
                        }

                        String entity = null;
                        if (formField.mEntity != null && !formField.mEntity.isEmpty()) {
                            entity = formField.mEntity;
                        }

                        if (parent != null) {
                            // It is a dependent value from another drop down

                            // Check if we have a selected value for this key in the submission array
                            boolean parentFound = ((DynamicFormActivity) getActivity()).presentInSubmissionArray(parent);
                            if (parentFound) {
                                // Value is present, pick value
                                JSONObject parentObject = null;
                                parentObject = ((DynamicFormActivity) getActivity()).getFromSubmissionArray(parent);
                                if (parentObject != null) {
                                    try {
                                        parentValue = parentObject.getString("val");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                // Value is not present
                            }
                        } else {
                            // This value is independent of any other dropdown

                        }
                        // TODO : Get the values from the entity table
                        if (entity != null) {
                            // fetch from entity metadata table
                            String entityName = entity;
                            String parentEntity;
                            if (parent == null) {
                                parentEntity = entity;
                            } else {
                                parentEntity = parentValue;
                            }
                            // getFromDB()
                            UnifiedAppDBHelper dbHelper = UAAppContext.getInstance().getDBHelper();
                            if (parentEntity != null) {
                                    elements.addAll(dbHelper.getEntityList(Constants.SUPER_APP_ID
                                            , null, null, null, parentEntity, entityName));
                            } else {
                            }
                        } else {
                            // get values from form
                        }

                        String source = formField.mSource;
                        String sourceValue = null;
                        if (source != null) {
                            try {
                                sourceValue = getValuesFromSource(source, key);
                            } catch (JSONException e) {
                                sourceValue = null;
                                e.printStackTrace();
                            }
                        }

                        ArrayAdapter<String> spinnerArrayAdapter = null;

                        String values[] = new String[elements.size()];
                        for (int i = 0; i < elements.size(); i++) {
                            values[i] = elements.get(i).getName();
                        }

                        spinnerArrayAdapter = new ArrayAdapter<String>
                                (getActivity(), R.layout.form_spinner_item_layout, values);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spinnerArrayAdapter);

                        String finalKey1 = key;
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                Utils.logInfo("finalKey1 :: " + finalKey1);
                                // Saving value to the submission array
                                if (values[position].length() > 0) {

                                    JSONObject formFieldJsonToSave = new JSONObject();
                                    try {
                                        formFieldJsonToSave.put("key", finalKey1);
                                        formFieldJsonToSave.put("dt", formField.mDatatype);
                                        formFieldJsonToSave.put("val", values[position]);
                                        formFieldJsonToSave.put("ui", formField.mUiType);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    ((DynamicFormActivity) getActivity()).addToSubmissionArray(finalKey1,
                                            formFieldJsonToSave);

                                    FormField fieldToUpdate = null;
                                    // Check if some other drop down needs to be populated
                                    for (FormField formField1 : formFields) {
                                        if (formField1.mParent != null && !formField1.mParent.isEmpty()) {
                                            if (formField1.mParent.equals(finalKey1)) {
                                                // Populate formField1
                                                fieldToUpdate = formField1;
                                                break;
                                            }
                                        }
                                    }

                                    if (fieldToUpdate != null) {
                                        // Update field values for child spinner
                                        String key = null;
                                        List<String> keys = Arrays.asList
                                                (formField.mIdentifier.trim().split("\\$\\$"));

                                        if (keys != null) {
                                            int lengthOfKeysList = keys.size();
                                            if (lengthOfKeysList > 0) {
                                                key = keys.get((lengthOfKeysList - 1));
                                            }
                                        }

                                        if (key != null && !key.isEmpty()) {
                                            if (mFormSpinners.get(key) != null) {

                                                // Get Values from DB
                                                String fieldToUpdateEntity = fieldToUpdate.mEntity;
                                                String fieldToUpdateParent = finalKey1;
                                                String fieldToUpdateEntityParentValue = null;


                                                List<Entity> entities = new ArrayList<>();
                                                if (fieldToUpdateEntity != null && !fieldToUpdateEntity.isEmpty()) {
                                                    try {
                                                        String valueSelected = ((DynamicFormActivity) getActivity())
                                                                .getFromSubmissionArray(finalKey1).getString("val");
                                                        if (formField.mEntity != null) {
                                                            for (Entity element : elements) {
                                                                if (element.getName().equals(valueSelected)) {
                                                                    fieldToUpdateEntityParentValue = element.getId() + Constants.DEFAULT_DELIMITER + formField.mEntity + Constants.DEFAULT_DELIMITER + element.getName();
                                                                }
                                                            }
                                                        } else {
                                                            fieldToUpdateEntityParentValue = valueSelected;
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
//                                                        && fieldToUpdateEntityParentValue != null
//                                                        && !fieldToUpdateEntityParentValue.isEmpty()) {

                                                        entities.addAll(UAAppContext.getInstance()
                                                                .getDBHelper().getEntityList(Constants.SUPER_APP_ID,
                                                                        ServerConstants.DEFAULT_UUID,
                                                                        ServerConstants.DEFAULT_UUID,
                                                                        ServerConstants.ENTITY_METADATA_CONFIG_DEFAULT_USERID,
                                                                        fieldToUpdateEntityParentValue, fieldToUpdateEntity));

                                                }

                                                String[] childDropdownValues = null;
                                                if (entities != null && entities.size() > 0) {
                                                    childDropdownValues = new String[entities.size()];
                                                    int i = 0;
                                                    for (Entity entity1 : entities) {
                                                        childDropdownValues[i] = entity1.getName();
                                                        i++;
                                                    }
                                                }

                                                if (childDropdownValues != null) {
                                                    ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<String>
                                                            (getActivity(), R.layout.form_spinner_item_layout, childDropdownValues);
                                                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    spinner.setAdapter(spinnerArrayAdapter);
                                                    mFormSpinners.get(key).setAdapter(spinnerArrayAdapter);
                                                }

                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // your code here
                            }

                        });

                        if (sourceValue != null) {
                            int position = spinnerArrayAdapter.getPosition(sourceValue);
                            if (position != -1) {
                                spinner.setSelection(position);
                            }
                        }

                        mFormSpinners.put(finalKey1, spinner);

                        layout.addView(spinnerElement);

                        break;

                    default:
                        break;
                }
            }
        }
    }

    /** Load buttons from the form */
    private void loadButtons(List<FormButton> buttons, LinearLayout layout, List<FormField> formFields) {
        if (buttons != null) {
            for (FormButton formButton : buttons) {
                Button button = new Button(getActivity());
                float heightPixels = Utils.getInstance().dpToPx(getActivity(), 48.0f);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout
                        .LayoutParams.WRAP_CONTENT, (int) heightPixels);
                params.weight = 1;
                button.setLayoutParams(params);
                button.setTextSize(17);
                button.setTextColor(getActivity().getResources().getColor(R.color.white));
                button.setBackground(getActivity().getResources()
                        .getDrawable(R.drawable.shape_rectangular_orange_buttons));
                button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                button.setText(formButton.mLabel);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (formButton.mExpandable.mType) {
                            case 15:
                                // Save this data to the Shared Preferences
                                Map<String, JSONObject> data = ((DynamicFormActivity) getActivity()).getSubmissionArray();
                                JSONArray jsonArray = new JSONArray();
                                for (String key : data.keySet()) {
                                    JSONObject valueObject = data.get(key);
                                    jsonArray.put(valueObject);
                                }

                                JSONObject submissionObject = new JSONObject();
                                try {
                                    submissionObject.put(Constants.ON_STARTUP_DATA_ARRAY, jsonArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                boolean isValid = validateSubmissionData(formFields);

                                if (isValid) {
                                    SharedPreferences sharedPreferences = getActivity()
                                            .getSharedPreferences(Constants.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(Constants.ON_STARTUP_DATA_EXISTS, true);
                                    editor.putString(Constants.ON_STARTUP_DATA, submissionObject.toString());
                                    editor.apply();

                                    if (((DynamicFormActivity) getActivity()).moveToHome) {
                                        moveToHomeScreen();
                                    }
                                    // TODO : finish only when not called after login,(should be in else part)
                                    ((DynamicFormActivity) getActivity()).finish();


                                } else {
                                    Toast.makeText(getActivity(), "Enter all the fields.", Toast.LENGTH_LONG).show();
                                }

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

    private String getValuesFromSource(String source, String key) throws JSONException {
        switch (source) {
            case "USERMETADATA":
                String userId = UAAppContext.getInstance().getUserID();
                UserMetaData userMetaData = UAAppContext.getInstance().getDBHelper().getUserMeta(userId);
                String details = userMetaData.userDetails;
                JSONObject jsonObject = new JSONObject(details);
                String value = jsonObject.getString(key);
                return value;
        }
        return null;
    }

    protected void moveToHomeScreen() {
        Intent intentHome = new Intent(getActivity(), HomeActivity.class);
        startActivity(intentHome);
    }

    private List<String> getPhoneNumberForAndroidFiveAndAbove() {
        List<String> phoneNumbers = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 11);
            }
            List<SubscriptionInfo> subscription = SubscriptionManager
                    .from(getActivity().getApplicationContext()).getActiveSubscriptionInfoList();

            if (subscription != null && subscription.size() > 0) {

                for (SubscriptionInfo info : subscription) {
                    String number = info.getNumber();
                    if (number != null && !number.isEmpty()) {
                        if (number.matches("^([0]|\\+?91)?\\d{10}")) {
                            phoneNumbers.add(info.getNumber());
                        }
                    }
                }
            }
        }
        return phoneNumbers;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode  == 11){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getPhoneNumberForAndroidFiveAndAbove();
            }
        }
    }

    private boolean validateSubmissionData(List<FormField> formFields) {

        boolean isValid = true;

        for (FormField formField : formFields) {
            if (formField.mValidation != null && formField.mValidation.mMandatory) {
                String key = null;
                List<String> keys = Arrays.asList
                        (formField.mIdentifier.trim().split("\\$\\$"));

                if (keys != null) {
                    int lengthOfKeysList = keys.size();
                    if (lengthOfKeysList > 0) {
                        key = keys.get((lengthOfKeysList - 1));
                    }
                }
                JSONObject jsonObject = ((DynamicFormActivity) getActivity()).getFromSubmissionArray(key);
                if (jsonObject == null) {
                    isValid = false;
                    break;
                } else {
                        try {
                            String val = jsonObject.getString("val");
                            if (val == null || val.isEmpty()) {
                                isValid = false;
                                break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
        return isValid;
    }

    private void showSimSelectionDialog(List<String> phoneNumbers, EditText editText) {
        // create a Dialog component
        final Dialog dialog = new Dialog((DynamicFormActivity)getActivity());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final String[] chosenNumber = {""};

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.sim_selection_dialog);

        RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.sim_selection_radio_group);

        RadioButton radioButton1 = (RadioButton) dialog.findViewById(R.id.sim1Button);
        radioButton1.setText(phoneNumbers.get(0));

        RadioButton radioButton2 = (RadioButton) dialog.findViewById(R.id.sim2Button);
        radioButton2.setText(phoneNumbers.get(1));

        Button submitNumber = (Button) dialog.findViewById(R.id.submit_number);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (R.id.sim1Button == checkedId) {
                    chosenNumber[0] = radioButton1.getText().toString();

                } else if (R.id.sim2Button == checkedId){
                    chosenNumber[0] = radioButton2.getText().toString();
                }
            }
        });

        submitNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(chosenNumber[0]);
                editText.setEnabled(false);
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void getStartUpData () {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        boolean startUpDataExists = sharedPreferences.getBoolean(Constants.ON_STARTUP_DATA_EXISTS, false);

        if (startUpDataExists) {
            String startUpData = sharedPreferences.getString(Constants.ON_STARTUP_DATA, "");
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(startUpData);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.ON_STARTUP_DATA_ARRAY);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);
                    String keyValue = object.getString("key");

                    if (!userDetails.containsKey(keyValue)) {
                        userDetails.put(keyValue, object.getString("val"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addJSONToSubmissionArray(FormField formField, String finalKey, String val) {
        JSONObject formFieldJsonToSave = new JSONObject();
        try {
            formFieldJsonToSave.put("key", finalKey);
            formFieldJsonToSave.put("dt", formField.mDatatype);
            formFieldJsonToSave.put("val", val);
            formFieldJsonToSave.put("ui", formField.mUiType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((DynamicFormActivity) getActivity()).addToSubmissionArray(finalKey,
                formFieldJsonToSave);
    }
}