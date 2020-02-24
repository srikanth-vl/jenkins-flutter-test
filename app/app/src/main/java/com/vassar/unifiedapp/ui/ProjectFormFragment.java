package com.vassar.unifiedapp.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.ProjectSubmissionThread;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.asynctask.DownloadImageIfFailedTask;
import com.vassar.unifiedapp.asynctask.TransactionLogAsyncTask;
import com.vassar.unifiedapp.camera.LollipopVideoCaptureActivity;
import com.vassar.unifiedapp.camera.VideoCaptureActivity;
import com.vassar.unifiedapp.camera2.NewCamera2Activity;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.db.UnifiedAppDbContract;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AutoPopulateConfig;
import com.vassar.unifiedapp.model.BridgeValue;
import com.vassar.unifiedapp.model.ClientValidationResponse;
import com.vassar.unifiedapp.model.ExpandableComponent;
import com.vassar.unifiedapp.model.Form;
import com.vassar.unifiedapp.model.FormBridge;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.GpsValidation;
import com.vassar.unifiedapp.model.Header;
import com.vassar.unifiedapp.model.IncomingImage;
import com.vassar.unifiedapp.model.LatestFieldValue;
import com.vassar.unifiedapp.model.MultipleValues;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectListFieldModel;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.SelectableDates;
import com.vassar.unifiedapp.newcamera.CameraActivity;
import com.vassar.unifiedapp.offlinemaps.Destination;
import com.vassar.unifiedapp.offlinemaps.MapHelper;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.DBObjectCreationUtils;
import com.vassar.unifiedapp.utils.MapUtils;
import com.vassar.unifiedapp.utils.MediaType;
import com.vassar.unifiedapp.utils.PropertyReader;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;
import com.vassar.unifiedapp.validation.SPELExpressionValidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectFormFragment extends Fragment {

    public static final int DIALOG_LOADING = 1;
    public LinearLayout mCurrentImageLinearLayout;
    public LinearLayout mCurrentVideoLinearLayout;
    Map<String, LatestFieldValue> mLatestLastFieldValuesMapSubmittedByUser = new HashMap<>();
    private LayoutInflater mInflater;
    private Form mForm;
    private Project mProject;
    private LinearLayout mHeaderLinearLayout;
    private LinearLayout mUIElementsLinearLayout;
    private LinearLayout mButtonsLinearLayout;
    private LinearLayout mFormBridgesLinearLayout;
    private FrameLayout mFormLayout;
    private TextView mCurrentGeotagTextviewDisplayed;
    private TextView mCurrentGeotagTextview;
    private String mTag;
    private TextView mCurrentImageUuids;
    private TextView mCurrentVideoUuids;
    private TextView mCurrentTimePicker;
    private Context context;

    private int mCountForId = 0;
    private HashMap<String, String> mSuperFieldMapForValidation;
    private HashMap<String, JSONObject> mSubmittedFieldsForValidation;
    private DBObjectCreationUtils mDBObjectCreationUtils;
    private boolean formEditable = true;
    private boolean displayCreatedProjectData = false;

    public ProjectFormFragment() {
        // Required empty public constructor
        this.mDBObjectCreationUtils = new DBObjectCreationUtils();
    }

    @SuppressLint("ValidFragment")
    public ProjectFormFragment(LayoutInflater inflater, Form form, Project project, String tag, Map<String, LatestFieldValue> latestLastFieldValuesMapSubmittedByUser) {
        this.mInflater = inflater;
        this.mForm = form;
        this.mProject = project;
        this.mTag = tag;
        this.mLatestLastFieldValuesMapSubmittedByUser = latestLastFieldValuesMapSubmittedByUser;
        this.mDBObjectCreationUtils = new DBObjectCreationUtils();
        if(project.mState != null && !project.mState.isEmpty() && project.mState.equalsIgnoreCase("New") ) {
            formEditable = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Utils.getInstance().showLog("FORM FRAGMENT ", "ON CREATE CALLED");

        context = getContext();
//        ((ProjectFormActivity) getActivity()).printSubmittedFields();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_project_form, container, false);
        initializeViews(view);
        if (mForm != null) {
            if (mForm.mFormBridge != null) {
                mFormLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            } else {
                mFormLayout.setBackground(getResources().getDrawable(R.drawable.shape_form_background));
            }

            if (mForm != null && mForm.mName != null && !mForm.mName.isEmpty()){
                renderFormName(mForm.mName, mHeaderLinearLayout);
            }

            if (mForm.mHeaders != null) {
                mHeaderLinearLayout.removeAllViews();
                loadHeaders(mForm.mHeaders, mHeaderLinearLayout);
            }
            displayCreatedProjectData = ((ProjectFormActivity) getActivity()).displayCreatedProjectData;
            if(displayCreatedProjectData) {
                return view;
            }
            if (mForm.mFormBridge != null) {
                mFormBridgesLinearLayout.removeAllViews();
                loadBridges(mForm.mFormBridge, mFormBridgesLinearLayout);
            } else {
                if (mForm.mFormFields != null) {
                    mUIElementsLinearLayout.removeAllViews();
                    loadFormFields(mForm.mFormFields, mUIElementsLinearLayout, mTag);
                    ((ProjectFormActivity) getActivity()).printSubmittedFields();
                }

                if (mForm.mFormButtons != null) {
                    mButtonsLinearLayout.removeAllViews();
                    loadButtons(mForm.mFormButtons, mButtonsLinearLayout);
                }
            }
        }

        return view;
    }

    /**
     * We use three different linear layouts for different components of the form (headers,
     * UI elements, and buttons.
     *
     * @param view Layout inflated in onCreateView()
     */
    private void initializeViews(View view) {
        mHeaderLinearLayout = (LinearLayout) view.findViewById(R.id.activity_project_form_header_layout);
        mUIElementsLinearLayout = (LinearLayout) view.findViewById(R.id.activity_project_form_fields_layout);
        mButtonsLinearLayout = (LinearLayout) view.findViewById(R.id.activity_project_form_buttons_layout);
        mFormBridgesLinearLayout = (LinearLayout) view.findViewById(R.id.activity_project_form_bridges_layout);
        mFormLayout = (FrameLayout) view.findViewById(R.id.activity_project_form_layout);
    }


    private void renderFormName(String mFormName, LinearLayout layout) {

        TextView formNameView = new TextView(getActivity());
        formNameView.setText(mFormName);
        formNameView.setTextSize(22);
        formNameView.setTextColor(Color.WHITE);
        formNameView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        formNameView.setTypeface(null, Typeface.BOLD);
        layout.addView(formNameView);
    }



    /**
     * The headers labels are picked up from mForm that is part of the ProjectType configuration,
     * and the values for the headers are picked from mProject that is from the ProjectList configuration
     * @param headers
     * @param layout
     */
    private void loadHeaders(ArrayList<Header> headers, LinearLayout layout) {

        List<ProjectListFieldModel> projectListHeaders = mProject.mFields;
        if(projectListHeaders == null) {
            projectListHeaders =  new ArrayList<>();
        }
        Map<String, String> keyToValueMapForHeaders = getKeyToValueMapForHeaders(mProject.mFields, mLatestLastFieldValuesMapSubmittedByUser);
        if (headers == null) {
            layout.setVisibility(View.GONE);
            return;
        }
        try {
            Map<String, JSONObject> keyToJSONObject = new HashMap<>();
            for (ProjectListFieldModel projectListHeader : projectListHeaders) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", getKeyName(projectListHeader.mIdentifier));
                jsonObject.put("dt", "");
                jsonObject.put("ui", "");
                if (projectListHeader.mProjectListFieldValue != null && projectListHeader.mProjectListFieldValue.mValue != null)
                    jsonObject.put("val", projectListHeader.mProjectListFieldValue.mValue);
                else
                    jsonObject.put("val", "");
                keyToJSONObject.put(projectListHeader.mIdentifier, jsonObject);
            }
            if(projectListHeaders == null || projectListHeaders.isEmpty()){
                for (String key : keyToValueMapForHeaders.keySet()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("key", key);
                    jsonObject.put("dt", "");
                    jsonObject.put("ui", "");
                    if (keyToValueMapForHeaders.get(key) != null )
                        jsonObject.put("val", keyToValueMapForHeaders.get(key));
                    else
                        jsonObject.put("val", "");
                    keyToJSONObject.put(key, jsonObject);
                }
            }
            if (((ProjectFormActivity) getActivity()).mFormBridgeKey != null) {
                String bridgeSelectedValue = ((ProjectFormActivity) getActivity()).mFormBridgeValue;
                if (bridgeSelectedValue != null && !bridgeSelectedValue.isEmpty()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("key", ((ProjectFormActivity) getActivity()).mFormBridgeKey);
                    jsonObject.put("val", bridgeSelectedValue);
                    jsonObject.put("ui", "");
                    jsonObject.put("dt", "");
                    keyToJSONObject.put(((ProjectFormActivity) getActivity()).mFormBridgeKey, jsonObject);
                }
            }
            JSONArray submissionArray = ((ProjectFormActivity) getActivity()).getSubmissionArray();
            List<String> keysInSubmissionArray = new ArrayList<>();
            for (int i = 0; i < submissionArray.length(); i++) {
                JSONObject submissionObject = submissionArray.getJSONObject(i);
                String key = submissionObject.getString("key");
//                submissionObject.put("key", getKeyName(key));
                keyToJSONObject.put(key, submissionObject);
                keysInSubmissionArray.add(key);
            }

            ((ProjectFormActivity) getActivity()).clearSubmissionArray();

            for (Header header : headers) {
                if (keyToJSONObject.containsKey(header.mIdentifier)) {
                    JSONObject submissionObject = keyToJSONObject.get(header.mIdentifier);
                    String value = submissionObject.getString("val");
                    String uiType = submissionObject.getString("ui");
                    View view = null;
                    if (value != null && !value.isEmpty()) {
                        if (uiType == null || uiType.isEmpty()) {
                            view = loadHeaderView(layout, header, null, keyToValueMapForHeaders.get(header.mIdentifier));
                            if ((header.mDisplay && !header.mSubmittable) ||
                                    (header.mDisplay && header.mSubmittable &&
                                            keysInSubmissionArray.contains(header.mIdentifier))) {
                                layout.addView(view);
                            }
                            if ((header.mSubmittable && !header.mDisplay) || (header.mDisplay && header.mSubmittable &&
                                    keysInSubmissionArray.contains(header.mIdentifier))) {
                                ((ProjectFormActivity) getActivity()).addToSubmissionArray(submissionObject);
                            }
                            continue;
                        }
                        if (uiType.equals("timepicker")) {
                            List<String> savedTime = Arrays.asList(value.split(":"));
                            int labelHours = Integer.parseInt(savedTime.get(0));
                            int labelMins = Integer.parseInt(savedTime.get(1));

                            long valueInLong = (labelHours * 3600) + (labelMins * 60);

                            String timeInFormat = Utils.getInstance()
                                    .changeTimeToHhMmFormat(valueInLong);
                            view = loadHeaderView(layout, header, null, timeInFormat);
                        } else if (uiType.equals("edittext") || uiType.equals("date") || uiType.equals("radio") || uiType.equals("textbox") || uiType.equals("dropdown")
                                || uiType.equals("checkbox") || uiType.equals("geotag") || uiType.equals("toggle")) {
                            view = loadHeaderView(layout, header, null, value);
                        } else if (uiType.equals("image") || uiType.equals("geotagimage") || uiType.equals("geotagimagefused")) {
                            view = loadHeaderView(layout, header, MediaType.IMAGE.name(), value);
                        } else if (uiType.equals("video")) {
                            view = loadHeaderView(layout, header, MediaType.VIDEO.name(), value);
                        }
                    }
                    if (view != null && header.mDisplay && keysInSubmissionArray.contains(header.mIdentifier)) {
                        layout.addView(view);
                    }
                    if (header.mSubmittable ) {
                        ((ProjectFormActivity) getActivity()).addToSubmissionArray(submissionObject);
                    }
                }
            }
        } catch (JSONException e) {
            Utils.logError("JSON_Exception", "Got Json Exception while parsing form");
        }
    }

    /**
     * Creates a view that is added to the header layout
     */
    private View loadHeaderView(LinearLayout layout, Header header, String mediaType, String valueText) {

        View view = mInflater.inflate(R.layout.form_header_element, layout, false);

        RelativeLayout staticIconLayout = (RelativeLayout) view.findViewById(R.id.form_header_static_icon_layout);
        ImageView staticIcon = (ImageView) view.findViewById(R.id.form_header_static_icon);
        TextView label = (TextView) view.findViewById(R.id.form_header_label);
        TextView value = (TextView) view.findViewById(R.id.form_header_value);
        HorizontalScrollView imageScrollView = (HorizontalScrollView) view
                .findViewById(R.id.form_header_image_group_scroll_view);
        LinearLayout imageGroupLayout = (LinearLayout) view.findViewById(R.id.form_header_image_group);

        RelativeLayout headerLabelLayout = (RelativeLayout) view.findViewById(R.id.form_header_label_layout);
        RelativeLayout headerValueLayout = (RelativeLayout) view.findViewById(R.id.form_header_value_layout);

        String labelText = header.mValue;
        String translatedLabel = getLabel(labelText);
        String originalLabel = labelText;
        labelText = translatedLabel;
        label.setText(labelText);
        if (originalLabel.equals("Name") || originalLabel.equals("Tank Name"))
            headerLabelLayout.setVisibility(View.GONE);

        if (mediaType == null) {
            value.setText(valueText);
        } else {
            if (mediaType.equalsIgnoreCase(MediaType.IMAGE.name())) {
                value.setVisibility(View.GONE);
                imageScrollView.setVisibility(View.VISIBLE);
                loadMedia(valueText, imageGroupLayout, mediaType);
            } else if (mediaType.equalsIgnoreCase(MediaType.VIDEO.name())) {
                value.setVisibility(View.GONE);
                imageScrollView.setVisibility(View.VISIBLE);
                loadMedia(valueText, imageGroupLayout, mediaType);
            } else {
                value.setText(valueText);
            }
        }
        return view;
    }

    private void loadMedia(String value, LinearLayout layout, String mediaType) {
        List<String> UUIDListWithGeotag = Arrays.asList(value.split("\\s*,\\s*"));
        List<String> UUIDList;
        if (mediaType.equalsIgnoreCase(MediaType.IMAGE.name())) {
            UUIDList = Utils.getImageUUIDList(UUIDListWithGeotag);
        } else {
            UUIDList = UUIDListWithGeotag;
        }
        if (!UUIDList.isEmpty()) {
            for (String uuid : UUIDList) {
                FormMedia formMedia = UAAppContext.getInstance().getDBHelper().getFormMedia(uuid, ((ProjectFormActivity) getActivity()).mAppId, UAAppContext.getInstance().getUserID());
                if (formMedia != null) {
                    ImageView imageView = new ImageView(getActivity());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            (int) getResources().getDimension(R.dimen.dimen_60dp)
                            , ViewGroup.LayoutParams.MATCH_PARENT);
                    params.setMargins(8, 0, 8, 0);
                    imageView.setLayoutParams(params);

                    if (mediaType.equalsIgnoreCase(MediaType.IMAGE.name())) {
                        File imgFile = new File(formMedia.getLocalPath());
                        if (imgFile != null) {
                            Picasso.get().load(imgFile)
                                    .resize(150, 150)
                                    .placeholder(R.drawable.placeholder)
                                    .into(imageView);
                        }

                        imageView.setAdjustViewBounds(true);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Opening activity that shows the image
                                Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
                                intent.putExtra("image_path", formMedia.getLocalPath());
                                startActivity(intent);
                            }
                        });
                        layout.addView(imageView, 0);
                    } else if (mediaType.equalsIgnoreCase(MediaType.VIDEO.name())) {
                        imageView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play_video));
                        imageView.setPadding(16, 16, 16, 16);
                        imageView.setAdjustViewBounds(true);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Opening activity that shows the image
                                Intent intent = new Intent(getActivity(), VideoPreviewActivity.class);
                                intent.putExtra("video_path", formMedia.getLocalPath());
                                startActivity(intent);
                            }
                        });
                        layout.addView(imageView, 0);
                    }
                }
            }
        }
    }

    /**
     * Renders all the form fields according to their UI type
     * @param formFields
     * @param layout
     * @param tag
     */
    private void loadFormFields(ArrayList<FormField> formFields, LinearLayout layout, String tag) {

        if (formFields == null || formFields.isEmpty()) {
            return;
        }
        // List of project fields from the ProjectListConfig
        ArrayList<ProjectListFieldModel> projectFields = mProject.mFields != null ? mProject.mFields : new ArrayList<>();

        // Iterating through all the fields from the ProjectTypeConfig
        for (FormField formField : formFields) {

            ProjectListFieldModel projectModel = null;
            String formLabel = formField.mLabel;
            String translatedLabel = getLabel(formLabel);
            String originalLabel = formLabel;
            formLabel = translatedLabel;
            JSONObject savedObject = ((ProjectFormActivity) getActivity())
                    .getSubmittedField(tag + "#" + formField.mIdentifier);

            Utils.logInfo("LOADFORMFIELDSJSON", tag + "#" + formField.mIdentifier);

            // Adding the UOM to the label
            if (formField.mUOM != null && !formField.mUOM.isEmpty())
                formLabel += (" (" + formField.mUOM + ")");

            String key = getKeyName(formField.mIdentifier);

            for (ProjectListFieldModel projectListFieldModel : projectFields) {
                if (key != null && key.equals(projectListFieldModel.mIdentifier)) {
                    projectModel = projectListFieldModel;
                    break;
                }
            }
            LatestFieldValue lastSubmittedFieldValue = mLatestLastFieldValuesMapSubmittedByUser.get(formField.mIdentifier);

            String projectFieldValue = "";

            String lastUpdatedValue = "";
            // Adding the current values to the label
            if (projectModel != null) {
                if (projectModel.mProjectListFieldValue.mLabel != null &&
                        !projectModel.mProjectListFieldValue.mLabel.isEmpty() && lastSubmittedFieldValue == null)
                    lastUpdatedValue = " (" + projectModel.mProjectListFieldValue.mLabel + ")";
                    formLabel += lastUpdatedValue;
                if (projectModel.mProjectListFieldValue.mValue != null &&
                        !projectModel.mProjectListFieldValue.mValue.isEmpty())
                    projectFieldValue += projectModel.mProjectListFieldValue.mValue;
            }
            if (lastSubmittedFieldValue != null && lastSubmittedFieldValue.getLabel() != null && !lastSubmittedFieldValue.getLabel().isEmpty()
                    && !(formField.mUiType.equals("video") || formField.mUiType.equals("image") || formField.mUiType.equals("geotagimage")
                    || formField.mUiType.equals("geotagimagefused") || formField.mUiType.equals("timepicker") || formField.mUiType.equals("date"))) {
                lastUpdatedValue = " (" + lastSubmittedFieldValue.getLabel() + ")" ;
                formLabel += lastUpdatedValue;
            }
            if (lastSubmittedFieldValue != null && lastSubmittedFieldValue.getValue() != null && !lastSubmittedFieldValue.getValue().isEmpty())
                projectFieldValue += lastSubmittedFieldValue.getValue();
            AutoPopulateConfig autoPopulateConfig = formField.mAutoPopulateConfig;
            // Adding * to the label for a mandatory field
            if (formField.mValidation != null) {
                if (formField.mValidation.mMandatory) {
                    formLabel += " *";
                }

                // Saving validation related to this field
                ((ProjectFormActivity) getActivity()).mFieldValidations.put(formField.mIdentifier,
                        formField.mValidation);
            }

            String latitude = ((ProjectFormActivity) getActivity()).getProjectLatitude();
            String longitude = ((ProjectFormActivity) getActivity()).getProjectLongitude();

            RendererUtils rendererUtils = new RendererUtils();
            String value;

            switch (formField.mUiType) {

                case "label":
                    View labelElement = mInflater.inflate(R.layout.form_label_view, layout, false);
                    TextView labelText = labelElement.findViewById(R.id.form_label_text);
                    labelText.setTextAlignment(rendererUtils.getAlignment(formField.mAligned));
                    labelText.setText(formLabel);
//                    labelText.setText(formField.mLabel);
                    layout.addView(labelElement);
                    break;

                case "textview":
                    View textViewElement = mInflater.inflate(R.layout.form_text_view, layout, false);

                    LinearLayout textViewRoot = (LinearLayout) textViewElement.findViewById(R.id.form_text_view_root);
                    RelativeLayout textViewExpandableLayout = (RelativeLayout) textViewElement.findViewById(R.id.form_text_view_expandable_layout);
                    RelativeLayout textViewStaticIconLayout = (RelativeLayout) textViewElement.findViewById(R.id.form_text_view_static_icon_layout);
                    TextView textViewLabel = (TextView) textViewElement.findViewById(R.id.form_text_view_label);
                    TextView textViewValue = (TextView) textViewElement.findViewById(R.id.form_text_view_value);
                    ImageView textViewStaticIcon = (ImageView) textViewElement.findViewById(R.id.form_text_view_static_icon);
                    ImageView textViewExpandableIcon = (ImageView) textViewElement.findViewById(R.id.form_text_view_expandable_icon);
                    TextView textViewExpandableText = (TextView) textViewElement.findViewById(R.id.form_text_view_expandable_text);
                    String defaultValueForTextView = formField.mDefault;
                    textViewLabel.setText(formLabel);
                    textViewValue.setTag(tag + "#" + formField.mIdentifier);
                    if(!formField.mDisplay) {
                        textViewElement.setVisibility(View.GONE);
                    }
                    if((projectFieldValue == null || projectFieldValue.isEmpty()) && defaultValueForTextView!= null && !defaultValueForTextView.isEmpty()) {
                        projectFieldValue = defaultValueForTextView;
                    }
                    if (!projectFieldValue.isEmpty() ) {
                        textViewValue.setText(projectFieldValue);
                        JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, projectFieldValue, null);

                        // Saving value to saved fields with key with parent information
                        ((ProjectFormActivity) getActivity()).addFormFieldsSaved(
                                tag + "#" + formField.mIdentifier, formFieldJsonToSave);
                        // Saving value to saved fields with just the simple key
                        String keyTemp = getKeyName(formField.mIdentifier);

                        if (keyTemp != null) {
                            ((ProjectFormActivity) getActivity()).mFieldValues.put(keyTemp,
                                    projectFieldValue);
                        }
                    } else {
                        textViewValue.setVisibility(View.GONE);
                    }

                    //Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        textViewStaticIconLayout.setVisibility(View.VISIBLE);
                        textViewStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }
                    setExpandableProps(formField.mExpandable, textViewExpandableLayout, tag, textViewRoot, textViewExpandableIcon, textViewExpandableText);
                    layout.addView(textViewElement);
                    if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
                        autoPopulateConfig.mView = textViewValue;
                        ((ProjectFormActivity) getActivity()).addToAutoPopulateKeysMap(autoPopulateConfig.getSourceKey(), formField.mIdentifier, autoPopulateConfig);
                        autoPopulateField(formField.mIdentifier);
                    }
                    if(autoPopulateConfig != null && autoPopulateConfig.isSource) {
                        autoPopulateDependentFields(formField.mIdentifier);
                    }
                    break;

                case "edittext":
                    View editTextElement = mInflater.inflate(R.layout.form_edit_text, layout, false);

                    LinearLayout editTextRoot = (LinearLayout) editTextElement.findViewById(R.id.form_edit_text_root);
                    RelativeLayout editTextExpandableLayout = (RelativeLayout) editTextElement.findViewById(R.id.form_edit_text_expandable_layout);
                    RelativeLayout editTextStaticIconLayout = (RelativeLayout) editTextElement.findViewById(R.id.form_edit_text_static_icon_layout);
                    TextView editTextLabel = (TextView) editTextElement.findViewById(R.id.form_edit_text_label);
                    EditText editTextValue = (EditText) editTextElement.findViewById(R.id.form_edit_text_value);
                    ImageView editTextStaticIcon = (ImageView) editTextElement.findViewById(R.id.form_edit_text_static_icon);
                    ImageView editTextExpandableIcon = (ImageView) editTextElement.findViewById(R.id.form_edit_text_expandable_icon);
                    TextView editTextExpandableText = (TextView) editTextElement.findViewById(R.id.form_edit_text_expandable_text);
                    ImageView editTextTransaction = (ImageView) editTextElement.findViewById(R.id.edittext_transaction_info);
                    LinearLayout infoLayout = (LinearLayout) editTextElement.findViewById(R.id.transaction_log_outer_layout);
                    final Spinner uomSpinner = (Spinner) editTextElement.findViewById(R.id.uom_spinner);
                    TextView lastUpdated = (TextView) editTextElement.findViewById(R.id.last_updated_value);

                    editTextValue.setTag(tag + "#" + formField.mIdentifier);

                    editTextValue.setId(mCountForId);

                    mCountForId++;

                    if (lastUpdatedValue == null || lastUpdatedValue.isEmpty()){
                        lastUpdated.setVisibility(View.GONE);
                    } else {
                        lastUpdated.setText(lastUpdatedValue);
                    }
                    if (formField.mLabel != null && !formField.mLabel.isEmpty()) {
                        String label = formField.mLabel;

                        // Adding the UOM to the label
                        if (formField.mUOM != null && !formField.mUOM.isEmpty()) {

                            String uoms[] = formField.mUOM.split(Constants.DEFAULT_DELIMITER);
                            if (uoms.length > 1) {

                                String values[] = new String[uoms.length];
                                for (int i = 0; i < uoms.length; i++) {
                                    values[i] = StringUtils.getTranslatedString(uoms[i]);
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                                        (getActivity(), R.layout.units_spinner_item_layout, values);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                uomSpinner.setAdapter(spinnerArrayAdapter);

                                uomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                        JSONObject updatedObject = ((ProjectFormActivity) getActivity())
                                                .getSubmittedField(tag + "#" + formField.mIdentifier);
                                        final String editTextSavedValue = getValueFromSavedObject(updatedObject, originalLabel);
                                        if (editTextSavedValue != null && !editTextSavedValue.isEmpty()) {
                                            editTextValue.setText(editTextSavedValue);
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parentView) {
                                    }

                                });
                            } else {
                                uomSpinner.setVisibility(View.INVISIBLE);
                                label += (" (" + formField.mUOM + ")");
                            }
                        }
                        editTextLabel.setText(label);
                        // Adding * to the label for a mandatory field
                        if (formField.mValidation != null) {
                            if (formField.mValidation.mMandatory) {
                                String fieldLabel = label + "*";
                                editTextLabel.setText(fieldLabel);
                            }
                        }
                    }

                    if (uomSpinner.getCount() > 0) {
                        String savedValue = null;
                        if (savedObject != null) {
                            try {
                                savedValue = savedObject.getString("uom");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (savedValue != null && !savedValue.isEmpty()) {
                            for (int spinnerItem = 0; spinnerItem < uomSpinner.getCount(); spinnerItem++) {
                                if (((String)uomSpinner.getItemAtPosition(spinnerItem)).equalsIgnoreCase(savedValue)) {
                                    uomSpinner.setSelection(spinnerItem);
                                }
                            }
                        } else{
                            uomSpinner.setSelection(0);
                        }
                    }

                    editTextValue.setInputType(rendererUtils.getInputType(formField.mDatatype));

                    if(PropertyReader.getBooleanProperty("TRANSACTION_INFO")){
                        editTextTransaction.setVisibility(View.VISIBLE);
                        editTextTransaction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showTransactionLog(editTextTransaction, key);
                            }
                        });

                    } else{
                        editTextTransaction.setVisibility(View.GONE);
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
                            if (editable.length() > 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField,
                                        editable.toString(), (String)uomSpinner.getSelectedItem());

                                // Saving value to saved fields with key with parent information
                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(
                                        tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);
                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key,
                                            editable.toString());
                                }

                            } else if (editable.length() == 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, "", null);
                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(
                                        tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                // Get value from projectlist, if it exists
                                getValueFromProjectListConf(key, mProject.mFields);
                            }
                            if(autoPopulateConfig != null && autoPopulateConfig.isSource) {
                                autoPopulateDependentFields(formField.mIdentifier);
                            }
                        }
                    });
                    value = getValueFromSavedObject(savedObject, originalLabel);

                    editTextValue.setText(value);

                    //Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        editTextStaticIconLayout.setVisibility(View.VISIBLE);
                        editTextStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }
                    setExpandableProps(formField.mExpandable, editTextExpandableLayout, tag, editTextRoot, editTextExpandableIcon, editTextExpandableText);
                    layout.addView(editTextElement);
                    if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
                        autoPopulateConfig.mView = editTextValue;
                        ((ProjectFormActivity) getActivity()).addToAutoPopulateKeysMap(autoPopulateConfig.getSourceKey(), formField.mIdentifier, autoPopulateConfig);
                        autoPopulateField(formField.mIdentifier);
                        editTextValue.setEnabled(false);
                    }
                    if(autoPopulateConfig != null && autoPopulateConfig.isSource) {
                        autoPopulateDependentFields(formField.mIdentifier);
                    }

                    break;

                case "toggle":
                    View toggleElement = mInflater.inflate(R.layout.form_toggle, layout, false);

                    LinearLayout toggleRoot = (LinearLayout) toggleElement.findViewById(R.id.form_toggle_root);
                    TextView toggleLabel = (TextView) toggleElement.findViewById(R.id.form_toggle_label);
                    Switch toggleValue = (Switch) toggleElement.findViewById(R.id.form_toggle_view);
                    TextView enabledTextView = (TextView) toggleElement.findViewById(R.id.form_toggle_enabled_text);
                    TextView disabledTextView = (TextView) toggleElement.findViewById(R.id.form_toggle_disabled_text);

                    MultipleValues enabledValue = formField.mMultipleValues.get(0);
                    MultipleValues disabledValue = formField.mMultipleValues.get(1);

                    toggleValue.setTextOn(enabledValue.mValue);
                    enabledTextView.setText(enabledValue.mValue);
                    toggleValue.setTextOff(disabledValue.mValue);
                    disabledTextView.setText(disabledValue.mValue);

                    toggleLabel.setText(formLabel);
                    toggleValue.setTag(tag + "#" + formField.mIdentifier);

                    toggleValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                // The toggle is enabled
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, enabledValue.mValue, null);

                                // Saving value to saved fields with key with parent information
                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(
                                        tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key,
                                            enabledValue.mValue);
                                }

                                if (enabledValue.mExpandable != null) {
                                    expandableItemClicked(enabledValue.mExpandable, toggleRoot, tag);
                                } else {
                                    int childCount = toggleRoot.getChildCount();
                                    if (childCount > 2) {
                                        while (toggleRoot.getChildCount() > 2) {
                                            toggleRoot.removeViewAt(toggleRoot.getChildCount() - 1);
                                        }
                                    }
                                }

                            } else {
                                // The toggle is disabled
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, disabledValue.mValue, null);

                                // Saving value to saved fields with key with parent information
                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key, disabledValue.mValue);
                                }

                                if (disabledValue.mExpandable != null) {
                                    // your code here
                                    expandableItemClicked(disabledValue.mExpandable, toggleRoot, tag);
                                }
                            }
                        }
                    });

                    if (savedObject != null) {
                        value = getValueFromSavedObject(savedObject, originalLabel);
                        if (value.equals(enabledValue.mValue)) {
                            toggleValue.setChecked(true);
                        } else {
                            if (!toggleValue.isChecked()) {
                                toggleValue.setChecked(true);
                                toggleValue.setChecked(false);
                            } else {
                                toggleValue.setChecked(false);
                            }
                        }
                    } else {
                        if (formField.mDefault != null) {
                            if (formField.mDefault.equals(enabledValue.mValue)) {
                                toggleValue.setChecked(true);
                            } else {
                                if (!toggleValue.isChecked()) {
                                    toggleValue.setChecked(true);
                                    toggleValue.setChecked(false);
                                } else {
                                    toggleValue.setChecked(false);
                                }
                            }
                        }
                    }

                    layout.addView(toggleElement);
                    break;

                case "textbox":
                    View textBoxElement = mInflater.inflate(R.layout.form_textbox, layout, false);

                    LinearLayout textBoxRoot = (LinearLayout) textBoxElement.findViewById(R.id.form_textbox_root);
                    RelativeLayout textBoxExpandableLayout = (RelativeLayout) textBoxElement.findViewById(R.id.form_textbox_expandable_layout);
                    RelativeLayout textBoxStaticIconLayout = (RelativeLayout) textBoxElement.findViewById(R.id.form_textbox_static_icon_layout);
                    TextView textBoxLabel = (TextView) textBoxElement.findViewById(R.id.form_textbox_label);
                    EditText textBoxValue = (EditText) textBoxElement.findViewById(R.id.form_textbox_value);
                    ImageView textBoxStaticIcon = (ImageView) textBoxElement.findViewById(R.id.form_textbox_static_icon);
                    ImageView textBoxExpandableIcon = (ImageView) textBoxElement.findViewById(R.id.form_textbox_expandable_icon);
                    TextView textBoxExpandableText = (TextView) textBoxElement.findViewById(R.id.form_textbox_expandable_text);

                    textBoxLabel.setText(formLabel);
                    textBoxValue.setTag(tag + "#" + formField.mIdentifier);
                    String maxLengthHint = "Maximum characters";
                    maxLengthHint = getLabel(maxLengthHint);
                    textBoxValue.setHint(getResources().getString(R.string.max_character)+" (" + formField.mMaxChars + ")");

                    textBoxValue.setId(mCountForId);

                    mCountForId++;

                    textBoxValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.length() > 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, charSequence.toString(), null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key, charSequence.toString());
                                }
                            } else if (charSequence.length() == 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, "", null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                // Get value from projectlist, if it exists
                                getValueFromProjectListConf(key, mProject.mFields);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    value = getValueFromSavedObject(savedObject, originalLabel);
                    textBoxValue.setText(value);

                    //Set Length filter. Restricting to a certain number of characters only
                    textBoxValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(formField.mMaxChars)});

                    //Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        textBoxStaticIconLayout.setVisibility(View.VISIBLE);
                        textBoxStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }
                    setExpandableProps(formField.mExpandable, textBoxExpandableLayout, tag, textBoxRoot, textBoxExpandableIcon, textBoxExpandableText);
                    layout.addView(textBoxElement);
                    break;

                case "timepicker":
                    View timeElement = mInflater.inflate(R.layout.form_time_picker, layout, false);

                    LinearLayout timePickerRoot = (LinearLayout) timeElement.findViewById(R.id.form_time_picker_root);
                    RelativeLayout timePickerExpandableLayout = (RelativeLayout) timeElement.findViewById(R.id.form_time_picker_expandable_layout);
                    RelativeLayout timePickerStaticIconLayout = (RelativeLayout) timeElement.findViewById(R.id.form_time_picker_static_icon_layout);
                    TextView timePickerLabel = (TextView) timeElement.findViewById(R.id.time_picker_label);
                    TextView timePickerValue = (TextView) timeElement.findViewById(R.id.time_picker_value);
                    ImageView timePickerStaticIcon = (ImageView) timeElement.findViewById(R.id.form_time_picker_static_icon);
                    ImageView timePickerExpandableIcon = (ImageView) timeElement.findViewById(R.id.form_time_picker_expandable_icon);
                    TextView timePickerExpandableText = (TextView) timeElement.findViewById(R.id.form_time_picker_expandable_text);

                    timePickerLabel.setText(formLabel);
                    timePickerValue.setTag(tag + "#" + formField.mIdentifier);

                    timePickerValue.setId(mCountForId);

                    mCountForId++;

                    timePickerValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.length() > 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, charSequence.toString(), null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key, charSequence.toString());
                                }

                            } else if (charSequence.length() == 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, "", null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                // Get value from projectlist, if it exists
                                getValueFromProjectListConf(key, mProject.mFields);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                    // Getting current time
                    Calendar timeCalendar = Calendar.getInstance();
                    int hours = timeCalendar.get(Calendar.HOUR_OF_DAY);
                    int mins = timeCalendar.get(Calendar.MINUTE);

                    if (savedObject != null) {

                        value = getValueFromSavedObject(savedObject, originalLabel);
                        // Update hours, mins, secs to the saved values

                        List<String> savedTime = Arrays.asList(value.split(":"));
                        int labelHours = Integer.parseInt(savedTime.get(0));
                        int labelMins = Integer.parseInt(savedTime.get(1));

                        long valueInLong = (labelHours * 3600) + (labelMins * 60);
                        String timeInFormat = Utils.getInstance().changeTimeToHhMmFormat(valueInLong);

                        timePickerValue.setText(timeInFormat);

                    } else {
                        // If there is no saved value
                        timePickerValue.setText(String.format("%02d", hours) + ":" + String.format("%02d", mins));
                    }

                    // Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        timePickerStaticIconLayout.setVisibility(View.VISIBLE);
                        timePickerStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }

                    final int fHours = hours;
                    final int fMins = mins;

                    // Check if there is an expandable component
                    if (formField.mExpandable != null) {
                        // Make expandable component visible
                        timePickerExpandableLayout.setVisibility(View.VISIBLE);
                        if (formField.mExpandable.mIconUrl != null) {
                            // Expandable component has clickable icon
                            timePickerExpandableIcon.setVisibility(View.VISIBLE);
                            timePickerExpandableIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open timer
                                    mCurrentTimePicker = timePickerValue;
                                    CustomTimePickerDialog dialog = new CustomTimePickerDialog(getActivity(), null, fHours, fMins, true, mCurrentTimePicker);
                                    dialog.show();
                                }
                            });
                        } else {
                            // Expandable component has clickable text
                            timePickerExpandableText.setVisibility(View.VISIBLE);
                            timePickerExpandableText.setText(formField.mExpandable.mText);
                            timePickerExpandableText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open timer
                                    mCurrentTimePicker = timePickerValue;
                                    CustomTimePickerDialog dialog = new CustomTimePickerDialog(getActivity(), null, fHours, fMins, true, mCurrentTimePicker);
                                    dialog.show();
                                }
                            });
                        }
                    }

                    layout.addView(timeElement);

                    break;

                case "date":
                    View dateElement = mInflater.inflate(R.layout.form_date_picker, layout, false);

                    LinearLayout datePickerRoot = (LinearLayout) dateElement.findViewById(R.id.form_date_picker_root);
                    RelativeLayout datePickerExpandableLayout = (RelativeLayout) dateElement.findViewById(R.id.form_date_picker_expandable_layout);
                    RelativeLayout datePickerStaticIconLayout = (RelativeLayout) dateElement.findViewById(R.id.form_date_picker_static_icon_layout);
                    TextView datePickerLabel = (TextView) dateElement.findViewById(R.id.date_picker_label);
                    TextView datePickerValue = (TextView) dateElement.findViewById(R.id.date_picker_value);
                    ImageView datePickerStaticIcon = (ImageView) dateElement.findViewById(R.id.form_date_picker_static_icon);
                    ImageView datePickerExpandableIcon = (ImageView) dateElement.findViewById(R.id.form_date_picker_expandable_icon);
                    TextView datePickerExpandableText = (TextView) dateElement.findViewById(R.id.form_date_picker_expandable_text);

                    datePickerLabel.setText(formLabel);
                    datePickerValue.setTag(tag + "#" + formField.mIdentifier);

                    datePickerValue.setId(mCountForId);

                    mCountForId++;

                    datePickerValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.length() > 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, charSequence.toString(), null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key, charSequence.toString());
                                }

                            } else if (charSequence.length() == 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, "", null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                // Get value from projectlist, if it exists
                                getValueFromProjectListConf(key, mProject.mFields);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                    // Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        datePickerStaticIconLayout.setVisibility(View.VISIBLE);
                        datePickerStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }

                    final SelectableDates selectableDates = formField.mSelectableDates;
                    final Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = (c.get(Calendar.MONTH) + 1);
                    int day = c.get(Calendar.DAY_OF_MONTH);
                    if (selectableDates.mSelect != null) {
                        Utils.getInstance().showLog("DATE : ", day + selectableDates.mSelect);
                        String val = day + Integer.parseInt(selectableDates.mSelect) + "/" + month + "/" + year;
                        datePickerValue.setText(val);
                    } else {
                        Utils.getInstance().showLog("DATE : ", day + "");
                        String val = day + "/" + month + "/" + year;
                        datePickerValue.setText(val);
                    }
                    if (savedObject != null) {
                        value = getValueFromSavedObject(savedObject, originalLabel);
                        if (value != null && !value.isEmpty()) {
                            datePickerValue.setText(value);
                        }
                    }
                    // Check if there is an expandable component
                    if (formField.mExpandable != null) {
                        // Make expandable component visible
                        datePickerExpandableLayout.setVisibility(View.VISIBLE);
                        if (formField.mExpandable.mIconUrl != null) {
                            // Expandable component has clickable icon
                            datePickerExpandableIcon.setVisibility(View.VISIBLE);
                            datePickerExpandableIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    DialogFragment newFragment = new DatePickerFragment(getActivity(), datePickerValue, selectableDates);
                                    newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                                }
                            });
                        } else {
                            // Expandable component has clickable text
                            datePickerExpandableText.setVisibility(View.VISIBLE);
                            datePickerExpandableText.setText(formField.mExpandable.mText);
                            datePickerExpandableText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    DialogFragment newFragment = new DatePickerFragment(getActivity(), datePickerValue, selectableDates);
                                    newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                                }
                            });
                        }
                    }

                    layout.addView(dateElement);
                    break;

                case "dropdown":
                    DropDownRenderer dropDownRenderer = new DropDownRenderer();
                    View spinnerElement = mInflater.inflate(R.layout.form_spinner, layout, false);
                    LinearLayout spinnerRoot = (LinearLayout) spinnerElement.findViewById(R.id.form_spinner_root);

                    Spinner spinner = dropDownRenderer.loadDropDown(formField, mInflater, tag, spinnerElement, getActivity());

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                            TextView selectedText = (TextView) parentView.getChildAt(0);
                            if (selectedText != null) {
                                selectedText.setTextColor(Color.WHITE);
                            }
                            ArrayAdapter spinnerArrayAdapter = (ArrayAdapter) spinner.getAdapter();
                            String selectedValue= formField.mMultipleValues == null ?  spinnerArrayAdapter.getItem(position).toString(): formField.mMultipleValues.get(position).mValue;
                            if (selectedValue.length() > 0) {

                                JSONObject formFieldJsonToSave = dropDownRenderer.getJsonObjectToSave(formField, position, selectedValue);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);
                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key,
                                            selectedValue);
                                }
                            }
                            if(formField.mMultipleValues != null) {
                            if (formField.mMultipleValues.get(position).mExpandable != null) {
                                expandableItemClicked(formField.mMultipleValues.get(position).mExpandable, spinnerRoot, tag);
                            } else {
                                int childCount = spinnerRoot.getChildCount();
                                if (childCount > 2) {
                                    while (spinnerRoot.getChildCount() > 2) {
                                        spinnerRoot.removeViewAt(spinnerRoot.getChildCount() - 1);
                                    }
                                }
                            }}
//                            if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
//                                autoPopulateConfig.mView =spinner;
//                                ((ProjectFormActivity) getActivity()).addToAutoPopulateKeysMap(autoPopulateConfig.getSourceKey(), formField.mIdentifier, autoPopulateConfig);
//                                autoPopulateField(formField.mIdentifier);
////                                spinner.setEnabled(false);
//                            }
                            if(autoPopulateConfig != null && autoPopulateConfig.isSource) {
                                autoPopulateDependentFields(formField.mIdentifier);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }

                    });
                    ArrayAdapter spinnerArrayAdapter = (ArrayAdapter) spinner.getAdapter();
                    if (savedObject != null) {
                        /** This value saves the last entered value for the spinner
                         * and updates it when we return to the fragment */
                        value = getValueFromSavedObject(savedObject, originalLabel);
                        int position = spinnerArrayAdapter.getPosition(value);

                        if (position != -1) {
                            spinner.setSelection(position);
                        }
                    } else {
                        if (formField.mDefault != null) {
                            int position = spinnerArrayAdapter.getPosition(formField.mDatatype);
                            if (position != -1) {
                                spinner.setSelection(position);
                            }
                        }
                    }
                    if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
                        autoPopulateConfig.mView = spinner;
                        autoPopulateConfig.mUiType = "dropdown";
                        ((ProjectFormActivity) getActivity()).addToAutoPopulateKeysMap(autoPopulateConfig.getSourceKey(), formField.mIdentifier, autoPopulateConfig);
                        autoPopulateField(formField.mIdentifier);
//                        editTextValue.setEnabled(false);
                    }
                    if(autoPopulateConfig != null && autoPopulateConfig.isSource) {
                        autoPopulateDependentFields(formField.mIdentifier);
                    }
                    layout.addView(spinnerElement);
                    break;

                case "checkbox":
                case "radio":
                    View radioElement = mInflater.inflate(R.layout.form_radio, layout, false);

                    LinearLayout radioRoot = (LinearLayout) radioElement.findViewById(R.id.form_radio_root);
                    RelativeLayout radioStaticIconLayout = (RelativeLayout) radioElement.findViewById(R.id.form_radio_static_icon_layout);
                    TextView radioLabel = (TextView) radioElement.findViewById(R.id.form_radio_label);
                    RadioGroup radioGroup = (RadioGroup) radioElement.findViewById(R.id.form_radio_group);
                    ImageView radioStaticIcon = (ImageView) radioElement.findViewById(R.id.form_radio_static_icon);

                    radioLabel.setText(formLabel);

                    //Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        radioStaticIconLayout.setVisibility(View.VISIBLE);
                        radioStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }

                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            // checkedId is the RadioButton selected
                            if (formField.mMultipleValues.get((checkedId / 20) - 1).mValue.length() > 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(
                                        formField, formField.mMultipleValues.get((checkedId / 20) - 1).mValue, null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key, formField.mMultipleValues.get((checkedId / 20) - 1).mValue);
                                }
                            }

                            // TODO : Test this case
                            if (formField.mMultipleValues.get((checkedId / 20) - 1).mExpandable != null) {
                                expandableItemClicked(formField.mMultipleValues.get((checkedId / 20) - 1).mExpandable, radioRoot, tag);
                            } else {
                                int childCount = radioRoot.getChildCount();
                                if (childCount > 2) {
                                    // Subform has been added before, remove children after 2
                                    for (int i = 2; i < childCount; i++) {
                                        radioRoot.removeViewAt(i);
                                    }
                                }
                            }
                        }
                    });


                    ArrayList<MultipleValues> radioMultipleValues = formField.mMultipleValues;
                    String defaultValue = formField.mDefault;
                    for (int i = 0; i < radioMultipleValues.size(); i++) {
                        RadioButton radioButton = new RadioButton(getActivity());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                                , LinearLayout.LayoutParams.WRAP_CONTENT);

                        params.setMargins(0, 0, 75, 0);
                        radioButton.setLayoutParams(params);
                        radioButton.setId((i + 1) * 20);
                        String optionValue = radioMultipleValues.get(i).mValue;
                        String translatedValue = getLabel(optionValue);
                        String originalValue = optionValue;
                        radioButton.setText(translatedValue);

                        radioButton.setTextColor(getResources().getColor(R.color.white));

                        radioButton.setTag(tag + "#" + formField.mIdentifier);

                        if (savedObject != null) {
                            value = getValueFromSavedObject(savedObject, originalLabel);

                            if (value.equals(radioMultipleValues.get(i).mValue)) {
                                radioButton.setChecked(true);
                            }

                        } else if (defaultValue != null && !defaultValue.isEmpty()) {
                            if (defaultValue.equals(radioMultipleValues.get(i).mValue)) {
                                radioButton.setChecked(true);
                            }

                        } else {
                            if (i == 0) {
                                radioButton.setChecked(true);
                            }
                        }
                        radioGroup.addView(radioButton);
                    }

                    layout.addView(radioElement);
                    break;

                // TODO : Handle checkbox case (Multi select)
//                case "checkbox":
//                    View view = mInflater.inflate(R.layout.form_checkbox, layout, false);
//
//                    LinearLayout checkBoxRoot = (LinearLayout) view.findViewById(R.id.form_checkbox_root);
//                    RelativeLayout checkBoxStaticIconLayout = (RelativeLayout) view.findViewById(R.id.form_checkbox_static_icon_layout);
//                    TextView checkBoxLabel = (TextView) view.findViewById(R.id.form_checkbox_label);
//                    LinearLayout checkBoxGroup = (LinearLayout) view.findViewById(R.id.form_checkbox_group);
//                    ImageView checkBoxStaticIcon = (ImageView) view.findViewById(R.id.form_checkbox_static_icon);
//
//                    checkBoxLabel.setText(formLabel);
//
//                    //Check if there is a static icon
//                    if (formField.mIcon != null) {
//                         Set icon
//                        checkBoxStaticIconLayout.setVisibility(View.VISIBLE);
//                        checkBoxStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
//                    }
//
//                    ArrayList<MultipleValues> multipleValues = formField.mMultipleValues;
//
//                    for (int i = 0; i< multipleValues.size(); i++) {
//                        CheckBox checkBox = new CheckBox(getActivity());
//                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
//                                (LinearLayout.LayoutParams.WRAP_CONTENT
//                                        , LinearLayout.LayoutParams.WRAP_CONTENT);
//                        params.setMargins(0,0,75,0);
//                        checkBox.setLayoutParams(params);
//                        checkBox.setText(multipleValues.get(i).mValue);
//                        checkBox.setTextColor(getResources().getColor(R.color.white));
//                        checkBox.setTag(formField.mIdentifier);
//                        if (i==0) {
//                            checkBox.setChecked(true);
//                        }
//                        checkBox.setOnClickListener( new View.OnClickListener() {
//                            public void onClick(View v) {
//
//                                CheckBox cb = (CheckBox) v ;
//                                if(cb.isChecked()){
//                                    for (MultipleValues values : multipleValues) {
//                                        if (checkBox.getText().toString().equals(values.mValue)) {
//                                            if (values.mExpandable != null) {
//                                                expandableItemClicked(values.mExpandable, checkBoxRoot);
//                                            }
//                                        }
//                                    }
//                                }
//
//                            }
//                        });
//                        checkBoxGroup.addView(checkBox);
//                    }
//
//                    layout.addView(view);
//                    break;

                case "direction":

                    /** If the user has an active internet connection, they are redirected to Google Maps.
                     * If the user does not have an active internet connection, they are
                     * redirected to the Navigation screen of Offline Maps module */

                    String directionLat = latitude;
                    String directionLon = longitude;

                    if (directionLat.equals("") && directionLon.equals("")) {
                        String commaSeperatedLocation = formField.mDefault;
                        if(commaSeperatedLocation == null ) {
                            continue;
                        }
                        List<String> latLng = Arrays.asList
                                (commaSeperatedLocation.trim().split("\\s*,\\s*"));
                        if (latLng.size() == 2) {
                            directionLat = latLng.get(0);
                            directionLon = latLng.get(1);
                        }
                    }

                    final String dLat = directionLat;
                    final String dLon = directionLon;

                    View directionElement = mInflater.inflate(R.layout.form_directions_element, layout, false);

                    LinearLayout formDirectionsRoot = (LinearLayout) directionElement.findViewById(R.id.form_directions_view_root);
                    RelativeLayout formDirectionsExpandableLayout = (RelativeLayout) directionElement.findViewById(R.id.form_directions_view_expandable_layout);
                    TextView formDirectionsLabel = (TextView) directionElement.findViewById(R.id.form_directions_view_label);
                    TextView formDirectionsExpandableText = (TextView) directionElement.findViewById(R.id.form_directions_view_expandable_text);

                    formDirectionsLabel.setText(formLabel);

                    // Expandable component has clickable text
                    formDirectionsExpandableLayout.setVisibility(View.VISIBLE);
                    formDirectionsExpandableText.setVisibility(View.VISIBLE);
                    formDirectionsExpandableText.setText(R.string.get_direction);
                    formDirectionsExpandableText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // If active internet connection, open google maps
                            // If no internet connection, load navigation module from offline maps
                            if (Utils.getInstance().isOnline(null)) {
                                // Google Maps
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("http://maps.google.com/maps?daddr=" + dLat + "," + dLon));
                                ((ProjectFormActivity) getActivity()).startActivity(intent);
                            } else {
                                // Navigation screen from OfflineMaps module
                                GeoPoint destination = new GeoPoint(Double.parseDouble(dLat), Double.parseDouble(dLon));
                                String destinationName = "";
                                if (mProject.mProjectName != null && !mProject.mProjectName.isEmpty()) {
                                    destinationName = mProject.mProjectName;
                                } else {
                                    destinationName = "Destination";
                                }
                                Destination.getDestination().setEndPoint(destination, destinationName);
                                Destination.getDestination().setExtraProjects(MapUtils.getInstance()
                                        .getProjectsForOfflineNavigation(mProject.mProjectId,
                                                ((ProjectFormActivity) getActivity()).mAppId));
                                String file_path = Environment.getExternalStorageDirectory() + "/" + "osmdroid";
                                List<String> map_files = new ArrayList<>(MapHelper.getInstance().getFilesFromFolder(file_path));
                                Destination.getDestination().setFilePath(file_path);
                                Destination.getDestination().setFile_names(map_files);
                                Destination.getDestination().setmAppId(((ProjectFormActivity) getActivity()).mAppId);
                                Intent intent = new Intent(context, NavigationActivity.class);
                                startActivity(intent);
                            }
                        }
                    });

                    layout.addView(directionElement);
                    break;

                case "geotag":
                    String geotagLat = latitude;
                    String geotagLon = longitude;

//                    if (geotagLat.isEmpty() && geotagLon.isEmpty()
//                            && formField.mDefault != null && !formField.mDefault.isEmpty()) {
//                        String commaSeperatedLocation = formField.mDefault;
//                        List<String> latLng = Arrays.asList
//                                (commaSeperatedLocation.trim().split("\\s*,\\s*"));
//                        if (latLng.size() == 2) {
//                            geotagLat = latLng.get(0);
//                            geotagLon = latLng.get(1);
//                        }
//                    }

                    final String gLat = geotagLat;
                    final String gLon = geotagLon;

                    View geotagElement = mInflater.inflate(R.layout.form_geotag_element, layout, false);

                    LinearLayout formGeotagRoot = (LinearLayout) geotagElement.findViewById(R.id.form_geotag_view_root);
                    RelativeLayout formGeotagExpandableLayout = (RelativeLayout) geotagElement.findViewById(R.id.form_geotag_view_expandable_layout);
                    TextView formGeotagLabel = (TextView) geotagElement.findViewById(R.id.form_geotag_view_label);
                    TextView formGeotagExpandableText = (TextView) geotagElement.findViewById(R.id.form_geotag_view_expandable_text);
                    TextView formGeotagValue = (TextView) geotagElement.findViewById(R.id.form_geotag_value);

                    formGeotagLabel.setText(formLabel);
                    if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
                        autoPopulateConfig.mView = formGeotagValue;
                        ((ProjectFormActivity) getActivity()).addToAutoPopulateKeysMap(autoPopulateConfig.getSourceKey(), formField.mIdentifier, autoPopulateConfig);
//                        autoPopulateField(formField.mIdentifier);
                    }
                    formGeotagValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                            if (charSequence != null) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, charSequence.toString(), null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key, charSequence.toString());
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(autoPopulateConfig != null && autoPopulateConfig.isSource) {
                                autoPopulateDependentFields(formField.mIdentifier);
                            }
                        }
                    });

                    value = getValueFromSavedObject(savedObject, originalLabel);
                    if (value != null && !value.isEmpty()) {
                        formGeotagValue.setText(value);
                    }

                    // Expandable component has clickable text
                    formGeotagExpandableLayout.setVisibility(View.VISIBLE);
                    formGeotagExpandableText.setVisibility(View.VISIBLE);
                    formGeotagExpandableText.setText(R.string.view_on_map);
                    formGeotagExpandableText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Starting map activity
                            Intent intent = new Intent(getContext(), MapActivity.class);

                            HashMap<String, String> submittedFieldsMap = convertJSONObjectToStringInMap(((ProjectFormActivity) getActivity()).mSubmittedFields);
                            intent.putExtra("submittedFields", submittedFieldsMap);

                            if (formField.mGpsValidation != null) {
                                // Have to perform validation
                                intent.putExtra("gps_validation", formField.mGpsValidation);
                            }

                            if (formField.mMapFieldInfo != null) {
                                intent.putExtra("map_field_info", formField.mMapFieldInfo);
                            }

                            if (formGeotagValue != null && !formGeotagValue.getText().toString().isEmpty()){
                                List<String> latLng = Arrays.asList
                                        (formGeotagValue.getText().toString().trim().split("\\s*,\\s*"));
                                if (latLng.size() == 2) {
                                    intent.putExtra(Constants.LATITUDE_KEY, latLng.get(0));
                                    intent.putExtra(Constants.LONGITUDE_KEY, latLng.get(1));
                                }
                            } else {
                                intent.putExtra(Constants.LATITUDE_KEY, "");
                                intent.putExtra(Constants.LONGITUDE_KEY, "");
                            }
                            // TODO :  add a null check for project lat lon
                            intent.putExtra(Constants.PROJECT_LATITUDE, gLat);
                            intent.putExtra(Constants.PROJECT_LONGITUDE, gLon);

                            intent.putExtra(Constants.PROJECT_TYPE_CONFIG_APP_ID_KEY, ((ProjectFormActivity)getActivity()).mAppId);
                            mCurrentGeotagTextview = formGeotagValue;
                            startActivityForResult(intent, Constants.REQUEST_GEOTAG_PIN_DROP);
                        }
                    });

                    layout.addView(formGeotagRoot);
                    break;

//                        if (Utils.getInstance().isOnline(getActivity())) {
//                            String lat = latitude;
//                            String lon = longitude;
//                            if (lat.equals("") && lon.equals("")) {
//                                String commaSeperatedLocation = formField.mDefault;
//                                List<String> latLng = Arrays.asList
//                                        (commaSeperatedLocation.trim().split("\\s*,\\s*"));
//                       \         if (latLng.size() == 2) {
//                                    lat = latLng.get(0);
//                                    lon = latLng.get(1);
//                                }
//                            }
//
//                            final String finalLat = lat;
//                            final String finalLon = lon;
//
//                            View geotagView = null;
//
//                            if (formField.mExpandable != null) {
//                                if (formField.mExpandable.mType == 1) {
//                                    // Show an embedded map element
//                                    geotagView = mInflater.inflate(R.layout.form_embedded_geotag
//                                            , layout, false);
//
//                                    TextView geotagLabel = (TextView) geotagView.findViewById(R.id.form_embedded_geotag_label);
//                                    final TextView geotagValue = (TextView) geotagView.findViewById(R.id.form_embedded_geotag_value);
//                                    geotagLabel.setText(formLabel);
//
//                                    MapView mapView = (MapView) geotagView.findViewById(R.id.form_embedded_geotag_map_view);
//
//                                    mapView.getMapAsync(new OnMapReadyCallback() {
//                                        @Override
//                                        public void onMapReady(@NonNull MapboxMap mapboxMap) {
//
//                                            mapboxMap.setCameraPosition(new CameraPosition.Builder()
//                                                    .target(new LatLng(Double.parseDouble(finalLat), Double.parseDouble(finalLon)))
//                                                    .zoom(5)
//                                                    .build());
//
//                                            mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
//                                                @Override
//                                                public void onStyleLoaded(@NonNull Style style) {
//
//                                                    // Map is set up and the style has loaded.
//                                                    // Now you can add data or make other map adjustments
//                                                    List<Feature> markerCoordinates = new ArrayList<>();
//                                                    markerCoordinates.add(Feature.fromGeometry(
//                                                            Point.fromLngLat(Double.parseDouble(finalLon), Double.parseDouble(finalLat)))); // Guntur
//
//                                                    style.addSource(new GeoJsonSource("marker-source",
//                                                            FeatureCollection.fromFeatures(markerCoordinates)));
//
//                                                    // Add the marker image to map
//                                                    style.addImage("my-marker-image", BitmapFactory.decodeResource(
//                                                            getActivity().getResources(), R.drawable.map_marker_icon));
//
//                                                    // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
//                                                    // middle of the icon being fixed to the coordinate point.
//                                                    style.addLayer(new SymbolLayer("marker-layer", "marker-source")
//                                                            .withProperties(PropertyFactory.iconImage("my-marker-image"),
//                                                                    PropertyFactory.iconOffset(new Float[]{0f, -9f})));
//
//                                                    // Add the selected marker source and layer
//                                                    style.addSource(new GeoJsonSource("selected-marker"));
//
//                                                    // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
//                                                    // middle of the icon being fixed to the coordinate point.
//                                                    style.addLayer(new SymbolLayer("selected-marker-layer", "selected-marker")
//                                                            .withProperties(PropertyFactory.iconImage("my-marker-image"),
//                                                                    PropertyFactory.iconOffset(new Float[]{0f, -9f})));
//                                                }
//                                            });
//                                        }
//                                    });
//                                }
//                            }
//                            if (geotagView != null)
//                                layout.addView(geotagView);
//                        } else {
//                            // TODO : Offline geotagging
//                        }
//                        break;

                case "video":
                    View videoElement = mInflater.inflate(R.layout.form_video, layout, false);

                    final LinearLayout videoGroup = (LinearLayout) videoElement.findViewById(R.id.form_video_group);
                    LinearLayout videoRoot = (LinearLayout) videoElement.findViewById(R.id.form_video_root);
                    RelativeLayout videoExpandableLayout = (RelativeLayout) videoElement.findViewById(R.id.form_video_expandable_layout);
                    RelativeLayout videoStaticIconLayout = (RelativeLayout) videoElement.findViewById(R.id.form_video_static_icon_layout);
                    TextView videoLabel = (TextView) videoElement.findViewById(R.id.form_video_group_label);
                    TextView videoValue = (TextView) videoElement.findViewById(R.id.form_video_uuids);
                    ImageView videoStaticIcon = (ImageView) videoElement.findViewById(R.id.form_video_static_icon);
                    ImageView videoExpandableIcon = (ImageView) videoElement.findViewById(R.id.form_video_expandable_icon);
                    TextView videoExpandableText = (TextView) videoElement.findViewById(R.id.form_video_expandable_text);

                    videoLabel.setText(formLabel);
                    videoValue.setTag(tag + "#" + formField.mIdentifier);

                    videoValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.length() > 0) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, charSequence.toString(), null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(
                                        tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key,
                                            charSequence.toString());
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });
                    value = getValueFromSavedObject(savedObject, originalLabel);
                    if (value != null) {
                        mCurrentVideoLinearLayout = videoGroup;
                        mCurrentVideoLinearLayout.removeAllViews();

                        videoValue.setText(value);
                        List<String> uuids = Arrays.asList(value.split("\\s*,\\s*"));
                        for (String uuid : uuids) {
                            FormMedia formVideo = ((ProjectFormActivity) getActivity())
                                    .mDBHelper.getFormMedia(uuid, ((ProjectFormActivity) getActivity()).mAppId, UAAppContext.getInstance().getUserID());
                            if (formVideo != null) {
                                // Create thumbnails
                                createVideoThumbnails(formVideo);
                            }
                        }
                    }

                    //Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        videoStaticIconLayout.setVisibility(View.VISIBLE);
                        videoStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }

                    // Check if there is an expandable component
                    if (formField.mExpandable != null) {
                        // Make expandable component visible
                        videoExpandableLayout.setVisibility(View.VISIBLE);
                        if (formField.mExpandable.mIconUrl != null) {
                            // Expandable component has clickable icon
                            videoExpandableIcon.setVisibility(View.VISIBLE);
                            videoExpandableIcon.setImageDrawable(getResources().getDrawable(R.drawable.video_icon));
                            videoExpandableIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open Camera Activity
                                    if (Utils.getInstance().checkCameraHardware(((ProjectFormActivity) getActivity()))) {
                                        // Check for the current values of imageValue
                                        String videos = videoValue.getText().toString();
                                        if (videos != null && !videos.isEmpty()) {
                                            // Delete these images
                                            List<String> uuids = Arrays.asList(videos.split("\\s*,\\s*"));
                                            if (uuids.size() > 0) {
                                                for (String uuid : uuids) {
                                                    FormMedia formVideo = UAAppContext.getInstance().getDBHelper().getFormMedia(uuid, ((ProjectFormActivity) getActivity()).mAppId, UAAppContext.getInstance().getUserID());
                                                    if (formVideo != null) {
                                                        // Delete video from Storage
                                                        Utils.getInstance().deleteVideoFromStorage(getActivity(), formVideo.getLocalPath());
                                                        //  Deleting image from DB
                                                        UAAppContext.getInstance().getDBHelper().deleteFormMedia(uuid);
                                                    }
                                                }
                                            }
                                            //  Clearing image uuids from the UI element
                                            videoValue.setText("");
                                            // Clear the preview layout too
                                            videoGroup.removeAllViews();
                                        }

                                        // Device has camera
                                        // If the SDK is >= Marshmello, runtime permissions required
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            boolean isAudioPermissionGiven = Utils.getInstance()
                                                    .checkForPermission(((ProjectFormActivity) getActivity())
                                                            , Manifest.permission.RECORD_AUDIO);
                                            boolean isCameraPermissionGiven = Utils.getInstance()
                                                    .checkForPermission(((ProjectFormActivity) getActivity())
                                                            , Manifest.permission.CAMERA);
                                            if (isAudioPermissionGiven && isCameraPermissionGiven) {
                                                mCurrentVideoLinearLayout = videoGroup;
                                                mCurrentVideoUuids = videoValue;
                                                startVideoCaptureActivity(formField.mIdentifier
                                                        , formField.mDatatype, formField.mMax,
                                                        formField.mIdentifier);
                                            } else {
                                                ActivityCompat.requestPermissions(getActivity(),
                                                        new String[]{Manifest.permission.RECORD_AUDIO,
                                                                Manifest.permission.CAMERA},
                                                        Constants.PERMISSION_VIDEO);
                                            }
                                        } else {
                                            mCurrentVideoLinearLayout = videoGroup;
                                            mCurrentVideoUuids = videoValue;
                                            startVideoCaptureActivity(formField.mIdentifier
                                                    , formField.mDatatype, formField.mMax,
                                                    formField.mIdentifier);
                                        }
                                    } else {
                                        // Device does not have camera
                                        Toast.makeText(getActivity(), getResources().getString(R.string.no_camera)
                                                , Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            // Expandable component has clickable text
                            videoExpandableText.setVisibility(View.VISIBLE);
                            videoExpandableText.setText(formField.mExpandable.mText);
                            videoExpandableText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open VideoCaptureActivity
                                }
                            });
                        }
                    }

                    layout.addView(videoElement);
                    break;

                case "geotagimage":
                case "geotagimagefused":
                case "image":
                    String lat = latitude;
                    String lon = longitude;
                    if (lat.equals("") && lon.equals("") && formField.mDefault != null) {
                        String commaSeparatedLocation = formField.mDefault;
                        List<String> latLng = Arrays.asList
                                (commaSeparatedLocation.trim().split("\\s*,\\s*"));
                        if (latLng.size() == 2) {
                            lat = latLng.get(0);
                            lon = latLng.get(1);
                        }
                    }

                    final String imageLatitude = lat;
                    final String imageLongitude = lon;

                    View imageView = mInflater.inflate(R.layout.form_image_group, mUIElementsLinearLayout, false);

                    final LinearLayout imageGroup = (LinearLayout) imageView.findViewById(R.id.form_image_group);
                    LinearLayout imageRoot = (LinearLayout) imageView.findViewById(R.id.form_image_root);
                    RelativeLayout imageExpandableLayout = (RelativeLayout) imageView.findViewById(R.id.form_image_expandable_layout);
                    RelativeLayout imageStaticIconLayout = (RelativeLayout) imageView.findViewById(R.id.form_image_static_icon_layout);
                    TextView imageLabel = (TextView) imageView.findViewById(R.id.form_image_group_label);
                    TextView imageValue = (TextView) imageView.findViewById(R.id.form_image_uuids);
                    ImageView imageStaticIcon = (ImageView) imageView.findViewById(R.id.form_image_static_icon);
                    ImageView imageExpandableIcon = (ImageView) imageView.findViewById(R.id.form_image_expandable_icon);
                    TextView imagetExpandableText = (TextView) imageView.findViewById(R.id.form_image_expandable_text);

                    imageLabel.setText(formLabel);
                    imageValue.setTag(tag + "#" + formField.mIdentifier);

                    imageValue.setId(mCountForId);

                    mCountForId++;
//                    if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
//                        autoPopulateConfig.mView = imageView;
//                        ((ProjectFormActivity) getActivity()).addToAutoPopulateKeysMap(autoPopulateConfig.getSourceKey(), formField.mIdentifier, autoPopulateConfig);
////                        autoPopulateField(formField.mIdentifier);
//                    }
                    imageValue.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence != null) {
                                JSONObject formFieldJsonToSave = rendererUtils.getJsonObjectToSave(formField, charSequence.toString(), null);

                                ((ProjectFormActivity) getActivity()).addFormFieldsSaved(tag + "#" + formField.mIdentifier, formFieldJsonToSave);

                                // Saving value to saved fields with just the simple key
                                String key = getKeyName(formField.mIdentifier);

                                if (key != null) {
                                    ((ProjectFormActivity) getActivity()).mFieldValues.put(key, charSequence.toString());
                                }
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if(autoPopulateConfig != null && autoPopulateConfig.isSource) {
                                autoPopulateDependentFields(formField.mIdentifier);
                            }

                        }
                    });

                    value = getValueFromSavedObject(savedObject, originalLabel);
                    if (value != null) {
                        imageValue.setText(value);
                        mCurrentImageLinearLayout = imageGroup;
                        mCurrentImageLinearLayout.removeAllViews();
                        List<String> uuidsWithLongLat = Arrays.asList(value.split("\\s*,\\s*"));
                        List<String> uuids = Utils.getImageUUIDList(uuidsWithLongLat);
                        for (String uuid : uuids) {
                            FormMedia formImage = ((ProjectFormActivity) getActivity())
                                    .mDBHelper.getFormMedia(uuid, ((ProjectFormActivity) getActivity()).mAppId, UAAppContext.getInstance().getUserID());
                            if (formImage != null) {
                                // Create thumbnails
                                createNewImageThumbnail(formImage, imageGroup, imageValue);
                            }
                        }
                    }
                    //Check if there is a static icon
                    if (formField.mIcon != null) {
                        // Set icon
                        imageStaticIconLayout.setVisibility(View.VISIBLE);
                        imageStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                    }

                    if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
                        autoPopulateConfig.mView = imageView;
                        ((ProjectFormActivity) getActivity()).addToAutoPopulateKeysMap(autoPopulateConfig.getSourceKey(), formField.mIdentifier, autoPopulateConfig);
                    }
                    // Check if there is an expandable component
                    if (formField.mExpandable != null) {
                        // Make expandable component visible
                        imageExpandableLayout.setVisibility(View.VISIBLE);
                        if (formField.mExpandable.mIconUrl != null) {
                            // Expandable component has clickable icon
                            imageExpandableIcon.setVisibility(View.VISIBLE);
                            if (formField.mUiType.equals("geotagimage") || formField.mUiType.equals("geotagimagefused")) {
                                imageExpandableIcon.setImageDrawable(getResources().getDrawable(R.drawable.geotagged_picture));
                            } else {
                                imageExpandableIcon.setImageDrawable(getResources().getDrawable(R.drawable.picture));
                            }
                            imageExpandableIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open Camera Activity
                                    List<String> uuidsWithLonLat = new ArrayList<>();
                                    List<String> uuids = new ArrayList<>();

                                    if (Utils.getInstance().checkCameraHardware(((ProjectFormActivity) getActivity()))) {
                                        // Check for the current values of imageValue
                                        String images = imageValue.getText().toString();
                                        if (images != null && !images.isEmpty()) {
                                            // Delete these images
                                            uuidsWithLonLat.addAll(Arrays.asList(images.split("\\s*,\\s*")));
                                            uuids = Utils.getImageUUIDList(uuidsWithLonLat);
                                                /*if (uuids.size() > 0) {
                                                    for (String uuid : uuids) {
                                                        FormImage formImage = ((ProjectFormActivity) getActivity()).mDBHelper.getFormImage(uuid);
                                                        if (formImage != null) {
                                                            // Deleting image from Storage
                                                            Utils.getInstance().deleteImageFromStorage(getActivity(), formImage.mLocalPath);
                                                            // Deleting image from DB
                                                            ((ProjectFormActivity) getActivity()).mDBHelper.deleteFormImage(uuid);
                                                        }
                                                    }
                                                }*/
                                            //  Clearing image uuids from the UI element
                                            //imageValue.setText("");
                                            // Clear the preview layout too
                                            //imageGroup.removeAllViews();
                                        }

                                        // Device has camera
                                        // If the SDK is >= Marshmello, runtime permissions required
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            boolean isPermissionGiven = Utils.getInstance()
                                                    .checkForPermission(((ProjectFormActivity) getActivity())
                                                            , Manifest.permission.CAMERA);
                                            if (isPermissionGiven) {
                                                mCurrentImageLinearLayout = imageGroup;
                                                mCurrentImageUuids = imageValue;
                                                startCameraActivity(formField.mIdentifier, formField.mDatatype, formField.mUiType, formField.mMax, formField.mIdentifier, imageLatitude,
                                                        imageLongitude, uuids, formField.mGpsValidation);
                                            } else {
                                                ActivityCompat.requestPermissions(getActivity(),
                                                        new String[]{Manifest.permission.CAMERA},
                                                        Constants.PERMISSION_CAMERA);
                                            }
                                        } else {
                                            mCurrentImageLinearLayout = imageGroup;
                                            mCurrentImageUuids = imageValue;
                                            startCameraActivity(formField.mIdentifier, formField.mDatatype, formField.mUiType, formField.mMax, formField.mIdentifier, imageLatitude,
                                                    imageLongitude, uuids, formField.mGpsValidation);
                                        }
                                    } else {
                                        // Device does not have camera
                                        Toast.makeText(getActivity(), getResources().getString(R.string.no_camera)
                                                , Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            // Expandable component has clickable text
                            imagetExpandableText.setVisibility(View.VISIBLE);
                            imagetExpandableText.setText(formField.mExpandable.mText);
                            imagetExpandableText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open Camera Activity
                                }
                            });
                        }
                    }

                    layout.addView(imageView);
                    break;

                default:
                    break;
            }
        }

        List<View> views = new ArrayList<View>();
        final int childCount = layout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.add(child);
            }

            final Object tagObj = child.getId();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
            Utils.getInstance().showLog("CHILD BY ID", child.toString());
        }
    }

    private String getValueFromSavedObject(JSONObject savedObject, String formLabel) {
        String value = null;
        if (savedObject != null) {
            try {
                value = savedObject.getString("val");
                Utils.getInstance().showLog(formLabel + " SavedValue ", value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    private void setExpandableProps(ExpandableComponent expandable, View textViewExpandableLayout, String tag, LinearLayout rootView, ImageView expandableIcon, TextView expandableText) {
        // Check if there is an expandable component
        if (expandable != null) {
            // Make expandable component visible
            textViewExpandableLayout.setVisibility(View.VISIBLE);
            if (expandable.mIconUrl != null) {
                // Expandable component has clickable icon
                expandableIcon.setVisibility(View.VISIBLE);
                expandableIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                expandableIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expandableItemClicked(expandable, rootView, tag);
                    }
                });
            } else {
                // Expandable component has clickable text
                expandableText.setVisibility(View.VISIBLE);
                String textValue = expandable.mText;
                String translatedValue = getLabel(textValue);
                String originalText =  textValue;
                expandableText.setText(translatedValue);
                expandableText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expandableItemClicked(expandable, rootView, tag);
                    }
                });
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

    private void getValueFromProjectListConf(String key, ArrayList<ProjectListFieldModel> fields) {
        if (key != null && fields != null) {
            for (ProjectListFieldModel fieldModel : fields) {
                if (key.equals(fieldModel.mIdentifier)) {
                    if (fieldModel.mProjectListFieldValue != null) {
                        if (fieldModel.mProjectListFieldValue.mValue != null) {
                            ((ProjectFormActivity) getActivity()).mFieldValues.put(key,
                                    fieldModel.mProjectListFieldValue.mValue);
                            break;
                        } else {
                            ((ProjectFormActivity) getActivity()).mFieldValues.put(key,
                                    "");
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Renders custom buttons (formbridges if any)
     *
     * @param bridge
     * @param layout
     */
    private void loadBridges(FormBridge bridge, LinearLayout layout) {
        if (bridge == null) {
            return;
        }
        for (BridgeValue bridgeValue : bridge.mBridgeValues) {
            View bridgeElement = mInflater.inflate(R.layout.form_bridge_layout, layout, false);
            RelativeLayout rootLayout = (RelativeLayout) bridgeElement.findViewById(R.id.form_bridge_root_layout);
            ImageView bridgeImage = (ImageView) bridgeElement.findViewById(R.id.form_bridge_image);
            TextView bridgeText = (TextView) bridgeElement.findViewById(R.id.form_bridge_text);
            UnifiedAppDBHelper dbHelper = UAAppContext.getInstance().getDBHelper();

            bridgeText.setText(bridgeValue.mValue);
            if (bridgeValue.mBackgroundColor != null) {
                rootLayout.setBackgroundColor(Color.parseColor(bridgeValue.mBackgroundColor));
            }


            bridgeElement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ProjectFormActivity) getActivity()).mFormBridgeKey = bridge.mKey;
                    ((ProjectFormActivity) getActivity()).mFormBridgeValue = bridgeValue.mValue;
                    ((ProjectFormActivity) getActivity()).mFormBridgeMandatoryFields.clear();
                    if (bridgeValue.mMandatoryFields != null && !bridgeValue.mMandatoryFields.isEmpty()) {
                        ((ProjectFormActivity) getActivity()).mFormBridgeMandatoryFields.addAll(bridgeValue.mMandatoryFields);
                    }
                    expandableItemClicked(bridgeValue.mExpandableComponent, null, mTag);
                }
            });

            if (bridgeValue.mExpandableComponent.mIconUrl != null) {
                IncomingImage image = dbHelper.getIncomingImageWithUrl(bridgeValue.mExpandableComponent.mIconUrl);
                if (image != null) {
                    if (image.getImageLocalPath() == null || image.getImageLocalPath().isEmpty()) {
                        // Setting the default image, while the icon downloads
                        bridgeImage.setImageResource(R.drawable.splash_app_logo);
                        // Image download failed the first time, download again
                        ArrayList<IncomingImage> rootImage = new ArrayList<>();
                        rootImage.add(image);
                        DownloadImageIfFailedTask object = new DownloadImageIfFailedTask(getActivity(), dbHelper, rootImage);
                        object.execute();
                    } else {
                        File imgFile = new File(image.getImageLocalPath());
                        if (imgFile.exists()) {
                            if (imgFile.length() == 0) {
                                // Setting the default image, while the icon downloads
                                bridgeImage.setImageResource(R.drawable.splash_app_logo);
                                // Image download failed the first time, download again
                                ArrayList<IncomingImage> rootImage = new ArrayList<>();
                                rootImage.add(image);
                                DownloadImageIfFailedTask object = new DownloadImageIfFailedTask(getActivity(), dbHelper, rootImage);
                                object.execute();
                            } else {
                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                bridgeImage.setImageBitmap(myBitmap);
                            }
                        } else {
                            // Image does not exist
                            bridgeImage.setImageResource(R.drawable.splash_app_logo);
                        }
                    }
                } else {
                    // Image does not exist
                    bridgeImage.setImageResource(R.drawable.splash_app_logo);
                }
            } else {
                bridgeImage.setImageResource(R.drawable.splash_app_logo);
            }

            layout.addView(bridgeElement);
        }
    }

    /**
     * Load buttons from the form
     * Performs validations for preview and save button
     *
     * @param buttons
     * @param layout
     */
    private void loadButtons(ArrayList<FormButton> buttons, LinearLayout layout) {
        if (buttons == null || buttons.isEmpty()) {
            return;
        }
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
            String label = formButton.mLabel;
            String traslatedLabel = getLabel(label);
            String originalLabel = label;
            label = traslatedLabel;
            button.setText(label);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        switch (formButton.mExpandable.mType) {
                            case 10:
                                // Cancel : Pop the current fragment
                                if(mProject.mState.equalsIgnoreCase("New")) {
                                    ((ProjectFormActivity) getActivity()).mSubmissionArray = new JSONArray();
                                }
                                ((ProjectFormActivity) getActivity()).popCurrentFragment();
                                break;
                            case 11:
                                // Edit (New Fragment) new fields from sub-form, in 'Expandables' object
                                expandableItemClicked(formButton.mExpandable, null, mTag);
                                break;
                            case 12:
                                // Preview
                                performValidation(formButton, 12);
                                break;
                            case 13:
                                // Submit the form
                                submitForm(formButton);
                                break;
                            case 14:
                                // Save the current fields of the fragment
                                // Saving the current subform after validation
                                performValidation(formButton, 14);
                                break;
                            default:
                                Utils.logError("INVALID ENTRY", "Wrong button type found - " + formButton.mExpandable.mType);
                                break;
                        }
                    } catch (JSONException e) {
                        Utils.logError("LOAD_BUTTONS", "JSON Exception while loading buttons");
                        e.printStackTrace();
                    }
                }
            });
            layout.addView(button);
        }
    }

    /**
     * Performs Two types of validations
     * 1. Mandatory Validation
     * 2. SPEL Expression Validation
     *
     * @param formButton
     * @param buttonType
     * @throws JSONException
     */
    private void performValidation(FormButton formButton, int buttonType) throws JSONException {

        FormDataValidationService formDataValidationService = new FormDataValidationService();

        boolean isMandatoryValidationPassed = formDataValidationService.validateMandatoryFields(((ProjectFormActivity) getActivity()).mSubmittedFields, ((ProjectFormActivity) getActivity()).mFormBridgeMandatoryFields);

        if (!isMandatoryValidationPassed) {
            ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                    (getResources().getString(R.string.MANDATORY_FIELD_NOT_ENTERED), false);
        } else {
            // Bridge present, check with bridge mandatory form fields
            if (((ProjectFormActivity) getActivity()).mFormBridgeKey != null
                    && ((ProjectFormActivity) getActivity()).mFormBridgeMandatoryFields != null
                    && !((ProjectFormActivity) getActivity()).mFormBridgeMandatoryFields.isEmpty()) {

                // Custom buttons mandatory validation successful
                // Initiating regular validation
                initiateValidation();

                if (mSuperFieldMapForValidation != null && mSubmittedFieldsForValidation != null) {
                    ClientValidationResponse clientValidationResponse = formDataValidationService.validateFormSubmission((ProjectFormActivity) getActivity(), mForm.mFormFields,
                            mSuperFieldMapForValidation, mSubmittedFieldsForValidation);
                    if (buttonType == 12) {
                        // For preview button, save the values
                        prepareDataForSubmission(clientValidationResponse, ((ProjectFormActivity) getActivity()).mFormBridgeKey, formButton.mExpandable);
                    } else if (buttonType == 14) {
                        // For save Button - Pop current fragment
                        ((ProjectFormActivity) getActivity()).popCurrentFragment();
                    }
                } else {
                    // Could not gather validation fields
                    ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                            (getResources().getString(R.string.VALIDATION_INITIALIZATION_FAILED), false);
                }
            } else {
                if (mForm.mFormFields != null) {
                    // Initiating validation
                    initiateValidation();

                    // Validating
                    if (mSuperFieldMapForValidation != null && mSubmittedFieldsForValidation != null) {
                        ClientValidationResponse clientValidationResponse = formDataValidationService.validateFormSubmission((ProjectFormActivity) getActivity(), mForm.mFormFields,
                                mSuperFieldMapForValidation, mSubmittedFieldsForValidation);
                        prepareDataForSubmission(clientValidationResponse, ((ProjectFormActivity) getActivity()).mFormBridgeKey, formButton.mExpandable);
                    } else {
                        // Could not gather validation fields
                        ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                                (getResources().getString(R.string.VALIDATION_INITIALIZATION_FAILED), false);
                    }
                }
            }
        }
    }

    /**
     * Gets values from submission array entered by user, and prepare final submission array
     *
     * @param clientValidationResponse
     * @param bridgeKey
     * @param expandable
     */
    private void prepareDataForSubmission(ClientValidationResponse clientValidationResponse, String bridgeKey, ExpandableComponent expandable) {
        if (clientValidationResponse.mIsValid) {
            ((ProjectFormActivity) getActivity()).clearSubmissionArray();
            Form initialForm = ((ProjectFormActivity) getActivity()).getInitialForm();

            if (bridgeKey != null) {
                // Load the submission array with the bridge hierarchy
                pickEnteredValues(mForm, mTag);
            } else {
                if (mForm.mFormId.equals(initialForm.mFormId)) {
                    pickEnteredValues(mForm, mForm.mFormId);
                } else {
                    String tag = ((ProjectFormActivity) getActivity()).createTagToIdentifyFields();
                    pickEnteredValues(mForm, tag);
                }
            }
            expandableItemClicked(expandable, null, mTag);

        } else {
            Toast.makeText(getActivity(), clientValidationResponse.mMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initiate validation :
     * 1. Create a map with all the values that are coming from the project list
     * and also user submitted values if any.
     * 2. All the submitted fields in a map to check if the mandatory fields have
     * been entered by the user.
     * 3. Adding time related data fields to the map.
     *
     * @throws JSONException
     */
    private void initiateValidation() throws JSONException {

        mSuperFieldMapForValidation = new HashMap<>();

        // Add project updation timestamp
        long insertionTimestamp = ((ProjectFormActivity) getActivity()).mProjectFormInitializeTimestamp;

        mSuperFieldMapForValidation.put(Constants.SPEL_PROJECT_TS, String.valueOf(insertionTimestamp));

        long form_submit_time = (insertionTimestamp - SPELExpressionValidator.getStartOfDay(insertionTimestamp)) / 1000;
        mSuperFieldMapForValidation.put(Constants.SPEL_PROJECT_TIME_IN_SECONDS, String.valueOf(form_submit_time));

        // Get all the project list values in the superFieldMap
        if (mProject.mFields != null) {
            for (ProjectListFieldModel projectListFieldModel : mProject.mFields) {
                mSuperFieldMapForValidation.put(projectListFieldModel.mIdentifier,
                        projectListFieldModel.mProjectListFieldValue.mValue);
            }
        }

        // Map of submitted fields to check for submission of mandatory fields
        mSubmittedFieldsForValidation = new HashMap<>();
        mSubmittedFieldsForValidation.putAll(((ProjectFormActivity) getActivity()).mSubmittedFields);

        /** Check if any values are submitted by the user, and if present,
         replace project values with the user submitted values in the superFieldMap **/
        for (Map.Entry<String, JSONObject> entry : mSubmittedFieldsForValidation.entrySet()) {
            String key = entry.getKey();
            JSONObject valueObject = entry.getValue();
            String[] keys = key.split("#");
            String finalKey = getKeyName(keys[keys.length - 1]);
            if (finalKey != null) {
                if (mLatestLastFieldValuesMapSubmittedByUser.containsKey(finalKey)) {
                    String value = mLatestLastFieldValuesMapSubmittedByUser.get(finalKey).getValue();
                    if (value != null && !value.isEmpty()) {
                        mSuperFieldMapForValidation.put(finalKey, value);
                    } else {
                        mSuperFieldMapForValidation.put(finalKey, null);
                    }
                }

//                if (!mSuperFieldMapForValidation.containsKey(finalKey)) {
//                    Utils.getInstance().showLog("VALIDATIONEXCEPTION",
//                            "Project list contains field that is not present in the form");
//                } else {
                    if (valueObject != null && valueObject.getString("val") != null) {
                        String value = valueObject.getString("val");
                        if (value.isEmpty()) value = null;
                        mSuperFieldMapForValidation.put(finalKey, value);
//                    }
                }
            }
        }
    }

    /**
     * Submits the form data (Online and Offline)
     */
    private void submitForm(FormButton formButton) {

        // Show progress bar and disable UI
        ((ProjectFormActivity) getActivity()).showProgressBar();
        ((ProjectFormActivity) getActivity()).disableUI();
        ((ProjectFormActivity) getActivity()).allowBackPress = false;
        try {

            // deleting media from storage and db (removed by the user)
            ((ProjectFormActivity) getActivity()).deletedMediaFromDBAndStorage();

            addExtraUserInfoToSubmissionArray();

            String appId = ((ProjectFormActivity) getActivity()).mAppId;

            // Creating ContentValues for inserting into DB
            ContentValues values = mDBObjectCreationUtils.createFormSubmissionEntry(((ProjectFormActivity) getActivity())
                            .getSubmissionArray(), formButton, appId, ((ProjectFormActivity) getActivity()).mProjectSpecificUpdateForm.mProjectFormId,
                    ((ProjectFormActivity) getActivity()).mProjectSpecificUpdateForm.mMetaDataInstanceId, mProject);

            if(((ProjectFormActivity) getActivity()).mForm_action_type.equals(Constants.INSERT_FORM_KEY) && mProject.mState.equals("New")) {
                ProjectTypeModel projectTypeModel =  UAAppContext.getInstance().getProjectTypeModel(appId);
                List<String>filteringAttributes = new ArrayList<>();
                List<String> groupingAttributes = new ArrayList<>();
                if(projectTypeModel != null) {
                   filteringAttributes = projectTypeModel.mFilteringAttributes != null ? projectTypeModel.mFilteringAttributes : new ArrayList<>();
                   groupingAttributes = projectTypeModel.mGroupingAttributes != null ? projectTypeModel.mGroupingAttributes : new ArrayList<>();
                }
               ContentValues newProject =  mDBObjectCreationUtils.addNewProject(appId, UAAppContext.getInstance().getUserID(),mProject, filteringAttributes, groupingAttributes);
               UnifiedAppDBHelper dbHelper = UAAppContext.getInstance().getDBHelper();
                dbHelper.addOrUpdateToProjectTableWithTS(UnifiedAppDbContract.ProjectTableEntry.TABLE_PROJECT,newProject );
               // ProjectList projectList = UAAppContext.getInstance().getProjectList();
               // projectList.mProjects.add(mProject);
               // UAAppContext.getInstance().setProjectList(projectList);
               // UAAppContext.getInstance().setProjectListCache(projectList.mProjects);
            }
            Utils.logInfo(LogTags.PROJECT_SUBMIT, "Content Values created for project submission -- " + values.toString());

            // List of UUIDs with longitude and latitude that are part of this submission
            List<String> mediaUUIDWithLatLong = mDBObjectCreationUtils.getMediaUuidsFromSubmission(((ProjectFormActivity) getActivity())
                    .getSubmissionArray());

            // List of UUIDs that are part of this submission
            List<String> mediaUUIDList = Utils.getImageUUIDList(mediaUUIDWithLatLong);

            for (String uuid : mediaUUIDList) {
                Utils.logInfo(LogTags.PROJECT_SUBMIT, "Media Uuid for project submission -- " + uuid);
            }

            Utils.logInfo(LogTags.PROJECT_SUBMIT, "XXXX: RealTime Project submission from FORM -- ");

            ProjectSubmissionThread pst = new ProjectSubmissionThread(values, mediaUUIDList, (ProjectFormActivity) getActivity());
            pst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (JSONException e) {
            // JSON error while parsing mediaUUids from the submission array
            Utils.logError(LogTags.PROJECT_SUBMIT, "JSON error while parsing mediaUUids " +
                    "from the submission array", e);
            ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity(Constants.SUBMISSION_ERROR, false);
        }
    }

    // adding userinfo into SubmissionArray
    private void addExtraUserInfoToSubmissionArray() throws JSONException {
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(Constants.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);

        boolean hasUserData = sharedPreferences.getBoolean(Constants.ON_STARTUP_DATA_EXISTS, false);
        if (hasUserData) {
            String jsonStr = sharedPreferences.getString(Constants.ON_STARTUP_DATA, "");
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.ON_STARTUP_DATA_ARRAY);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                ((ProjectFormActivity) getActivity()).addToSubmissionArray(jsonObject1);
            }
        }
    }

    /**
     * Starts camera activity
     */
    public void startCameraActivity(String tag, String datatype, String uiType, int max, String key,
                                    String lat, String lon, List<String> uuids, GpsValidation gpsValidation) {
        if (((UnifiedAppApplication) getActivity().getApplicationContext()).mFusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                ((UnifiedAppApplication) getActivity().getApplicationContext()).mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    ((ProjectFormActivity) getActivity()).mUserLatitude = location.getLatitude();
                                    ((ProjectFormActivity) getActivity()).mUserLongitude = location.getLongitude();
                                    ((ProjectFormActivity) getActivity()).mAccuracy = location.getAccuracy();

                                } else {
                                }
                            }
                        });
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    ((UnifiedAppApplication) getActivity().getApplication()).initializeLocation();
                    ((UnifiedAppApplication) getActivity().getApplication()).startLocationUpdates();
                }
            } else {
                ((UnifiedAppApplication) getActivity().getApplication()).initializeLocation();
                ((UnifiedAppApplication) getActivity().getApplication()).startLocationUpdates();
            }
        }
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(getContext(), NewCamera2Activity.class);
        } else {
            intent = new Intent(getContext(), CameraActivity.class);
        }
        intent.putExtra("tag", tag);
        intent.putExtra("datatype", datatype);
        intent.putExtra("initialts", ((ProjectFormActivity) getActivity()).mProjectFormInitializeTimestamp);
        intent.putExtra("projectId", mProject.mProjectId);
        intent.putExtra("max", max);
        intent.putExtra("key", key);
        intent.putExtra("appId", ((ProjectFormActivity) getActivity()).mAppId);
        intent.putExtra("userId", UAAppContext.getInstance().getUserID());
        intent.putExtra(Constants.LATITUDE_KEY, lat);
        intent.putExtra(Constants.LONGITUDE_KEY, lon);
        intent.putExtra("uitype", uiType);
        if (gpsValidation != null) {
            // Have to perform validation
            intent.putExtra("gps_validation", gpsValidation);
        }
        HashMap<String, String> submittedFieldsMap = convertJSONObjectToStringInMap(((ProjectFormActivity) getActivity()).mSubmittedFields);
        intent.putExtra("submittedFields", submittedFieldsMap);

        intent.putStringArrayListExtra("uuids", (ArrayList<String>) uuids);
        startActivityForResult(intent, Constants.FORM_GET_IMAGES);
    }

    /**
     * Start VideoCaptureActivity
     **/
    public void startVideoCaptureActivity(String tag, String datatype, int max, String key) {

        Intent intent;
        if (Build.VERSION.SDK_INT >= 21) {
            intent = new Intent(getContext(), LollipopVideoCaptureActivity.class);
        } else {
            intent = new Intent(getContext(), VideoCaptureActivity.class);
        }
        intent.putExtra("tag", tag);
        intent.putExtra("datatype", datatype);
        intent.putExtra("initialts", ((ProjectFormActivity) getActivity()).mProjectFormInitializeTimestamp);
        intent.putExtra("projectId", mProject.mProjectId);
        intent.putExtra("max", max);
        intent.putExtra("key", key);
        intent.putExtra("appId", ((ProjectFormActivity) getActivity()).mAppId);
        intent.putExtra("userId", UAAppContext.getInstance().getUserID());

        startActivityForResult(intent, Constants.FORM_RECORD_VIDEO);
    }

    /**
     * Creates thumbnails to show video previews on the video UI element
     */
    private void createVideoThumbnails(FormMedia formVideo) {
        ImageView imageView = new ImageView(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.dimen_60dp)
                , ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(8, 0, 8, 0);
        imageView.setLayoutParams(params);
        imageView.setPadding(16, 16, 16, 16);
        imageView.setImageResource(R.drawable.play_video);
        imageView.setAdjustViewBounds(true);
        mCurrentVideoLinearLayout.addView(imageView, 0);
    }

    private void pickEnteredValues(Form form, String tag) {
        // Iterating through form fields
        if (form.mFormFields == null || form.mFormFields.isEmpty()) {
            return;
        }

        try {
            for (FormField formField : form.mFormFields) {
                String key = formField.mIdentifier;
                String keyForMap = "";
                if (tag != null)
                    keyForMap = tag + "#" + key;
                switch (formField.mUiType) {
                    case "timepicker":
                        JSONObject fundamentalTimeObject = ((ProjectFormActivity) getActivity()).getSubmittedField(keyForMap);
                        String fundamentalTimeValue = null;
                        if (fundamentalTimeObject != null) {
                            fundamentalTimeValue = fundamentalTimeObject.getString("val");
                            if (fundamentalTimeValue != null && !fundamentalTimeValue.isEmpty()) {
//                                String timeInSeconds = Utils.getInstance().changeTimeToSeconds(fundamentalTimeValue);
                                fundamentalTimeObject.put("val", fundamentalTimeValue);
                                ((ProjectFormActivity) getActivity()).addToSubmissionArray(fundamentalTimeObject);
                            }
                        }
                        break;

                    case "date":
                    case "textbox":
                    case "edittext":
                    case "geotag":
                    case "geotagimage":
                    case "geotagimagefused":
                    case "image":
                    case "video":
                        JSONObject fundamentalObject = ((ProjectFormActivity) getActivity()).getSubmittedField(keyForMap);
                        String fundamentalValue = null;
                        if (fundamentalObject != null) {

                            fundamentalValue = fundamentalObject.getString("val");

                            if (fundamentalValue != null && !fundamentalValue.isEmpty()) {
                                ((ProjectFormActivity) getActivity()).addToSubmissionArray(fundamentalObject);
                            }
                        }

                        break;

                    case "textview":
                        JSONObject textViewJsonObject = ((ProjectFormActivity) getActivity()).getSubmittedField(keyForMap);
                        String textViewValue = null;
                        if (textViewJsonObject != null) {

                            textViewValue = textViewJsonObject.getString("val");

                            if (textViewValue != null && !textViewValue.isEmpty()) {
                                ((ProjectFormActivity) getActivity()).addToSubmissionArray(textViewJsonObject);
                            }
                        }
                        if (formField.mExpandable != null) {
                            pickEnteredValues(((ProjectFormActivity) getActivity())
                                            .getFormFromId(formField.mExpandable.mSubForm),
                                    tag + "#" + formField.mExpandable.mSubForm);
                        }
                        break;

                    case "dropdown":
                    case "radio":
                    case "checkbox":
                    case "toggle":

                        JSONObject jsonObject = ((ProjectFormActivity) getActivity()).getSubmittedField(keyForMap);
                        if (jsonObject == null || jsonObject.getString("val") == null) {
                            break;
                        }
                        String valueSelected = jsonObject.getString("val");
                        MultipleValues value = null;
                        for (MultipleValues val : formField.mMultipleValues) {
                            if (valueSelected.equals(val.mValue)) {
                                value = val;
                                break;
                            }
                        }

                        if (value != null) {
                            if (value.mExpandable != null) {
                                ((ProjectFormActivity) getActivity()).addToSubmissionArray(
                                        ((ProjectFormActivity) getActivity()).getSubmittedField(keyForMap));
                                pickEnteredValues(((ProjectFormActivity) getActivity())
                                                .getFormFromId(value.mExpandable.mSubForm),
                                        tag + "#" + value.mExpandable.mSubForm);
                            } else {
                                ((ProjectFormActivity) getActivity()).addToSubmissionArray(
                                        ((ProjectFormActivity) getActivity()).getSubmittedField(keyForMap));
                            }
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Any expandable item, when clicked, has to open in the same page, or open a dialog,
     * or open in a pop up, or go to a new fragment
     */
    private void expandableItemClicked(ExpandableComponent expandable, LinearLayout root, String tag) {

        switch (expandable.mType) {
            case 12: // Preview Button
            case 11: // Edit Button
            case 2: // Pop Up
            case 0: // New Page
                ((ProjectFormActivity) getActivity()).addNewFormFragment(expandable.mSubForm, tag);
                break;
            case 1:  // Same page, use linear layout in the function parameters, and add form fields to it
                Form subform = ((ProjectFormActivity) getActivity()).getFormFromId(expandable.mSubForm);
                if (subform != null) {
                    // Remove subform, if added before
                    int childCount = root.getChildCount();
                    if (childCount > 2) {
                        // Subform has been added before, remove children after 2
//                        for (int i=2; i<childCount; i++) {
//                            root.removeViewAt(i);
//                        }
                        while (root.getChildCount() > 2) {
                            root.removeViewAt(root.getChildCount() - 1);
                        }
                    }
                    // Loading form fields
                    Utils.logInfo("LOADFORMFIELDS", tag + "#" + subform.mFormId);
                    loadFormFields(subform.mFormFields, root, tag + "#" + subform.mFormId);
                } else {
                    ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                            (getResources().getString(R.string.SOMETHING_WENT_WRONG), false);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_GEOTAG_PIN_DROP:
                if (resultCode == Activity.RESULT_OK) {
                    String latitude = data.getStringExtra(Constants.LATITUDE_KEY);
                    String longitude = data.getStringExtra(Constants.LONGITUDE_KEY);
                    String latLon = "Latitude : " + latitude + "\nLongitude : " + longitude;
//                    mCurrentGeotagTextviewDisplayed.setText(latLon);
                    mCurrentGeotagTextview.setText(latitude + "," + longitude);
                } else {
                    ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                            (getResources().getString(R.string.GEOTAG_WASNT_SET), false);
                }
                break;

            case Constants.FORM_GET_IMAGES:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    List<FormMedia> formImages = extras.getParcelableArrayList("formImages");
                    mCurrentImageUuids.setText("");
                    String uuids = extras.getString("imageuuids");
                    mCurrentImageUuids.setText(uuids);
                    mCurrentImageLinearLayout.removeAllViews();

                    if (formImages != null && !formImages.isEmpty()) {
                        for (int i = formImages.size() - 1; i >= 0; i--) {

                            FormMedia formImage = formImages.get(i);

                            if (formImage != null) {
                                //getting user authentication token 
                                String user_token_preference_key=UAAppContext.getInstance().getToken();
                                // Create thumbnails
                                createNewImageThumbnail(formImage, mCurrentImageLinearLayout, mCurrentImageUuids);

                                Map<String, String> additional_prop_map = formImage.getAdditionalProps();
                                String ext_proj_id = mProject.mExtProjectId;
                                additional_prop_map.put("ext_proj_id", ext_proj_id);
                                // Add current token value of user
                                additional_prop_map.put(Constants.USER_TOKEN, user_token_preference_key);
                                formImage.setAdditionalProps(additional_prop_map);

                                // Adding image to Form database
                                if (UAAppContext.getInstance().getDBHelper().getFormMedia(formImage.getmUUID(), formImage.getmAppId(), formImage.getmUserId()) == null) {
                                    mDBObjectCreationUtils.saveImageToFormDatabase(UAAppContext.getInstance().getDBHelper(),
                                            formImage);
                                }
                            }
                        }
                    }
                    int count = UAAppContext.getInstance().getDBHelper().getFormMediaCountForUser(UAAppContext.getInstance().getUserID());
                    Log.v("COUNT OF MEDIA ", String.valueOf(count));

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (mCurrentImageUuids != null) {
                        String uuids = mCurrentImageUuids.getText().toString();
                        List<String> prevImgs;
                        ArrayList<String> prevImgsUUIDsWithLonLat = new ArrayList<>();
                        prevImgsUUIDsWithLonLat.addAll(Arrays.asList(uuids.split("\\s*,\\s*")));
                        prevImgs = Utils.getImageUUIDList(prevImgsUUIDsWithLonLat);
                        mCurrentImageLinearLayout.removeAllViews();
                        int count = 0;
                        for (String uuid : prevImgs) {
                            FormMedia formImage = UAAppContext.getInstance().getDBHelper().getFormMedia(uuid, ((ProjectFormActivity) getActivity()).mAppId, UAAppContext.getInstance().getUserID());
                            if (formImage != null) {
                                createNewImageThumbnail(formImage, mCurrentImageLinearLayout, mCurrentImageUuids);
                                count = 1;
                            }
                        }

                        if (count == 0) {
                            mCurrentImageUuids.setText("");
                            ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                                    (getResources().getString(R.string.NO_IMAGES_TAKEN), false);
                        }
                    }
                } else {
                    if (mCurrentImageUuids != null) {
                        String uuids = mCurrentImageUuids.getText().toString();
                        List<String> prevImgs;
                        ArrayList<String> prevImgsUUIDsWithLonLat = new ArrayList<>();
                        prevImgsUUIDsWithLonLat.addAll(Arrays.asList(uuids.split("\\s*,\\s*")));
                        prevImgs = Utils.getImageUUIDList(prevImgsUUIDsWithLonLat);
                        mCurrentImageLinearLayout.removeAllViews();

                        for (String uuid : prevImgs) {
                            FormMedia formImage = UAAppContext.getInstance().getDBHelper()
                                    .getFormMedia(uuid, ((ProjectFormActivity) getActivity()).mAppId
                                            , UAAppContext.getInstance().getUserID());
                            if (formImage != null) {
//                            createImageThumbnails(formImage);
                                createNewImageThumbnail(formImage, mCurrentImageLinearLayout, mCurrentImageUuids);
                            }
                        }
                    }
                    ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                            (getResources().getString(R.string.SOMETHING_WENT_WRONG), false);

                }
                break;

            case Constants.FORM_RECORD_VIDEO:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();

                    ArrayList<FormMedia> formVideos = extras.getParcelableArrayList("formVideos");
//                    String uuids = extras.getString("videouuids");
                    // TODO: Remove hardcoding in case of multiple videos
                    mCurrentVideoUuids.setText(formVideos.get(0).getmUUID());

                    for (FormMedia formVideo : formVideos) {
                        // Create thumbnails
                        createVideoThumbnails(formVideo);

                        Map<String, String> additional_prop_map = formVideo.getAdditionalProps();
                        String ext_proj_id = mProject.mExtProjectId;
                        additional_prop_map.put("ext_proj_id", ext_proj_id);
                        formVideo.setAdditionalProps(additional_prop_map);

                        // Adding image to Form database
                        mDBObjectCreationUtils.saveImageToFormDatabase(UAAppContext.getInstance().getDBHelper(),
                                formVideo);
                    }
                    // Create thumbnails
//                    createVideoThumbnails(uuids);

                    // Adding image to Form database
//                    saveVideoToFormDatabase(((ProjectFormActivity) getActivity()).mDBHelper,
//                            formVideo);

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    mCurrentVideoUuids.setText("");
                    ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                            (getResources().getString(R.string.NO_VIDEO_TAKEN), false);
                } else {
                    mCurrentVideoUuids.setText("");
                    ((ProjectFormActivity) getActivity()).showErrorMessageAndFinishActivity
                            (getResources().getString(R.string.SOMETHING_WENT_WRONG), false);
                }
                break;
        }
    }

    /*private void createDialog(String message, boolean shouldFinishActivity, boolean redirectToLogin) {
        // create a Dialog component
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.project_submission_alert_dialog);

        TextView txt = (TextView) dialog.findViewById(R.id.project_submission_message);

        txt.setText(message);

        Button dialogButton = (Button) dialog.findViewById(R.id.project_submission_close);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((ProjectFormActivity) getActivity()).hideProgressBar();
                dialog.dismiss();
                ((ProjectFormActivity) getActivity()).dismissDialog(((ProjectFormActivity) getActivity()).DIALOG_LOADING);
                if (shouldFinishActivity) {
                    ((ProjectFormActivity) getActivity()).finish();
                }
                if (redirectToLogin) {
                    ((ProjectFormActivity) getActivity()).moveToLoginScreen();
                }
            }
        });

        dialog.show();
    }
*/
    private void createNewImageThumbnail(FormMedia formImage, LinearLayout currentImageLayout, TextView imageUuids) {

        OpenCameraImageThumbnail imgThumbnail = new OpenCameraImageThumbnail(context);
        ImageView imageView = imgThumbnail.getImgPhoto();

        File imgFile = new File(formImage.getLocalPath());
        if (imgFile != null) {
            Picasso.get().load(imgFile)
                    .resize(150, 150)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opening activity that shows the image
                Intent intent = new Intent(getActivity(), ImagePreviewActivity.class);
                intent.putExtra("image_path", formImage.getLocalPath());
                startActivity(intent);
            }
        });

        ImageButton imgButton = imgThumbnail.getBtnClose();
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uuids = imageUuids.getText().toString();
                ArrayList<String> prevImgsUUIDsWithLonLat = new ArrayList<>();
                prevImgsUUIDsWithLonLat.addAll(Arrays.asList(uuids.split("\\s*,\\s*")));
                String toChnge = "";
                if(formImage.ismHasGeotag()) {
                    toChnge = formImage.getmUUID() + Constants.IMAGE_UUID_LONG_LAT_SEPARATOR + formImage.getLongitude()
                            + Constants.IMAGE_UUID_LONG_LAT_SEPARATOR + formImage.getLatitude();
                } else{
                    toChnge = formImage.getmUUID();
                }
                prevImgsUUIDsWithLonLat.remove(toChnge);

                imageUuids.setText(android.text.TextUtils.join(",", prevImgsUUIDsWithLonLat));
                currentImageLayout.removeView(imgThumbnail);
                // Adding this media UUID to deleted media for removal from db and storage
                ((ProjectFormActivity) getActivity()).addToDeletedMediaUUIDs(formImage.getmUUID());

            }
        });

        currentImageLayout.addView(imgThumbnail, 0);
        mCurrentImageLinearLayout = currentImageLayout;
        mCurrentImageUuids = imageUuids;
    }

    Map<String, String> getKeyToValueMapForHeaders(ArrayList<ProjectListFieldModel> projectListHeaders, Map<String, LatestFieldValue> latestLastFieldValuesMapSubmittedByUser) {
        Map<String, String> keyToValueMap = new HashMap<>();
        if(projectListHeaders == null){
            projectListHeaders =  new ArrayList<>();
        }
        for (ProjectListFieldModel projectListHeader : projectListHeaders) {
            if (projectListHeader != null || projectListHeader.mIdentifier != null) {
                if (projectListHeader != null && projectListHeader.mProjectListFieldValue != null && projectListHeader.mProjectListFieldValue.mValue != null && !projectListHeader.mProjectListFieldValue.mValue.isEmpty()) {
                    keyToValueMap.put(projectListHeader.mIdentifier, projectListHeader.mProjectListFieldValue.mValue);
                }
            }

        }
        for (String submittedKey : latestLastFieldValuesMapSubmittedByUser.keySet()) {
            String key = getKeyName(submittedKey);
            if (latestLastFieldValuesMapSubmittedByUser.get(submittedKey).getValue() != null) {
                keyToValueMap.put(key, latestLastFieldValuesMapSubmittedByUser.get(submittedKey).getValue());
            }
        }
        return keyToValueMap;
    }

    public static class DatePickerFragment
            extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private Activity mActivity;
        private TextView mDateText;
        private SelectableDates mSelectableDates;

        public DatePickerFragment() {
        }

        @SuppressLint("ValidFragment")
        public DatePickerFragment(Activity activity, TextView dateText, SelectableDates selectableDates) {
            this.mActivity = activity;
            this.mDateText = dateText;
            this.mSelectableDates = selectableDates;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog;
            if (mSelectableDates.mSelect != null) {
                Utils.getInstance().showLog("DATE : ", day + mSelectableDates.mSelect);
                datePickerDialog = new DatePickerDialog(mActivity, this
                        , year, month, day + Integer.parseInt(mSelectableDates.mSelect));
            } else {
                Utils.getInstance().showLog("DATE : ", day + "");
                datePickerDialog = new DatePickerDialog(mActivity, this
                        , year, month, day);
            }

            if (mSelectableDates.mPast != null && !mSelectableDates.mPast.isEmpty()) {
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() +
                        (Integer.parseInt(mSelectableDates.mPast) * 86400000));
            }
            if (mSelectableDates.mFuture != null && !mSelectableDates.mFuture.isEmpty()) {
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() +
                        (Integer.parseInt(mSelectableDates.mFuture) * 86400000));
            }
            return datePickerDialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            mDateText.setText(i2 + "/" + (i1 + 1) + "/" + (i));
        }
    }

    private void showTransactionLog(View view, String key){

        if(Utils.getInstance().isOnline((ProjectFormActivity)getActivity())) {
            TransactionLogAsyncTask transactionLogAsyncTask = new TransactionLogAsyncTask(((ProjectFormActivity)getActivity()).mAppId,
                    mProject.mProjectId, key, (ProjectFormActivity)getActivity(), view);
            transactionLogAsyncTask.execute();
        } else {
            Toast.makeText((ProjectFormActivity) getActivity(), getResources().getString(R.string.CHECK_INTERNET_CONNECTION), Toast.LENGTH_SHORT).show();
        }
    }
    public void autoPopulateDependentFields(String sourceKey) {
        ((ProjectFormActivity)getActivity()).autoPopulateDependentFields(sourceKey);

    }
     public void autoPopulateField(String targetKey) {
         ((ProjectFormActivity)getActivity()).autoPopulateField(targetKey);
     }

     public HashMap<String, String> convertJSONObjectToStringInMap(Map<String, JSONObject> map){
         HashMap<String, String> convertedMap = new HashMap<>();
         for (String key : map.keySet()){
             JSONObject jsonObject = map.get(key);
             if (jsonObject != null){
                 String jsonString = jsonObject.toString();
                 convertedMap.put(key, jsonString);
             }
         }
         return convertedMap;
     }
    public String getLabel(String label) {
        if(label != null && !label.isEmpty()) {
            label = StringUtils.getTranslatedString(label);
        }
        return label;
     }
}