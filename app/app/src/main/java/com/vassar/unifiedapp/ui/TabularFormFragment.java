package com.vassar.unifiedapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;
import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.api.ArrayDataSubmissionThread;
import com.vassar.unifiedapp.application.UnifiedAppApplication;
import com.vassar.unifiedapp.camera2.NewCamera2Activity;
import com.vassar.unifiedapp.context.UAAppContext;
import com.vassar.unifiedapp.err.UAAppErrorCodes;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AutoPopulateConfig;
import com.vassar.unifiedapp.model.ClientValidationResponse;
import com.vassar.unifiedapp.model.ExpandableComponent;
import com.vassar.unifiedapp.model.Form;
import com.vassar.unifiedapp.model.FormButton;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.FormMedia;
import com.vassar.unifiedapp.model.GpsValidation;
import com.vassar.unifiedapp.model.Header;
import com.vassar.unifiedapp.model.MultipleValues;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.SelectableDates;
import com.vassar.unifiedapp.model.TableRow;
import com.vassar.unifiedapp.newcamera.CameraActivity;
import com.vassar.unifiedapp.utils.AppValidationService;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.CustomExpressionEvaluator;
import com.vassar.unifiedapp.utils.DBObjectCreationUtils;
import com.vassar.unifiedapp.utils.StringUtils;
import com.vassar.unifiedapp.utils.Utils;
import com.vassar.unifiedapp.view.MultiLineRadioGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */

public class TabularFormFragment extends Fragment {

    private Project mProject;
    private Form mForm;
    private LayoutInflater mInflater;

    private LinearLayout mCurrentImageLinearLayout;
    private TextView mCurrentImageUuids;

    private DBObjectCreationUtils mDBObjectCreationUtils;

    private Map<String, String> mAdditionalInfo = new HashMap<>();

    private Map<String, LinkedHashMap<Integer, Map<String, String>>> addendumKeyToValueMap = new HashMap<>();
    private Map<String, Integer> addendumFieldKeyToLastIndexMap = new HashMap<>();

    private TextView rowAdditionTextButton;

    private boolean submitClicked = false;
    private int mCountForId = 0;

    private String mTag;

    private Map<Integer, Map<String, ComputedExpression>> indexTokeyToComputedDataMap = new HashMap<>();

    private Map<String, LinkedHashMap<Integer, Map<String, String>>> mUserRenderedTablevalues = new HashMap<>();
    private Map<String, String> mUserRenderedValues = new HashMap<>();

    private Map<String, Object> keyToFieldObjectMap = new HashMap<>();

    public TabularFormFragment() {
    }

    @SuppressLint("ValidFragment")
    public TabularFormFragment(Form form, Project project, Map<String, String> additionalInfo, String tag) {
        // Required empty public constructor
        mForm = form;
        mProject = project;
        mDBObjectCreationUtils = new DBObjectCreationUtils();
        if (additionalInfo != null) {
            mAdditionalInfo = additionalInfo;
        }
        mTag = tag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tabular_form, container, false);

        // Maintain previous copy of user entered values
        Map<String, String> userEnteredValues = ((TabularFormActivity) getActivity()).getmUserEnteredValues();
        Map<String, LinkedHashMap<Integer, Map<String, String>>> userEnteredTableValues =
                ((TabularFormActivity) getActivity()).getUserEnteredTableValues();

        for (String key : userEnteredValues.keySet()){
            String value = userEnteredValues.get(key);
            mUserRenderedValues.put(key, value);
        }
        for (String key : userEnteredTableValues.keySet()){

            LinkedHashMap<Integer, Map<String, String>> linkedHashMap = new LinkedHashMap<>();
            LinkedHashMap<Integer, Map<String, String>> value = userEnteredTableValues.get(key);
            for (Integer key1 : value.keySet()){
                Map<String, String> internalMap = new HashMap<>();
                internalMap.putAll(value.get(key1));
                linkedHashMap.put(key1, internalMap);
            }
            mUserRenderedTablevalues.put(key, linkedHashMap);
        }

        mInflater = inflater;

        LinearLayout fieldsLayout = view.findViewById(R.id.tabular_form_fields_layout);
        LinearLayout headerLayout = view.findViewById(R.id.tabular_form_header_layout);

        LinearLayout buttonsLayout = view.findViewById(R.id.tabular_form_buttons_layout);

        if (mForm != null && mForm.mFormFields != null && mForm.mFormFields.size() > 0) {
            renderFormFields(mForm.mFormFields, fieldsLayout, mTag);
        }
        if (mForm != null && mForm.mName != null && !mForm.mName.isEmpty()) {
            renderFormName(mForm.mName, headerLayout);
        }
        if (mForm != null && mForm.mHeaders != null && mForm.mHeaders.size() > 0) {
            renderFormHeaders(mForm.mHeaders, headerLayout, null);
        }

        if (mForm != null && mForm.mFormButtons != null && mForm.mFormButtons.size() > 0) {
            renderFormButtons(mForm.mFormButtons, buttonsLayout);
        }

        return view;
    }

    private void renderFormName(String mFormName, LinearLayout layout) {

        TextView formNameView = new TextView(getActivity());
        formNameView.setText(StringUtils.getTranslatedString(mFormName));
        formNameView.setTextSize(22);
        formNameView.setTextColor(getResources().getColor(R.color.table_heading_text_color));
        formNameView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        formNameView.setTypeface(null, Typeface.BOLD);
        layout.addView(formNameView);
    }

    /* Renders form fields linearly (vertically) into the layout passed as
     the parameter. This function is called recursively in case a sub-form
      needs to be rendered within the same form (expandable type 1) */
    private void renderFormFields(List<FormField> fields, LinearLayout layout, String tag) {

        // Iterating through all the form fields
        for (FormField field : fields) {
            System.out.println("render new page");
            if (field != null && field.mUiType != null) {
                AutoPopulateConfig autoPopulateConfig = field.mAutoPopulateConfig;
                if(autoPopulateConfig != null) {
                    autoPopulateConfig.mTag = tag;
                };
                /* Based on the type of UI component (field) we create views
                and add them to the layout specified in the parameters */
                switch (field.mUiType) {


                    case Constants.TABULAR_FORM_NEW_PAGE_TABLE_FIELD: {
                        // Inflating the table view component
                        View newPageTableView = mInflater.inflate(R.layout.new_page_table_form_field_layout, layout, false);

                        TextView newPageTableHeader = newPageTableView.findViewById(R.id.new_page_table_form_field_label);
                        LinearLayout newPageTableRootLayout = newPageTableView.findViewById(R.id.new_page_table_form_field_linear_layout);
                        TextView newPageTableAddIcon = newPageTableView.findViewById(R.id.new_page_table_add_item_icon);

                        // Heading of the table
                        if (field.mLabel != null) {
                            newPageTableHeader.setText(StringUtils.getTranslatedString(field.mLabel));
                            newPageTableHeader.setTextSize(22);
                            newPageTableHeader.setTextColor(getResources().getColor(R.color.table_heading_text_color));
                            newPageTableHeader.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                newPageTableHeader.setText(newPageTableHeader.getText() + " (" + field.mUOM + ")");
                            }
                        }

                        renderNewPageSubformTableFields(newPageTableRootLayout, field);

                        newPageTableAddIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Launch subform
                                Integer lastIndex = ((TabularFormActivity) getActivity()).newPageTableFieldKeyToLastIndexMap.get(field.mIdentifier);
                                if (lastIndex != null) {
                                    expandableItemClicked(field.mExpandable, null , field.mIdentifier + "##" + lastIndex, null);
                                }
                            }
                        });

                        layout.addView(newPageTableView);
                        }
                        break;

                    // Table UI component
                    case Constants.TABULAR_FORM_TABLE_FIELD: {

                        // Inflating the table view component
                        View tableView = mInflater.inflate(R.layout.table_form_field_layout, layout, false);

                        TextView tableHeader = tableView.findViewById(R.id.table_form_field_label);
                        LinearLayout tableRootLayout = tableView.findViewById(R.id.table_form_field_linear_layout);

                        // Heading of the table
                        if (field.mLabel != null) {
                            tableHeader.setText(StringUtils.getTranslatedString(field.mLabel));
                            tableHeader.setTextSize(22);
                            tableHeader.setTextColor(getResources().getColor(R.color.table_heading_text_color));
                            if(field.mAligned != null && !field.mAligned.isEmpty()){
                                setAlignment(field.mAligned, tableHeader);
                            } else {
                                tableHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                tableHeader.setText(tableHeader.getText() + " (" + field.mUOM + ")");
                            }
                        }

                        if (field.mTableStructure != null && field.mTableStructure.size() > 0) {

                            for (int i = 0; i < field.mTableStructure.size(); i++) {

                                // Differentiate based on repeat (Rows that need repetition)
                                if (field.mTableStructure.get(i).mRepeat) {

                                    /*
                                     This row needs to fetch data from the JSON array defined by the field key
                                     and based on the number of elements of the JSON array populate the table.

                                     Values for these repeated rows have to be added to the submission
                                     map.

                                     Assumption : Any user editable fields are part of repeated rows only.
                                      */

                                    // Using filtering param to determine if the table needs filtering

                                    String jsonArrayString = ((TabularFormActivity) getActivity()).getValueForKey(field.mIdentifier);
//                                    String jsonArrayString = getValueFromProject(field.mIdentifier);
                                    Utils.logInfo("JSON ARRAY FOR KEY :: " + field.mIdentifier + " VALUE :: " + jsonArrayString);
                                    JSONArray jsonArray = null;
                                    jsonArray = new JSONArray();

                                    try {
                                        if (jsonArrayString != null && !jsonArrayString.isEmpty()) {
                                            jsonArray = new JSONArray(jsonArrayString);
                                        }

                                        boolean haveRowsToShow = false;
                                        if (jsonArray.length() > 0) {
                                            for (int k = 0; k < jsonArray.length(); k++) {
                                                RadioButton radioButtonToChecked = null;
                                                int finalK = k;
                                                int finalI = i;
                                                // For each JSON object, render a row and fetch the values from the object
//                                                JSONObject jsonObject = jsonArray.getJSONObject(k);

                                                LinearLayout rowContainer = new LinearLayout(getActivity());
                                                rowContainer.setOrientation(LinearLayout.VERTICAL);

                                                if (k % 2 == 0) {
                                                    rowContainer.setBackground(getResources()
                                                            .getDrawable(R.drawable.shape_rectangle_blue_grey_edges));
                                                } else {
                                                    rowContainer.setBackground(getResources()
                                                            .getDrawable(R.drawable.shape_rectangular_grey_edges));
                                                }

                                                LinearLayout rowLayout = new LinearLayout(getActivity());
                                                rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                                                rowLayout.setPadding(0, 10, 0, 10);

                                                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams
                                                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                                // Create Horizontal LinearLayout (Row)
//                                                if (field.mTableStructure.get(i) != null && field.mTableStructure.get(i).mWeightSum > 0) {
//                                                    rowLayout.setWeightSum(field.mTableStructure.get(i).mWeightSum);
//                                                }

                                                rowLayout.setLayoutParams(rowParams);
                                                rowContainer.setLayoutParams(rowParams);

                                                // Add components to LinearLayout
                                                if (field.mTableStructure.get(i) != null &&
                                                        field.mTableStructure.get(i).mRowComponents != null &&
                                                        field.mTableStructure.get(i).mRowComponents.size() > 0) {

                                                    int rowCellCount = field.mTableStructure.get(i).mRowComponents.size();

                                                    for (int j = 0; j < rowCellCount; j++) {

                                                        int finalJ = j;
                                                        FormField tableObject = field.mTableStructure.get(i).mRowComponents.get(j);

                                                        LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams
                                                                (0, ViewGroup.LayoutParams.MATCH_PARENT);

                                                        // TODO : Check if these weights are being assigned properly
                                                        if (tableObject.mWeight > 0) {
                                                            cellParams.weight = tableObject.mWeight;
                                                        } else {
                                                            cellParams.weight = 1;
                                                        }

                                                        switch (tableObject.mUiType) {

                                                            case Constants.TABULAR_FORM_TEXT_FIELD:

                                                                TextView textView = new TextView(getActivity());

                                                                if (tableObject.mLabelStyle != null &&
                                                                        tableObject
                                                                                .mLabelStyle.equals(Constants.VIEW_LABEL_STYLE_BOLD)) {
                                                                    textView.setTypeface(null, Typeface.BOLD);
                                                                }

                                                                //            textView.setEllipsize(TextUtils.TruncateAt.END);
                                                                //              textView.setSingleLine(true);

                                                                // Alignment of the textview
                                                                setAlignment(tableObject.mAligned, textView);

                                                                cellParams.setMargins(5, 0, 5, 0);
                                                                textView.setLayoutParams(cellParams);

                                                                String textViewValue = "-";
                                                                FormField individualObject = tableObject;

                                                                if (individualObject.mLabel != null && !individualObject.mLabel.isEmpty()) {
                                                                    // Label is available
                                                                    textViewValue = StringUtils.getTranslatedString(individualObject.mLabel);

                                                                } else {
                                                                    textViewValue = getValueForKey(individualObject.mIdentifier, field.mIdentifier + "##" + finalK);
//                                                                            jsonObject.getString(individualObject.mIdentifier);
                                                                }

                                                                if (textViewValue == null || textViewValue.isEmpty() || textViewValue.equalsIgnoreCase("null")) {
                                                                    textViewValue = "-";
                                                                }
                                                                textView.setText(StringUtils.getTranslatedString(textViewValue));

                                                                // Adding table cell value to the submission map
                                                                ((TabularFormActivity) getActivity()).addToUserEnteredValuesMap(field.mIdentifier,
                                                                        k, individualObject.mIdentifier, textViewValue);

                                                                // TextView with expandale component
                                                                textView.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        if (tableObject.mExpandable != null &&
                                                                                tableObject.mExpandable.mSubForm != null &&
                                                                                !tableObject.mExpandable.mSubForm.isEmpty()) {

                                                                            if (tableObject.mFilteringParam) {
                                                                                ArrayList<String> keyElements = new ArrayList<>();
                                                                                keyElements.addAll(Arrays.asList(tableObject.mIdentifier.split("\\$")));
                                                                                if (keyElements.size() > 0) {
                                                                                    String filterKey = keyElements.get(keyElements.size() - 1);
                                                                                    mAdditionalInfo.put(filterKey, textView.getText().toString());
                                                                                }
                                                                            }

                                                                            // This text field is clickable and loads a sub-form
                                                                            expandableItemClicked(tableObject.mExpandable, layout, tag, mAdditionalInfo);
                                                                        }
                                                                    }
                                                                });
                                                                if (tableObject.mDisplay)
                                                                    rowLayout.addView(textView);

                                                                break;

                                                            case Constants.TABULAR_FORM_RADIO_FIELD:

                                                                if (field.mTableStructure.get(i)
                                                                        .mRowComponents.get(j).mMultipleValues != null &&
                                                                        field.mTableStructure.get(i)
                                                                                .mRowComponents.get(j).mMultipleValues.size() > 0) {

                                                                    MultiLineRadioGroup multiLineRadioGroup = new MultiLineRadioGroup(getActivity());

                                                                    multiLineRadioGroup.setOrientation(LinearLayout.HORIZONTAL);

                                                                    multiLineRadioGroup.setLayoutParams(cellParams);

                                                                    ArrayList<MultipleValues> values = new ArrayList<>();
                                                                    values.addAll(field.mTableStructure.get(i)
                                                                            .mRowComponents.get(j).mMultipleValues);
                                                                    int radioIdCount = 0;
                                                                    Utils.logInfo("JSON OBJECT :: " + jsonArray);
//                                                                    String presentValue = jsonObject.getString(field
//                                                                            .mTableStructure.get(i).mRowComponents.get(j).mIdentifier);
                                                                    String presentValue = getValueForKey(field.mTableStructure.get(i)
                                                                            .mRowComponents.get(j).mIdentifier, field.mIdentifier + "##" + finalK);
                                                                    Utils.logInfo("radioButtonToChecked :: " + field.mTableStructure.get(i)
                                                                            .mRowComponents.get(j).mIdentifier + presentValue);
                                                                    if (presentValue == null || presentValue.isEmpty()) {
                                                                        presentValue = field.mTableStructure.get(i)
                                                                                .mRowComponents.get(j).mDefault;
                                                                    }
//                                                                    RadioButton radioButtonToChecked  = null;
                                                                    for (MultipleValues value : values) {
                                                                        RadioButton radioButton = new RadioButton(getActivity());
                                                                        radioButton.setText(StringUtils.getTranslatedString(value.mValue));
                                                                        multiLineRadioGroup.addView(radioButton);
                                                                        radioButton.setId(radioIdCount);
                                                                        radioIdCount++;
                                                                        Utils.logInfo("radioButton Value :: " + value.mValue);
                                                                        Utils.logInfo("Present Value  : : " + presentValue);
                                                                        if (presentValue != null && !presentValue.isEmpty() && value.mValue.equalsIgnoreCase(presentValue)) {
                                                                            radioButtonToChecked = radioButton;
                                                                            Utils.logInfo("radioButtonToChecked Found:: " + value.mValue);

                                                                        }
                                                                    }


                                                                    multiLineRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                                                        @Override
                                                                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                                                                            String selectedValue = String.valueOf(field.mTableStructure.get(finalI)
                                                                                    .mRowComponents.get(finalJ).mMultipleValues.get(checkedId).mValue);
                                                                            // Adding table cell value to the submission map
                                                                            ((TabularFormActivity) getActivity()).addToUserEnteredValuesMap(field.mIdentifier,
                                                                                    finalK, field.mTableStructure.get(finalI).mRowComponents
                                                                                            .get(finalJ).mIdentifier, selectedValue);

                                                                            if (values.get(checkedId).mExpandable != null) {
                                                                                // Have to render views from the expandable sub-form
                                                                                int childCount = rowContainer.getChildCount();
                                                                                if (childCount > 1) {
                                                                                    while (rowContainer.getChildCount() > 1) {
                                                                                        rowContainer.removeViewAt(rowContainer.getChildCount() - 1);
                                                                                    }
                                                                                }
                                                                                expandableItemClicked(values.get(checkedId).mExpandable,
                                                                                        rowContainer, field.mIdentifier + "##" + finalK, mAdditionalInfo);
                                                                            } else {
                                                                                // Noting to expand
                                                                                int childCount = rowContainer.getChildCount();
                                                                                if (childCount > 1) {
                                                                                    while (rowContainer.getChildCount() > 1) {
                                                                                        rowContainer.removeViewAt(rowContainer.getChildCount() - 1);
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    });

                                                                    rowLayout.addView(multiLineRadioGroup);
                                                                }

                                                                break;
                                                            case Constants.TABULAR_FORM_CHECKBOX : {

                                                                LinearLayout checkBoxLayout = new LinearLayout(getActivity());
                                                                checkBoxLayout.setOrientation(LinearLayout.HORIZONTAL);
                                                                checkBoxLayout.setLayoutParams(cellParams);

                                                                LinearLayout checkBoxGroup = new LinearLayout(getActivity());
                                                                ViewGroup.LayoutParams checkBoxGroupParams = new LinearLayout.LayoutParams(
                                                                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                                checkBoxGroup.setOrientation(LinearLayout.VERTICAL);

                                                                checkBoxGroup.setLayoutParams(checkBoxGroupParams);
                                                                ArrayList<MultipleValues> multipleValues = field.mTableStructure.get(i)
                                                                        .mRowComponents.get(j).mMultipleValues;

                                                                for (int checkBoxValue = 0; checkBoxValue< multipleValues.size(); checkBoxValue++) {
                                                                    CheckBox checkBox = new CheckBox(getActivity());
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                                                            (LinearLayout.LayoutParams.WRAP_CONTENT
                                                                                    , LinearLayout.LayoutParams.WRAP_CONTENT);
                                                                    params.setMargins(0,0,75,0);
                                                                    checkBox.setLayoutParams(params);
                                                                    checkBox.setText(multipleValues.get(checkBoxValue).mValue);
                                                                    checkBox.setTag(field.mIdentifier);

                                                                    checkBox.setOnClickListener( new View.OnClickListener() {
                                                                        public void onClick(View v) {

                                                                            CheckBox cb = (CheckBox) v ;
                                                                            if(cb.isChecked()){
                                                                                for (MultipleValues values : multipleValues) {
                                                                                    if (checkBox.getText().toString().equals(values.mValue)) {
                                                                                        ((TabularFormActivity) getActivity()).addToUserEnteredValuesMap(field.mIdentifier,
                                                                                                finalK, field.mTableStructure.get(finalI).mRowComponents
                                                                                                        .get(finalJ).mIdentifier, values.mValue);

                                                                                        if (values.mExpandable != null) {
                                                                                            expandableItemClicked(values.mExpandable, checkBoxLayout, field.mIdentifier + "##" + finalK, null);
                                                                                        }
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                for (MultipleValues values : multipleValues) {
                                                                                    if (checkBox.getText().toString().equals(values.mValue)) {
                                                                                        ((TabularFormActivity) getActivity()).removeKeyFromUserEnteredTableValues(field.mIdentifier,
                                                                                                finalK, field.mTableStructure.get(finalI).mRowComponents
                                                                                                        .get(finalJ).mIdentifier);
                                                                                        if (values.mExpandable != null) {
                                                                                            expandableItemClicked(values.mExpandable, checkBoxLayout, field.mIdentifier + "##" + finalK, null);
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                    checkBoxGroup.addView(checkBox);
                                                                }
                                                                checkBoxLayout.addView(checkBoxGroup);
                                                                rowLayout.addView(checkBoxLayout);}
                                                            default:
                                                                break;
                                                        }
                                                    }
                                                }
                                                rowContainer.addView(rowLayout);
                                                if (radioButtonToChecked != null) {
                                                    Utils.logInfo("radioButtonToChecked  SET:: " + radioButtonToChecked.getText());
                                                    radioButtonToChecked.setChecked(true);
                                                }

                                                if (field.mTableStructure.get(i).mFilteringKey != null && !field.mTableStructure.get(i).mFilteringKey.isEmpty()) {
                                                    // Filtering key present. Only make those rows visible, that have the filtering key in their JSON
                                                    String filteringKey = field.mTableStructure.get(i).mFilteringKey;
                                                    ArrayList<String> keyElements = new ArrayList<>();
                                                    keyElements.addAll(Arrays.asList(filteringKey.split("\\$")));
                                                    if (keyElements.size() > 0) {
                                                        String additionalInfoKey = keyElements.get(keyElements.size() - 1);
                                                        String filterValue = mAdditionalInfo != null ? mAdditionalInfo.get(additionalInfoKey) : null;
                                                        String cellValue = getValueForKey(filteringKey, field.mIdentifier + "##" + finalK);
                                                        ((TabularFormActivity) getActivity()).addToUserEnteredValuesMap(field.mIdentifier,
                                                                finalK, filteringKey, cellValue);
                                                        if (cellValue == null || filterValue == null || !cellValue.equalsIgnoreCase(filterValue)) {
                                                            rowContainer.setVisibility(View.GONE);
                                                        } else {
                                                            haveRowsToShow = true;
                                                        }
                                                    } else {
                                                        haveRowsToShow = true;
                                                    }

                                                } else {
                                                    haveRowsToShow = true;
                                                }

                                                tableRootLayout.addView(rowContainer);
                                            }
                                        }
                                        if (!haveRowsToShow) {
                                            TextView noData = new TextView(getActivity());
                                            noData.setText(getResources().getString(R.string.NO_DATA_AVAILABLE));
                                            noData.setTextColor(Color.rgb(255, 0, 0));
                                            noData.setTextSize(20);
                                            noData.setGravity(Gravity.CENTER_HORIZONTAL);
                                            tableRootLayout.addView(noData);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {

                                    /*
                                     This row is NOT a repeat row that needs fetching data from the JSON array.

                                     We do not need to enter any values into the submission map, as we are already
                                     doing that for the repeated table entries.

                                     Assumption : Any user editable fields are part of repeated rows only
                                      */

                                    LinearLayout rowLayout = new LinearLayout(getActivity());
                                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                                    rowLayout.setMinimumHeight(100);

                                    rowLayout.setBackground(getResources().getDrawable(R.drawable.shape_rectangular_grey_edges));

                                    if (field.mTableStructure.get(i).mBackgroundColor != null) {
                                        rowLayout.getBackground().setColorFilter(Color.parseColor(field
                                                .mTableStructure.get(i).mBackgroundColor), PorterDuff.Mode.DARKEN);
                                    }

                                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams
                                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                    rowLayout.setLayoutParams(rowParams);

                                    LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams
                                            (0, ViewGroup.LayoutParams.MATCH_PARENT);

                                    // Create Horizontal LinearLayout (Row)
//                                    if (field.mTableStructure.get(i) != null && field.mTableStructure.get(i).mWeightSum > 0) {
//                                        rowLayout.setWeightSum(field.mTableStructure.get(i).mWeightSum);
//                                    }

                                    // Add components to LinearLayout
                                    if (field.mTableStructure.get(i) != null
                                            && field.mTableStructure.get(i).mRowComponents != null
                                            && field.mTableStructure.get(i).mRowComponents.size() > 0) {

                                        for (int j = 0; j < field.mTableStructure.get(i).mRowComponents.size(); j++) {
                                            FormField tableObject = field.mTableStructure.get(i).mRowComponents.get(j);

                                            if (tableObject.mWeight > 0) {
                                                cellParams.weight = tableObject.mWeight;
                                            } else {
                                                cellParams.weight = 1;
                                            }

                                            switch (tableObject.mUiType) {

                                                case Constants.TABULAR_FORM_TEXT_FIELD:

                                                    TextView textView = new TextView(getActivity());

                                                    if (tableObject.mLabelStyle != null &&
                                                            tableObject
                                                                    .mLabelStyle.equals(Constants.VIEW_LABEL_STYLE_BOLD)) {
                                                        textView.setTypeface(null, Typeface.BOLD);
                                                    }

                                                    textView.setTextSize(16);

                                                    if (field.mTableStructure.get(i).mForegroundColor != null) {
                                                        textView.setTextColor(Color.parseColor(field.mTableStructure
                                                                .get(i).mForegroundColor));
                                                    }

                                                    // Alignment of the textview
                                                    setAlignment(tableObject.mAligned, textView);

                                                    cellParams.setMargins(5, 0, 5, 0);
                                                    textView.setLayoutParams(cellParams);
                                                    if (tableObject.mLabel != null) {
                                                        // Label is available
                                                        textView.setText(StringUtils.getTranslatedString(tableObject.mLabel));
                                                        if (tableObject.mValidation != null && tableObject.mValidation.mMandatory) {
                                                            textView.setText(textView.getText() + "*");
                                                        }
                                                    }
                                                    if (tableObject.mIdentifier != null && !tableObject.mIdentifier.isEmpty()) {
                                                        String keyValue = getValueForKey(field
                                                                .mTableStructure.get(i).mRowComponents.get(j).mIdentifier, null);

                                                        if (keyValue != null) {
                                                            if (textView.getText() != null) {
                                                                if (!textView.getText().toString().isEmpty()) {
                                                                    textView.setText(textView.getText() + " : " + keyValue);
                                                                } else {
                                                                    textView.setText(keyValue);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    rowLayout.addView(textView);

                                                    break;

                                                case Constants.TABULAR_FORM_RADIO_FIELD:

                                                    break;

                                                case Constants.TABULAR_FORM_EDITTEXT_FIELD:

                                                    break;

                                                default:
                                                    break;
                                            }
                                        }
                                    }

                                    tableRootLayout.addView(rowLayout);
                                }
                            }
                        }

                        layout.addView(tableView);
                    }
                        break;

                    case Constants.TABULAR_FORM_SPINNER_FIELD: {

                        View spinnerView = mInflater.inflate(R.layout.tabular_form_spinner_field_layout,
                                layout, false);

                        LinearLayout spinnerRoot = spinnerView
                                .findViewById(R.id.tabular_form_spinner_field_root);

                        TextView spinnerLabel = spinnerView.findViewById(R.id.form_spinner_label);
                        if (field.mLabel != null && !field.mLabel.isEmpty()) {
                            spinnerLabel.setText(StringUtils.getTranslatedString(field.mLabel));

                            if (field.mValidation != null && field.mValidation.mMandatory) {
                                spinnerLabel.setText(spinnerLabel.getText() + "*");
                            }
                            if (field.mLabelColor != null && !field.mLabelColor.isEmpty()) {
                                spinnerLabel.setTextColor(Color.parseColor(field.mLabelColor));
                            } else {
                                spinnerLabel.setTextColor(Color.BLACK);
                            }
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                spinnerLabel.setText(spinnerLabel.getText() + " (" + field.mUOM + ")");
                            }
                            if(field.mAligned != null && !field.mAligned.isEmpty()){
                                setAlignment(field.mAligned, spinnerLabel);
                            } else {
                                spinnerLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                        }

                        RelativeLayout spinnerContainer = spinnerView
                                .findViewById(R.id.tabular_form_spinner_field_container);

                        // TODO: TEST APAIMS WITH UPDATION
                        Spinner spinner = new Spinner(getActivity());
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout
                                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);

                        spinner.setBackgroundResource(R.drawable.tabular_spinner_background);

                        layoutParams.setMargins(0, 0, 0, 0);

                        ArrayAdapter<String> spinnerArrayAdapter = null;


                        /*
                        TODO :
                        1. Check if we have multiple values.
                        2. If we do, the flow is as usual
                        3. If we don't, have to check from the entity table and populate the spinner.
                         */

//                        if (field.mMultipleValues != null && !field.mMultipleValues.isEmpty()) {
//                            ArrayList<MultipleValues> spinnerMultipleValues = field.mMultipleValues;
//                            String[] values = new String[spinnerMultipleValues.size()];
//                            for (int i = 0; i < spinnerMultipleValues.size(); i++) {
//                                values[i] = spinnerMultipleValues.get(i).mValue;
//                            }
//
//                            spinnerArrayAdapter = new ArrayAdapter<String>
//                                    (getActivity(), R.layout.form_spinner_item_layout, values);
//                        } else {
//                            String[] values = new String[0];
//                            spinnerArrayAdapter = new ArrayAdapter<String>
//                                    (getActivity(), R.layout.form_spinner_item_layout, values);
//                            // MultipleValues do not exist
//                            // Needs to fetch the possible values from the entity table
//
//                        }

                        ArrayList<MultipleValues> spinnerMultipleValues = field.mMultipleValues == null ? new ArrayList<>(): field.mMultipleValues;
                        String values[] = new String[spinnerMultipleValues.size()];
                        for (int i = 0; i < spinnerMultipleValues.size(); i++) {
                            values[i] = getTranslatedValue(spinnerMultipleValues.get(i).mValue);
                        }
                        spinnerArrayAdapter = new ArrayAdapter<String>
                                    (getActivity(), R.layout.form_spinner_item_layout, values);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spinnerArrayAdapter);

                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                Spinner spinnerReference = (Spinner) keyToFieldObjectMap.get(field.mIdentifier);
                                ArrayAdapter spinnerArrayAdapter = (ArrayAdapter) spinner.getAdapter();
                                // Adding table cell value to the submission map
                                String selectedValue = field.mMultipleValues != null && field.mMultipleValues.get(position) != null ? field.mMultipleValues.get(position).mValue : spinnerArrayAdapter.getItem(position).toString();
                                if (tag == null) {
                                    ((TabularFormActivity) getActivity()).addToUserEnteredValues(field.mIdentifier
                                            , selectedValue);
                                } else {
                                    // The tag is ---> outerField##index
                                    // Split on ## and add the value to the table submission array

                                    List<String> fieldInfo = Arrays.asList
                                            (tag.trim().split("##"));
//                                     selectedValue = String.valueOf(field.mMultipleValues.get(position).mValue);
                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValuesMap(fieldInfo.get(0),
                                                    Integer.parseInt(fieldInfo.get(1)),
                                                    field.mIdentifier, selectedValue);

                                }

                                if (field.mMultipleValues != null && field.mMultipleValues.get(position).mExpandable != null) {
                                    int childCount = spinnerRoot.getChildCount();
                                    if (childCount > 1) {
                                        while (spinnerRoot.getChildCount() > 1) {
                                            spinnerRoot.removeViewAt(spinnerRoot.getChildCount() - 1);
                                        }
                                    }
                                    expandableItemClicked(field.mMultipleValues.get(position).mExpandable, spinnerRoot, tag, mAdditionalInfo);
                                } else {
                                    int childCount = spinnerRoot.getChildCount();
                                    if (childCount > 1) {
                                        while (spinnerRoot.getChildCount() > 1) {
                                            spinnerRoot.removeViewAt(spinnerRoot.getChildCount() - 1);
                                        }
                                    }
                                }
                                if(autoPopulateConfig != null && autoPopulateConfig.isSource != null && autoPopulateConfig.isSource) {
                                    autoPopulateDependentFields(field.mIdentifier, tag);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // your code here
                            }

                        });

                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

                        if (field.mAligned == null) {
                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        } else {
                            if (field.mAligned.equals(Constants.VIEW_ALIGNMENT_RIGHT)) {
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            } else if (field.mAligned.equals(Constants.VIEW_ALIGNMENT_LEFT)) {
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            } else {
                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                            }
                        }

                        String savedValue = getValueForKey(field.mIdentifier, tag);
                        if (savedValue != null && !savedValue.isEmpty()){
                            for (int spinnerItem = 0; field.mMultipleValues != null && spinnerItem <field.mMultipleValues.size() ; spinnerItem++) {
                                if (field.mMultipleValues.get(spinnerItem).mValue.equalsIgnoreCase(savedValue)){
                                    spinner.setSelection(spinnerItem);
                                }
                            }
                        }

                        spinner.setLayoutParams(layoutParams);

                        spinnerContainer.addView(spinner);
                        if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
                            autoPopulateConfig.mView = spinner;
                            autoPopulateConfig.mUiType = field.mUiType;
                            String sourceKey = autoPopulateConfig.getSourceKey() + (tag == null ? "" : "$$" + tag);
                            ((TabularFormActivity) getActivity()).addToAutoPopulateKeysMap(sourceKey, field.mIdentifier, autoPopulateConfig);
                            autoPopulateField(field.mIdentifier);
//                        editTextValue.setEnabled(false);
                        }
                        if(autoPopulateConfig != null && autoPopulateConfig.isSource!= null && autoPopulateConfig.isSource) {
                            autoPopulateDependentFields(field.mIdentifier, tag);
                        }
                        layout.addView(spinnerView);

                    }

                        break;

                    case Constants.TABULAR_FORM_EDITTEXT_FIELD: {

                        View editTextView = mInflater.inflate(R.layout.tabular_edittext_field_layout,
                                layout, false);

                        TextView editTextLabel = editTextView.findViewById(R.id.tabular_edittext_label);
                        EditText editText = editTextView.findViewById(R.id.tabular_edittext);
                        editText.setBackgroundResource(R.drawable.tabular_edittext_background);

                        if (field.mLabel != null && !field.mLabel.isEmpty()) {
                            editTextLabel.setText(StringUtils.getTranslatedString(field.mLabel));
                            if (field.mValidation != null && field.mValidation.mMandatory) {
                                editTextLabel.setText(editTextLabel.getText() + "*");
                            }
                            if (field.mLabelColor != null && !field.mLabelColor.isEmpty()) {
                                editTextLabel.setTextColor(Color.parseColor(field.mLabelColor));
                            } else {
                                editTextLabel.setTextColor(Color.BLACK);
                            }
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                editTextLabel.setText(editTextLabel.getText() + " (" + field.mUOM + ")");
                            }
                            if(field.mAligned != null && !field.mAligned.isEmpty()){
                                setAlignment(field.mAligned, editTextLabel);
                            } else {
                                editTextLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                        }
                        editText.setId(mCountForId);

                        mCountForId++;
                        setInputType(field.mDatatype, editText);
                        editText.setMaxLines(1);

                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                /* Check if there is a tag or not
                                If there is a tag
                                    ----- the submission (part of sub-form) should go into table field
                                If the tag is null
                                    ----- the submission is not a part of a sub-form, can go into the
                                          map other than the table submission map
                                 */

                                if (tag == null) {
                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValues(field.mIdentifier, s.toString());
                                } else {
                                    // The tag is ---> outerField##index
                                    // Split on ## and add the value to the table submission array

                                    List<String> fieldInfo = Arrays.asList
                                            (tag.trim().split("##"));

                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValuesMap(fieldInfo.get(0),
                                                    Integer.parseInt(fieldInfo.get(1)),
                                                    field.mIdentifier, s.toString());

                                    calculateValueForComputedTextView(field.mIdentifier, s.toString(), Integer.parseInt(fieldInfo.get(1)));
                                }
                                if(autoPopulateConfig != null && autoPopulateConfig.isSource != null && autoPopulateConfig.isSource) {
                                    autoPopulateDependentFields(field.mIdentifier, tag);
                                }
                            }

                        });

                        String value = getValueForKey(field.mIdentifier, tag);
                        if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
                            editText.setText(value);
                        }

                        editText.setEnabled(field.mEditable);
                        layout.addView(editTextView);
                        if(autoPopulateConfig != null && autoPopulateConfig.getSourceKey() != null && !autoPopulateConfig.getSourceKey().isEmpty()) {
                            autoPopulateConfig.mView = editText;
                            autoPopulateConfig.mUiType = field.mUiType;
                            String sourceKey = autoPopulateConfig.getSourceKey() + (tag == null ? "" : "$$" + tag);
                            ((TabularFormActivity) getActivity()).addToAutoPopulateKeysMap(sourceKey, field.mIdentifier, autoPopulateConfig);
                            autoPopulateField(field.mIdentifier);
//
                        }
                        if(autoPopulateConfig != null && autoPopulateConfig.isSource != null && autoPopulateConfig.isSource) {
                            autoPopulateDependentFields(field.mIdentifier, tag);
                        }
                    }
                        break;

                    case Constants.TABULAR_FORM_COMPUTED_TEXTVIEW: {

                        View computedTextViewView = mInflater.inflate(R.layout.tablular_computed_textview_field_layout,
                                layout, false);

                        TextView computedTextViewLabel = computedTextViewView.findViewById(R.id.tabular_computed_textview_label);
                        TextView computedTextView = computedTextViewView.findViewById(R.id.tabular_computed_textview);

                        if (field.mLabel != null && !field.mLabel.isEmpty()) {
                            computedTextViewLabel.setText(StringUtils.getTranslatedString(field.mLabel));
                            if (field.mLabelColor != null && !field.mLabelColor.isEmpty()) {
                                computedTextViewLabel.setTextColor(Color.parseColor(field.mLabelColor));
                            } else {
                                computedTextViewLabel.setTextColor(Color.BLACK);
                            }
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                computedTextViewLabel.setText(computedTextViewLabel.getText() + " (" + field.mUOM + ")");
                            }
                            if(field.mAligned != null && !field.mAligned.isEmpty()){
                                setAlignment(field.mAligned, computedTextViewLabel);
                            } else {
                                computedTextViewLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                        }

                        computedTextView.setText("-");

                        String expression = field.mComputationExpression;
                        if (expression == null || expression.isEmpty()) {
                            Utils.logError(tag, "Computed Textview expression cannot be empty");
                            break;
                        }
                        List<String> variablesForEvaluation = CustomExpressionEvaluator.getVariables(expression);
                        int index = 0;
                        if (tag != null) {
                            List<String> fieldInfo = Arrays.asList(tag.trim().split("##"));
                            index = Integer.parseInt(fieldInfo.get(1));
                        }
                        Map<String, ComputedExpression> keyToComputedValueMap = new HashMap<>();
                        for (String variable : variablesForEvaluation) {
                            ComputedExpression computedExpression = new ComputedExpression();
                            computedExpression.setExpression(expression);
                            computedExpression.setView(computedTextView);
                            String valueFromProjectMetaData = getValueForKey(variable, tag);
                            if (valueFromProjectMetaData != null)
                                computedExpression.setValue(valueFromProjectMetaData);
                            keyToComputedValueMap.put(variable, computedExpression);
                        }
                        indexTokeyToComputedDataMap.put(index, keyToComputedValueMap);
                        String existingValue = getValueForKey(field.mIdentifier, tag);
                        indexTokeyToComputedDataMap.get(index).put(field.mIdentifier, new ComputedExpression(expression, computedTextView, existingValue));
                        calculateValueForComputedTextView(field.mIdentifier, existingValue, index);

                        computedTextView.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                /* Check if there is a tag or not
                                If there is a tag
                                    ----- the submission (part of sub-form) should go into table field
                                If the tag is null
                                    ----- the submission is not a part of a sub-form, can go into the
                                          map other than the table submission map
                                 */

                                if (tag == null) {
                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValues(field.mIdentifier, s.toString());
                                } else {
                                    // The tag is ---> outerField##index
                                    // Split on ## and add the value to the table submission array

                                    List<String> fieldInfo = Arrays.asList
                                            (tag.trim().split("##"));

                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValuesMap(fieldInfo.get(0),
                                                    Integer.parseInt(fieldInfo.get(1)),
                                                    field.mIdentifier, s.toString());
                                }
                            }
                        });

                        layout.addView(computedTextViewView);
                    }

                        break;

                    case Constants.TABULAR_FORM_GEOTAGGED_IMAGE_FUSED_FIELD: {

                        View imageView = mInflater.inflate(R.layout.tabular_form_image_field, layout, false);

                        LinearLayout imagePreviewLayout = imageView.findViewById(R.id.tabular_form_image_group);
                        TextView imageLabel = imageView.findViewById(R.id.tabular_form_image_field_label);
                        TextView imageValue = imageView.findViewById(R.id.tabular_form_image_uuids);
                        ImageView imageIcon = imageView.findViewById(R.id.tabular_form_image_field_click_button);

                        if (field.mLabel != null && !field.mLabel.isEmpty()) {
                            imageLabel.setText(StringUtils.getTranslatedString(field.mLabel));
                            if (field.mValidation != null && field.mValidation.mMandatory){
                                imageLabel.setText(imageLabel.getText() + "*");
                            }
                            if (field.mLabelColor != null && !field.mLabelColor.isEmpty()) {
                                imageLabel.setTextColor(Color.parseColor(field.mLabelColor));
                            } else {
                                imageLabel.setTextColor(Color.BLACK);
                            }
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                imageLabel.setText(imageLabel.getText() + " (" + field.mUOM + ")");
                            }
                            if(field.mAligned != null && !field.mAligned.isEmpty()){
                                setAlignment(field.mAligned, imageLabel);
                            } else {
                                imageLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                        }

                        String imageUuidString = getValueForKey(field.mIdentifier, tag);

                        if (imageUuidString != null && !imageUuidString.isEmpty()) {

                            imageValue.setText(imageUuidString);
                            mCurrentImageLinearLayout = imagePreviewLayout;
                            mCurrentImageLinearLayout.removeAllViews();
                            mCurrentImageUuids = imageValue;
                            List<String> uuidsWithLongLat = Arrays.asList(imageUuidString.split("\\s*,\\s*"));
                            List<String> uuids = Utils.getImageUUIDList(uuidsWithLongLat);
                            for (String uuid : uuids) {
                                FormMedia formImage = UAAppContext.getInstance().getDBHelper().getFormMedia(
                                        uuid, ((TabularFormActivity) getActivity()).getAppId(), UAAppContext.getInstance().getUserID());
                                if (formImage != null) {
                                    createNewImageThumbnail(formImage, imagePreviewLayout, imageValue);
                                }
                            }
                        }

                        imageValue.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                /* Check if there is a tag or not
                                If there is a tag
                                    ----- the submission (part of sub-form) should go into table field
                                If the tag is null
                                    ----- the submission is not a part of a sub-form, can go into the
                                          map other than the table submission map
                                 */

                                if (tag == null) {
                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValues(field.mIdentifier, s.toString());
                                } else {
                                    // The tag is ---> outerField##index
                                    // Split on ## and add the value to the table submission array

                                    List<String> fieldInfo = Arrays.asList
                                            (tag.trim().split("##"));

                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValuesMap(fieldInfo.get(0),
                                                    Integer.parseInt(fieldInfo.get(1)),
                                                    field.mIdentifier, s.toString());
                                }
                            }
                        });

                        // Check if there is an expandable component
                        if (field.mExpandable != null && field.mExpandable.mIconUrl != null) {
                            // Expandable component has clickable icon
                            if (field.mUiType.equals(Constants.TABULAR_FORM_GEOTAGGED_IMAGE_FUSED_FIELD)) {
                                imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.geotagged_picture));
                            } else {
                                imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.picture));
                            }
                            imageIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open Camera Activity
                                    List<String> uuidsWithLonLat = new ArrayList<>();
                                    List<String> uuids = new ArrayList<>();

                                    if (Utils.getInstance().checkCameraHardware(((TabularFormActivity) getActivity()))) {
                                        // Check for the current values of imageValue
                                        String images = imageValue.getText().toString();
                                        if (images != null && !images.isEmpty()) {
                                            // Delete these images
                                            uuidsWithLonLat.addAll(Arrays.asList(images.split("\\s*,\\s*")));
                                            uuids = Utils.getImageUUIDList(uuidsWithLonLat);
                                        }

                                        // Device has camera
                                        // If the SDK is >= Marshmello, runtime permissions required
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            boolean isPermissionGiven = Utils.getInstance()
                                                    .checkForPermission(((TabularFormActivity) getActivity())
                                                            , Manifest.permission.CAMERA);
                                            if (isPermissionGiven) {
                                                mCurrentImageLinearLayout = imagePreviewLayout;
                                                mCurrentImageUuids = imageValue;

                                                startCameraActivity(field.mIdentifier
                                                        , field.mDatatype, field.mUiType, field.mMax,
                                                        field.mIdentifier, "",
                                                        "", uuids, field.mGpsValidation);
                                            } else {
                                                ActivityCompat.requestPermissions(getActivity(),
                                                        new String[]{Manifest.permission.CAMERA},
                                                        Constants.PERMISSION_CAMERA);
                                            }
                                        } else {
                                            mCurrentImageLinearLayout = imagePreviewLayout;
                                            mCurrentImageUuids = imageValue;

                                            startCameraActivity(field.mIdentifier
                                                    , field.mDatatype, field.mUiType, field.mMax,
                                                    field.mIdentifier, "",
                                                    "", uuids, field.mGpsValidation);
                                        }
                                    } else {
                                        // Device does not have camera
                                        Toast.makeText(getActivity(), getResources().getString(R.string.no_camera)
                                                , Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                        layout.addView(imageView);
                    }
                        break;

                    case Constants.TABULAR_FORM_ADDENDUM_FIELD: {

                        /*
                        -- Inflate view
                        -- Create the label
                        -- Create the header row
	                    -- Initialize the RecyclerView
	                    -- Add row with all values
		                    -- Should only be added once all columns are filled
		                    -- Should be added to the submission
		                -- Show any values that come from the previous submissions (populate RecyclerView if needed)

		                NOTE :-
		                -- The filtering key should be added to the submissions
                         */

                        View addendumView = mInflater.inflate(R.layout.tabular_form_addendum_field, layout, false);

                        TextView addendumLabel = addendumView.findViewById(R.id.table_form_addendum_field_label);
                        LinearLayout addendumHeaderParentLayout = addendumView.findViewById(R.id.addendum_header_parent_linear_layout);
                        LinearLayout addendumHeaderHorizontalLayout = addendumView.findViewById(R.id.addendum_header_horizontal_linear_layout);
                        addendumHeaderHorizontalLayout.setPadding(0, 10, 0, 10);
                        LinearLayout addendumRowVerticalLayout = addendumView.findViewById(R.id.addendum_row_vertical_layout);

                        // Setting the label of the addendum field
                        if (field.mLabel != null) {
                            addendumLabel.setText(StringUtils.getTranslatedString(field.mLabel));
                            addendumLabel.setTextSize(22);
                            addendumLabel.setTextColor(getResources().getColor(R.color.table_heading_text_color));

                            setAlignment(field.mAligned, addendumLabel);

                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                addendumLabel.setText(addendumLabel.getText() + " (" + field.mUOM + ")");
                            }
                        }
                        addendumFieldKeyToLastIndexMap.put(field.mIdentifier, 0);
                        if (field.mTableStructure != null && !field.mTableStructure.isEmpty()) {

                            // Add any current rows to the addendum field
                            String addendumJsonArrayString = ((TabularFormActivity) getActivity()).getValueForKey(field.mIdentifier);
                            Utils.logInfo("JSON ARRAY FOR KEY :: " + field.mIdentifier + "VALUE :: " + addendumJsonArrayString);
                            JSONArray jsonArray = null;

                            Map<Integer, Map<String, String>> indexToKeyToValueMap = getAddendumFieldValue(field.mIdentifier);
                            if (indexToKeyToValueMap.size() > 0) {
                                for (int k : indexToKeyToValueMap.keySet()) {

                                    LinearLayout addendumViewRowLayout = new LinearLayout(getActivity());
                                    addendumViewRowLayout.setOrientation(LinearLayout.HORIZONTAL);

                                    addendumViewRowLayout.setPadding(0, 10, 0, 10);

                                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams
                                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                    addendumViewRowLayout.setLayoutParams(rowParams);

                                    int finalK = k;
                                    Map<String, String> keyToValueMap = indexToKeyToValueMap.get(finalK);
                                    TableRow tableRow = field.mTableStructure.get(1);
                                    // Add components to LinearLayout
                                    if (tableRow.mRowComponents != null &&
                                            tableRow.mRowComponents.size() > 0) {

                                        for (FormField rowComponent : tableRow.mRowComponents) {

                                            if (rowComponent == null) {
                                                continue;
                                            }

                                            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams
                                                    (0, ViewGroup.LayoutParams.MATCH_PARENT);

                                            if (rowComponent.mWeight > 0) {
                                                cellParams.weight = rowComponent.mWeight;
                                            } else {
                                                cellParams.weight = 1;
                                            }

                                            // Iterate through row components
                                            switch (rowComponent.mUiType) {

                                                case Constants.TABULAR_FORM_EDITTEXT_FIELD:

                                                    EditText addendumCellEditText = new EditText(getActivity());

                                                    addendumCellEditText.setTextSize(12);

                                                    addendumCellEditText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                                    addendumCellEditText.setPadding(8, 4, 4, 8);

                                                    addendumCellEditText.setLayoutParams(cellParams);

                                                    addendumCellEditText.setBackgroundResource(R.drawable.tabular_edittext_background);

                                                    addendumCellEditText.setMaxLines(1);

                                                    if (rowComponent.mLabel != null && !rowComponent.mLabel.isEmpty()) {
                                                        addendumCellEditText.setHint(rowComponent.mLabel);
                                                    }
                                                    setInputType(rowComponent.mDatatype, addendumCellEditText);

                                                    String cellValue = keyToValueMap.get(rowComponent.mIdentifier);
                                                    if (cellValue != null && !cellValue.isEmpty() && !cellValue.equalsIgnoreCase("null")) {
                                                        addendumCellEditText.setText(cellValue);
                                                    }

                                                    if (rowComponent.mEditable) {
                                                        addendumCellEditText.setEnabled(true);
                                                    } else {
                                                        addendumCellEditText.setEnabled(false);
                                                    }

                                                    addendumViewRowLayout.addView(addendumCellEditText);

                                                    break;

                                                default:
                                                    break;
                                            }
                                        }
                                    }

                                    RelativeLayout iconLayout = new RelativeLayout(getActivity());
                                    LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    iconLayout.setLayoutParams(iconLayoutParams);
                                    iconLayout.setBackgroundColor(Color.TRANSPARENT);

                                    TextView rowDeletionTextButton = new TextView(getActivity());
                                    rowDeletionTextButton.setGravity(Gravity.CENTER);
                                    rowDeletionTextButton.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                    rowDeletionTextButton.setBackground(getResources().getDrawable(R.drawable.round_button));
                                    rowDeletionTextButton.setTextColor(getResources().getColor(R.color.white));
                                    rowDeletionTextButton.setText("-");
                                    rowDeletionTextButton.setTextSize(20);
                                    rowDeletionTextButton.setTypeface(null, Typeface.BOLD);
                                    RelativeLayout.LayoutParams rowDeletionTextButtonParams = new RelativeLayout.LayoutParams(80, 80);
                                    rowDeletionTextButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
                                    rowDeletionTextButtonParams.setMargins(10, 8, 8, 10);
                                    rowDeletionTextButton.setLayoutParams(rowDeletionTextButtonParams);

                                    iconLayout.addView(rowDeletionTextButton);

                                    addendumViewRowLayout.addView(iconLayout, addendumViewRowLayout.getChildCount());

                                    // delete row from map
                                    rowDeletionTextButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            removeKeyToValueMapFromIndex(field.mIdentifier, finalK);
                                            addendumRowVerticalLayout.removeView(addendumViewRowLayout);
                                        }
                                    });

                                    if (tableRow.mFilteringKey != null && !tableRow.mFilteringKey.isEmpty()) {
                                        // Filtering key present. Only make those rows visible, that have the filtering key in their JSON
                                        String filteringKey = tableRow.mFilteringKey;

                                        List<String> keyElements = new ArrayList<>();
                                        keyElements.addAll(Arrays.asList(filteringKey.split("\\$")));

                                        if (keyElements.size() > 0) {

                                            String additionalInfoKey = keyElements.get(keyElements.size() - 1);

                                            String filterValue = mAdditionalInfo != null ? mAdditionalInfo.get(additionalInfoKey) : null;

                                            String cellValue = keyToValueMap.get(filteringKey);

                                            if (cellValue != null && !cellValue.isEmpty() &&
                                                    filterValue != null && !filterValue.isEmpty() &&
                                                    filterValue.equalsIgnoreCase(cellValue)) {

                                            } else {
                                                addendumViewRowLayout.setVisibility(View.GONE);
                                            }
                                        }

                                    }
                                    if (addendumFieldKeyToLastIndexMap != null && addendumFieldKeyToLastIndexMap.get(field.mIdentifier) <= finalK) {
                                        addendumFieldKeyToLastIndexMap.put(field.mIdentifier, finalK + 1);
                                    }

                                    addendumRowVerticalLayout.addView(addendumViewRowLayout);
                                }

                            }

                            // Adding Header Row to Addendum
                            if (field.mTableStructure.get(0).mWeightSum > 0) {
                                addendumHeaderHorizontalLayout.setWeightSum(field.mTableStructure.get(0).mWeightSum);
                            }

                            // Header is the first row without repeat
                            for (FormField headerComponent : field.mTableStructure.get(0).mRowComponents) {

                                LinearLayout.LayoutParams headerCellParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);

                                // Setting weight to the header component
                                if (headerComponent.mWeight > 0) {
                                    headerCellParams.weight = headerComponent.mWeight;
                                } else {
                                    headerCellParams.weight = 1;
                                }

                                switch (headerComponent.mUiType) {

                                    case Constants.TABULAR_FORM_EDITTEXT_FIELD:

                                        EditText headerEditText = new EditText(getActivity());

                                        setInputType(headerComponent.mDatatype, headerEditText);

                                        headerEditText.setTextSize(12);

                                        headerEditText.setPadding(8, 4, 4, 8);

                                        headerEditText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                        headerEditText.setLayoutParams(headerCellParams);

                                        headerEditText.setBackgroundResource(R.drawable.tabular_edittext_background);

                                        headerEditText.setMaxLines(1);

                                        if (headerComponent.mLabel != null && !headerComponent.mLabel.isEmpty()) {
                                            headerEditText.setHint(StringUtils.getTranslatedString(headerComponent.mLabel));
                                        }

                                        addendumHeaderHorizontalLayout.addView(headerEditText);

                                        break;

                                    case Constants.TABULAR_FORM_SPINNER_FIELD:

                                        Spinner addendumSpinner = new Spinner(getActivity());

                                        addendumSpinner.setBackgroundResource(R.drawable.tabular_spinner_background);

                                        addendumSpinner.setPadding(8, 4, 4, 8);

                                        ArrayAdapter<String> addendumSpinnerArrayAdapter = null;

                                        ArrayList<MultipleValues> addendumSpinnerMultipleValues = headerComponent.mMultipleValues;
                                        String[] spinnerValues = new String[addendumSpinnerMultipleValues.size()];
                                        for (int i = 0; i < addendumSpinnerMultipleValues.size(); i++) {
                                            spinnerValues[i] = StringUtils.getTranslatedString(addendumSpinnerMultipleValues.get(i).mValue);
                                        }

                                        addendumSpinnerArrayAdapter = new ArrayAdapter<String>
                                                (getActivity(), R.layout.form_spinner_item_layout, spinnerValues);
                                        addendumSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        addendumSpinner.setAdapter(addendumSpinnerArrayAdapter);

                                        addendumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                                ((TextView) parentView.getChildAt(0)).setTextSize(12);

                                                if (headerComponent.mMultipleValues.get(position).mExpandable != null) {
                                                    if (addendumHeaderParentLayout.getChildCount() > 1) {
                                                        while (addendumHeaderParentLayout.getChildCount() > 1) {
                                                            addendumHeaderParentLayout.removeViewAt(addendumHeaderParentLayout.getChildCount() - 1);
                                                        }
                                                    }
                                                    expandableItemClicked(headerComponent.mMultipleValues.get(position).mExpandable, addendumHeaderParentLayout, tag, mAdditionalInfo);
                                                } else {
                                                    if (addendumHeaderParentLayout.getChildCount() > 1) {
                                                        while (addendumHeaderParentLayout.getChildCount() > 1) {
                                                            addendumHeaderParentLayout.removeViewAt(addendumHeaderParentLayout.getChildCount() - 1);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parentView) {
                                                // your code here
                                            }
                                        });

                                        addendumSpinner.setLayoutParams(headerCellParams);
                                        addendumHeaderHorizontalLayout.addView(addendumSpinner);

                                        break;

                                    default:
                                        break;
                                }
                            }

                            RelativeLayout addIconLayout = new RelativeLayout(getActivity());
                            LinearLayout.LayoutParams addIconLayoutParams = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
                            addIconLayout.setLayoutParams(addIconLayoutParams);
                            addIconLayout.setBackgroundColor(Color.TRANSPARENT);

                            rowAdditionTextButton = createAddendumTextButton("+");

                            rowAdditionTextButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // TODO:
                                    // 1. Check if all the fields are filled
                                    // 2. Add to list
                                    // 3. Add to submission array
                                    // 4. Should take the filtering_key in account too

                                    int i = 0;
                                    boolean isValid = true;
                                    JSONObject submissionObject = new JSONObject();

                                    String spinnerExpandableValue = "";
                                    for (FormField headerComponent : field.mTableStructure.get(0).mRowComponents) {

                                        if (headerComponent.mUiType != null && !headerComponent.mUiType.isEmpty()) {
                                            switch (headerComponent.mUiType) {
                                                case Constants.TABULAR_FORM_EDITTEXT_FIELD:

                                                    // Creating JSON Object
                                                    String value = ((EditText) addendumHeaderHorizontalLayout.getChildAt(i)).getText().toString();
                                                    if (value == null || value.isEmpty()) {
                                                        isValid = false;
                                                    } else {
                                                        try {
                                                            submissionObject.put(headerComponent.mIdentifier, value);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    break;

                                                case Constants.TABULAR_FORM_SPINNER_FIELD:

                                                    Spinner spinner1 = (Spinner) addendumHeaderHorizontalLayout.getChildAt(i);
                                                    String spinnerValue = spinner1.getSelectedItem().toString();

                                                    if (spinnerValue == null || spinnerValue.isEmpty()) {
                                                        isValid = false;
                                                    } else {
                                                        try {

                                                            ArrayList<MultipleValues> multipleValues = headerComponent.mMultipleValues;

                                                            for (int k = 0; k < multipleValues.size(); k++) {
                                                                String spinnerItem = multipleValues.get(k).mValue;
                                                                if (spinnerValue.equals(spinnerItem)) {

                                                                    if (multipleValues.get(k).mExpandable.mSubForm != null) {
                                                                        String valueForKey = getValueForKey(headerComponent.mIdentifier, tag);
                                                                        if (valueForKey == null || valueForKey.isEmpty()) {
                                                                            isValid = false;
                                                                        } else {
                                                                            submissionObject.put(headerComponent.mIdentifier, valueForKey);
                                                                            spinnerExpandableValue = headerComponent.mIdentifier;
//                                                                            ((TabularFormActivity) getActivity()).removeKeyToValueFromUserEnteredValuesMap(headerComponent.mIdentifier);
                                                                        }
                                                                    } else {
                                                                        submissionObject.put(headerComponent.mIdentifier, spinnerValue);
                                                                    }
                                                                }
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    break;

                                                default:
                                                    break;
                                            }
                                        }
                                        i++;
                                    }

                                    if (!isValid) {
                                        if (!submitClicked) {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.FIELD_NOT_ENTERED), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Valid submission

                                        // Refresh the header components
                                        ((TabularFormActivity) getActivity()).
                                                removeKeyToValueFromUserEnteredValuesMap(spinnerExpandableValue);
                                        int j = 0;
                                        for (FormField headerComponent : field.mTableStructure.get(0).mRowComponents) {

                                            if (headerComponent.mUiType != null && !headerComponent.mUiType.isEmpty()) {
                                                switch (headerComponent.mUiType) {
                                                    case Constants.TABULAR_FORM_EDITTEXT_FIELD:

                                                        EditText addendumEdittext = ((EditText) addendumHeaderHorizontalLayout.getChildAt(j));
                                                        addendumEdittext.setText("");
                                                        addendumEdittext.setHint(headerComponent.mLabel);
                                                        addendumEdittext.setCursorVisible(false);

                                                        addendumEdittext.setOnTouchListener(new View.OnTouchListener() {
                                                            @Override
                                                            public boolean onTouch(View v, MotionEvent event) {
                                                                addendumEdittext.setCursorVisible(true);
                                                                return false;
                                                            }
                                                        });

                                                        break;
                                                    case Constants.TABULAR_FORM_SPINNER_FIELD:
                                                        Spinner addendumSpinner = (Spinner) addendumHeaderHorizontalLayout.getChildAt(j);
                                                        addendumSpinner.setSelection(0, true);

                                                        if (addendumHeaderParentLayout.getChildCount() > 1) {
                                                            while (addendumHeaderParentLayout.getChildCount() > 1) {
                                                                addendumHeaderParentLayout.removeViewAt(addendumHeaderParentLayout.getChildCount() - 1);
                                                            }
                                                        }

                                                    default:
                                                        break;
                                                }
                                            }

                                            j++;
                                        }

                                        // Render on screen (Add to linear layout)
                                        LinearLayout addendumViewRowLayout = new LinearLayout(getActivity());
                                        addendumViewRowLayout.setOrientation(LinearLayout.HORIZONTAL);

                                        addendumViewRowLayout.setPadding(0, 10, 0, 10);

                                        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams
                                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                        addendumViewRowLayout.setLayoutParams(rowParams);

                                        TableRow tableRow = field.mTableStructure.get(1);
                                        Map<String, String> KeyToValueMap = new HashMap<>();
                                        // Add components to LinearLayout
                                        if (tableRow.mRowComponents != null &&
                                                tableRow.mRowComponents.size() > 0) {

                                            for (FormField rowComponent : tableRow.mRowComponents) {

                                                if (rowComponent == null) {
                                                    continue;
                                                }

                                                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams
                                                        (0, ViewGroup.LayoutParams.MATCH_PARENT);

                                                if (rowComponent.mWeight > 0) {
                                                    cellParams.weight = rowComponent.mWeight;
                                                } else {
                                                    cellParams.weight = 1;
                                                }

                                                // Iterate through row components
                                                switch (rowComponent.mUiType) {

                                                    case Constants.TABULAR_FORM_EDITTEXT_FIELD:

                                                        EditText addendumCellEditText = new EditText(getActivity());

                                                        addendumCellEditText.setPadding(8, 4, 4, 8);

                                                        addendumCellEditText.setTextSize(12);

                                                        addendumCellEditText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                                                        addendumCellEditText.setLayoutParams(cellParams);

                                                        addendumCellEditText.setBackgroundResource(R.drawable.tabular_edittext_background);

                                                        if (rowComponent.mLabel != null && !rowComponent.mLabel.isEmpty()) {
                                                            addendumCellEditText.setHint(rowComponent.mLabel);
                                                        }

                                                        setInputType(rowComponent.mDatatype, addendumCellEditText);

                                                        addendumCellEditText.setMaxLines(1);

                                                        String cellValue = null;
                                                        try {
                                                            cellValue = submissionObject.getString(rowComponent.mIdentifier);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                        if (cellValue != null && !cellValue.isEmpty() && !cellValue.equalsIgnoreCase("null")) {
                                                            addendumCellEditText.setText(cellValue);
                                                        }

                                                        if (rowComponent.mEditable) {
                                                            addendumCellEditText.setEnabled(true);
                                                        } else {
                                                            addendumCellEditText.setEnabled(false);
                                                        }

                                                        KeyToValueMap.put(rowComponent.mIdentifier, cellValue);

                                                        addendumViewRowLayout.addView(addendumCellEditText);
                                                        break;

                                                    default:
                                                        break;
                                                }
                                            }
                                        }

                                        // Adding filtering key, if needed
                                        if (field.mFilteringKey != null && !field.mFilteringKey.isEmpty()) {

                                            List<String> keyElements = new ArrayList<>();
                                            keyElements.addAll(Arrays.asList(field.mFilteringKey.split("\\$")));

                                            if (keyElements.size() > 0) {
                                                if (mAdditionalInfo.containsKey(keyElements.get(keyElements.size() - 1))) {
                                                    KeyToValueMap.put(field.mFilteringKey, mAdditionalInfo.get(keyElements.get(keyElements.size() - 1)));

                                                }
                                            }
                                        }

                                        int index = addendumFieldKeyToLastIndexMap.get(field.mIdentifier);
                                        addKeyToValueMapAtIndex(field.mIdentifier, KeyToValueMap, index);
                                        addendumFieldKeyToLastIndexMap.put(field.mIdentifier, index + 1);
                                        RelativeLayout iconLayout = new RelativeLayout(getActivity());
                                        LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        iconLayout.setLayoutParams(iconLayoutParams);
                                        iconLayout.setBackgroundColor(Color.TRANSPARENT);

                                        TextView rowDeletionTextButton = new TextView(getActivity());
                                        rowDeletionTextButton.setGravity(Gravity.CENTER);
                                        rowDeletionTextButton.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                        rowDeletionTextButton.setBackground(getResources().getDrawable(R.drawable.round_button));
                                        rowDeletionTextButton.setTextColor(getResources().getColor(R.color.white));
                                        rowDeletionTextButton.setText("-");
                                        rowDeletionTextButton.setTextSize(20);
                                        rowDeletionTextButton.setTypeface(null, Typeface.BOLD);
                                        RelativeLayout.LayoutParams rowDeletionTextButtonParams = new RelativeLayout.LayoutParams(80, 80);
                                        rowDeletionTextButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
                                        rowDeletionTextButtonParams.setMargins(10, 8, 8, 10);
                                        rowDeletionTextButton.setLayoutParams(rowDeletionTextButtonParams);

                                        iconLayout.addView(rowDeletionTextButton);

                                        addendumViewRowLayout.addView(iconLayout, addendumViewRowLayout.getChildCount());

                                        // delete row from map
                                        rowDeletionTextButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                removeKeyToValueMapFromIndex(field.mIdentifier, index);
                                                addendumRowVerticalLayout.removeView(addendumViewRowLayout);
                                            }
                                        });
                                        addendumRowVerticalLayout.addView(addendumViewRowLayout);
                                    }

                                }
                            });

                            addIconLayout.addView(rowAdditionTextButton);

                            addendumHeaderHorizontalLayout.addView(addIconLayout, addendumHeaderHorizontalLayout.getChildCount());
                        }

                        layout.addView(addendumView);
                    }
                        break;


                    case Constants.TABULAR_FORM_DATE_FIELD: {

                        View dateElement = mInflater.inflate(R.layout.form_date_picker, layout, false);

                        LinearLayout datePickerRoot = (LinearLayout) dateElement.findViewById(R.id.form_date_picker_root);
                        RelativeLayout datePickerExpandableLayout = (RelativeLayout) dateElement.findViewById(R.id.form_date_picker_expandable_layout);
                        RelativeLayout datePickerStaticIconLayout = (RelativeLayout) dateElement.findViewById(R.id.form_date_picker_static_icon_layout);
                        TextView datePickerLabel = (TextView) dateElement.findViewById(R.id.date_picker_label);
                        TextView datePickerValue = (TextView) dateElement.findViewById(R.id.date_picker_value);
                        ImageView datePickerStaticIcon = (ImageView) dateElement.findViewById(R.id.form_date_picker_static_icon);
                        ImageView datePickerExpandableIcon = (ImageView) dateElement.findViewById(R.id.form_date_picker_expandable_icon);
                        TextView datePickerExpandableText = (TextView) dateElement.findViewById(R.id.form_date_picker_expandable_text);

                        if (field.mLabel != null && !field.mLabel.isEmpty()) {
                            datePickerLabel.setText(StringUtils.getTranslatedString(field.mLabel));
                            if (field.mValidation != null && field.mValidation.mMandatory) {
                                datePickerLabel.setText(datePickerLabel.getText() + "*");
                            }
                            if (field.mLabelColor != null && !field.mLabelColor.isEmpty()) {
                                datePickerLabel.setTextColor(Color.parseColor(field.mLabelColor));
                            } else {
                                datePickerLabel.setTextColor(Color.BLACK);
                            }
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                datePickerLabel.setText(datePickerLabel.getText() + " (" + field.mUOM + ")");
                            }
                            if(field.mAligned != null && !field.mAligned.isEmpty()){
                                setAlignment(field.mAligned, datePickerLabel);
                            } else {
                                datePickerLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                        }

                        datePickerValue.setTag(tag + "#" + field.mIdentifier);

                        datePickerValue.setId(mCountForId);

                        mCountForId++;

                        datePickerValue.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                if (tag == null) {
                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValues(field.mIdentifier, charSequence.toString());
                                } else {
                                    // The tag is ---> outerField##index
                                    // Split on ## and add the value to the table submission array

                                    List<String> fieldInfo = Arrays.asList
                                            (tag.trim().split("##"));

                                    ((TabularFormActivity) getActivity())
                                            .addToUserEnteredValuesMap(fieldInfo.get(0),
                                                    Integer.parseInt(fieldInfo.get(1)),
                                                    field.mIdentifier, charSequence.toString());
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });

                        String fieldValue = getValueForKey(field.mIdentifier, tag);

                        // Check if there is a static icon
                        if (field.mIcon != null) {
                            // Set icon
                            datePickerStaticIconLayout.setVisibility(View.VISIBLE);
                            datePickerStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                        }

                        final SelectableDates selectableDates = field.mSelectableDates;
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = (c.get(Calendar.MONTH) + 1);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        if (selectableDates != null && selectableDates.mSelect != null) {
                            Utils.getInstance().showLog("DATE : ", day + selectableDates.mSelect);
                            String val = day + Integer.parseInt(selectableDates.mSelect) + "/" + month + "/" + year;
                            datePickerValue.setText(val);
                        } else {
                            Utils.getInstance().showLog("DATE : ", day + "");
                            String val = day + "/" + month + "/" + year;
                            datePickerValue.setText(val);
                        }

                        if (fieldValue != null && !fieldValue.isEmpty()){
                            datePickerValue.setText(fieldValue);
                        }

                        // Check if there is an expandable component
                        if (field.mExpandable != null) {
                            // Make expandable component visible
                            datePickerExpandableLayout.setVisibility(View.VISIBLE);
                            if (field.mExpandable.mIconUrl != null) {
                                // Expandable component has clickable icon
                                datePickerExpandableIcon.setVisibility(View.VISIBLE);
                                datePickerExpandableIcon.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        DialogFragment newFragment = new TabularFormFragment.DatePickerFragment(getActivity(), datePickerValue, selectableDates);
                                        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                                    }
                                });
                            } else {
                                // Expandable component has clickable text
                                datePickerExpandableText.setVisibility(View.VISIBLE);
                                datePickerExpandableText.setText(field.mExpandable.mText);
                                datePickerExpandableText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        DialogFragment newFragment = new TabularFormFragment.DatePickerFragment(getActivity(), datePickerValue, selectableDates);
                                        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                                    }
                                });
                            }
                        }

                        layout.addView(dateElement);
                    }
                        break;

                    case Constants.TABULAR_FORM_TEXT_FIELD : {
                        View textViewElement = mInflater.inflate(R.layout.form_text_view, layout, false);

                        LinearLayout textViewRoot = (LinearLayout) textViewElement.findViewById(R.id.form_text_view_root);
                        RelativeLayout textViewExpandableLayout = (RelativeLayout) textViewElement.findViewById(R.id.form_text_view_expandable_layout);
                        RelativeLayout textViewStaticIconLayout = (RelativeLayout) textViewElement.findViewById(R.id.form_text_view_static_icon_layout);
                        TextView textViewLabel = (TextView) textViewElement.findViewById(R.id.form_text_view_label);
                        TextView textViewValue = (TextView) textViewElement.findViewById(R.id.form_text_view_value);
                        ImageView textViewStaticIcon = (ImageView) textViewElement.findViewById(R.id.form_text_view_static_icon);
                        ImageView textViewExpandableIcon = (ImageView) textViewElement.findViewById(R.id.form_text_view_expandable_icon);
                        TextView textViewExpandableText = (TextView) textViewElement.findViewById(R.id.form_text_view_expandable_text);
                        String defaultValueForTextView = field.mDefault;

                        if (field.mLabel != null && !field.mLabel.isEmpty()) {
                            textViewLabel.setText(StringUtils.getTranslatedString(field.mLabel));
                            if (field.mLabelColor != null && !field.mLabelColor.isEmpty()) {
                                textViewLabel.setTextColor(Color.parseColor(field.mLabelColor));
                            } else {
                                textViewLabel.setTextColor(Color.BLACK);
                            }
                            if (field.mUOM != null && !field.mUOM.isEmpty()){
                                textViewLabel.setText(textViewLabel.getText() + " (" + field.mUOM + ")");
                            }
                            if(field.mAligned != null && !field.mAligned.isEmpty()){
                                setAlignment(field.mAligned, textViewLabel);
                            } else {
                                textViewLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            }
                        }
                        textViewValue.setTag(tag + "#" + field.mIdentifier);
                        if (!field.mDisplay) {
                            textViewElement.setVisibility(View.GONE);
                        }
                        String projectFieldValue = ((TabularFormActivity) getActivity()).getValueFromProjectMetaData(field.mIdentifier);
                        if (projectFieldValue != null && !projectFieldValue.isEmpty()) {
                            textViewValue.setText(projectFieldValue);

                            if (tag == null) {
                                ((TabularFormActivity) getActivity()).addToUserEnteredValues(field.mIdentifier, projectFieldValue);
                            } else {
                                // The tag is ---> outerField##index
                                // Split on ## and add the value to the table submission array

                                List<String> fieldInfo = Arrays.asList
                                        (tag.trim().split("##"));

                                ((TabularFormActivity) getActivity())
                                        .addToUserEnteredValuesMap(fieldInfo.get(0),
                                                Integer.parseInt(fieldInfo.get(1)),
                                                field.mIdentifier, projectFieldValue);
                            }
                        } else {
                            textViewValue.setVisibility(View.GONE);
                        }

                        //Check if there is a static icon
                        if (field.mIcon != null) {
                            // Set icon
                            textViewStaticIconLayout.setVisibility(View.VISIBLE);
                            textViewStaticIcon.setImageDrawable(getResources().getDrawable(R.drawable.mi_tank_logo));
                        }
//                        setExpandableProps(field.mExpandable, textViewExpandableLayout, tag, textViewRoot, textViewExpandableIcon, textViewExpandableText);
                        layout.addView(textViewElement);
                    }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    /*
    Deletes entries from map that stores user entered data
     */
    private void deleteFromNewPageTable(String key, int index) {
        Map<String, LinkedHashMap<Integer, Map<String, String>>> userEnteredValuesForTablesMap =
                ((TabularFormActivity) getActivity()).getUserEnteredTableValues();
        if (userEnteredValuesForTablesMap == null || userEnteredValuesForTablesMap.isEmpty()) {
            // Log error
            Utils.logError(UAAppErrorCodes.NULL_POINTER, "User entered values map for tables is null " +
                    "-- continuing without deleting ");
            return;
        }

        if (userEnteredValuesForTablesMap.containsKey(key) && userEnteredValuesForTablesMap.get(key) != null &&
                userEnteredValuesForTablesMap.get(key).containsKey(index)) {
            userEnteredValuesForTablesMap.get(key).remove(index);
        } else {
            // Log error
            Utils.logError(UAAppErrorCodes.NULL_POINTER, "Entry in map for key or index not found " +
                    "-- continuing without deleting ");
        }
    }

    /*
    Add values from project list to table map
     */
    private void addProjectJsonArrayValuesToTableMap(FormField formField) {

        /*
        TODO :
        1. Add from json array
        2. Testing
         */

        // Add any current rows to the addendum field
        String jsonArrayString = ((TabularFormActivity) getActivity()).getValueForKey(formField.mIdentifier);
        Map<String, LinkedHashMap<Integer, Map<String, String>>> tableValues = ((TabularFormActivity) getActivity()).getUserEnteredTableValues();
        LinkedHashMap<Integer, Map<String, String>> linkedHashMap = new LinkedHashMap<>();

        if(jsonArrayString != null && !jsonArrayString.isEmpty()) {
            Utils.logInfo("JSON ARRAY FOR KEY :: " + formField.mIdentifier + "VALUE :: " + jsonArrayString);
            JSONArray jsonArray = null;

            if (tableValues == null) {
                ((TabularFormActivity) getActivity()).initializeUserEnteredTableValues();
                tableValues = ((TabularFormActivity) getActivity()).getUserEnteredTableValues();
            }

            if(tableValues != null) {
                // Add all the entries to the map
                try {
                    jsonArray = new JSONArray(jsonArrayString);
                    for (int i=0; i<jsonArray.length(); i++) {
                        Map<String, String> mapForJsonObject = new HashMap<>();
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Iterator<String> iter = jsonObject.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            Object value = jsonObject.get(key);
                            mapForJsonObject.put(key, String.valueOf(value));
                        }
                        linkedHashMap.put(i, mapForJsonObject);
                    }
                } catch (JSONException e) {
                    Utils.logError(UAAppErrorCodes.JSON_ERROR, "Exception while adding" +
                            "json values to new page table map -- continuing without adding anything to map", e);
                }
            }
        }

        ((TabularFormActivity) getActivity()).addToNewPageSubformTableValuesMap(formField.mIdentifier, linkedHashMap);
    }

    /**
     * Used to render rows of a table
     */
    private void renderNewPageSubformTableFields(LinearLayout layout, FormField formField) {

        /*
        TODO : MAKE SURE THAT THE VARIABLE STATE IN THE ACTIVITY IS SAVED AND RESTORED!

        1. Check if the map has any entries pertaining to this formField

        2. If it does, this field has been loaded before and we do not need
        to copy values from the Json Array into the map. If it does not, that means that
        this field is being rendered for the first time, so get the values from the json array,
        add them to the map and render these map values.

        3. Maintain index for next entry
         */

        Map<String, LinkedHashMap<Integer, Map<String, String>>> tableMap = ((TabularFormActivity) getActivity()).getUserEnteredTableValues();

        if (tableMap != null && formField != null) {
            if (!tableMap.containsKey(formField.mIdentifier)) {
                // Does not contain key, load the values from the Json array into the map
                addProjectJsonArrayValuesToTableMap(formField);
                tableMap = ((TabularFormActivity) getActivity()).getUserEnteredTableValues();
            }

            // Clear any children in the layout
            if (layout.getChildCount() > 0) {
                layout.removeAllViews();
            }

            // Render values from the map
            if (formField.mTableStructure != null && !formField.mTableStructure.isEmpty() && tableMap.containsKey(formField.mIdentifier)) {

                LinkedHashMap<Integer, Map<String, String>> linkedHashMap = tableMap.get(formField.mIdentifier);

                for (TableRow tableRow : formField.mTableStructure) {

                    if (tableRow.mRepeat) {
                        int weightSum = tableRow.mWeightSum;
                        String background = tableRow.mBackgroundColor;
                        String foreground = tableRow.mForegroundColor;
                        List<FormField> rowComponents = tableRow.mRowComponents;

                        Set<Integer> keys = linkedHashMap.keySet();

//                        if (keys.size() == 0) {
//                            // Maintain Last index
//
//                            ((TabularFormActivity) getActivity()).newPageTableFieldKeyToLastIndexMap.put(formField.mIdentifier, 0);
//                        }

                        int lastIndex = -1;
                        int count = 0;
                        for(Integer k : keys) {
                            if(k > lastIndex) {
                                lastIndex = k;
                            }

                            // Create one row
                            // create parent layout for row
                            RelativeLayout rowParent = new RelativeLayout(getActivity());
                            RelativeLayout.LayoutParams rowParentParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                                    , ViewGroup.LayoutParams.MATCH_PARENT);
                            rowParent.setLayoutParams(rowParentParams);

                            LinearLayout row = new LinearLayout(getActivity());
                            row.setOrientation(LinearLayout.HORIZONTAL);

                            row.setPadding(0, 20, 0, 20);

                            RelativeLayout.LayoutParams rowParams = new RelativeLayout.LayoutParams
                                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            rowParams.addRule(RelativeLayout.ALIGN_START);
//                            rowParams.addRule(RelativeLayout.RIGHT_OF, row.getId());

                            row.setWeightSum(weightSum);
                            row.setLayoutParams(rowParams);

                            if (count % 2 == 0) {
                                row.setBackground(getResources()
                                        .getDrawable(R.drawable.shape_rectangle_blue_grey_edges));
                            } else {
                                row.setBackground(getResources()
                                        .getDrawable(R.drawable.shape_rectangular_grey_edges));
                            }

                            final int index = k;


                            ImageButton rowDeletionButton = new ImageButton(getActivity());
                            rowDeletionButton.setBackground(getResources().getDrawable(R.drawable.minus));
                            RelativeLayout.LayoutParams rowDeletionButtonParams = new RelativeLayout.LayoutParams(50, 50);
                            rowDeletionButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
                            rowDeletionButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            rowDeletionButtonParams.setMargins(10, 8, 15, 10);
                            rowDeletionButton.setLayoutParams(rowDeletionButtonParams);

                            rowDeletionButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteFromNewPageTable(formField.mIdentifier, index);
                                    renderNewPageSubformTableFields(layout, formField);
                                }
                            });

                            rowParent.addView(row);
                            rowParent.addView(rowDeletionButton);

                            Map<String, String> keyToValueMap = linkedHashMap.get(k);

                            for (FormField rowComponent : rowComponents) {

                                if (rowComponent == null) {
                                    continue;
                                }

                                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams
                                        (0, ViewGroup.LayoutParams.MATCH_PARENT);

                                if (rowComponent.mWeight > 0) {
                                    cellParams.weight = rowComponent.mWeight;
                                } else {
                                    cellParams.weight = 1;
                                }

                                // Iterate through row components
                                switch (rowComponent.mUiType) {

                                    case Constants.TABULAR_FORM_TEXT_FIELD:

                                        TextView cellTextView = new TextView(getActivity());

                                        cellTextView.setTextSize(15);

                                        cellTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

                                        cellTextView.setPadding(8, 8, 4, 8);

                                        cellTextView.setLayoutParams(cellParams);

                                        cellTextView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                expandableItemClicked(formField.mExpandable, null , formField.mIdentifier + "##" + k, null);
                                            }
                                        });

                                        String cellValue = keyToValueMap.get(rowComponent.mIdentifier);
                                        if (cellValue != null && !cellValue.isEmpty() && !cellValue.equalsIgnoreCase("null")) {
                                            cellTextView.setText(cellValue);
                                        }

                                        row.addView(cellTextView);


                                        break;

                                    default:
                                        break;
                                }
                            }
                            count++;
                            layout.addView(rowParent);
                        }

                        // Maintain Last index
                        ((TabularFormActivity) getActivity()).newPageTableFieldKeyToLastIndexMap.put(formField.mIdentifier, lastIndex+1);
                    }
                }
            }

        } else {
            // Map is null, log error
            Utils.logError(UAAppErrorCodes.NULL_POINTER, "Map for new page table entries is null" +
                    " -- continuing without rendering");
        }
    }

    private void calculateValueForComputedTextView(String identifier, String value, int index) {

        Map<String, ComputedExpression> keyToComputedDataMap = indexTokeyToComputedDataMap.get(index);
        if (keyToComputedDataMap != null && keyToComputedDataMap.containsKey(identifier)) {

            if (value != null)
                keyToComputedDataMap.get(identifier).setValue(value);
            Map<String, String> keyToValueMap = new HashMap<>();
            for (String key : keyToComputedDataMap.keySet()) {
                String val = keyToComputedDataMap.get(key).getValue();
                if (val != null && !val.isEmpty()) {
                    keyToValueMap.put(key, keyToComputedDataMap.get(key).getValue());
                }
            }
            ComputedExpression computedExpression = keyToComputedDataMap.get(identifier);
            Double computedValue = CustomExpressionEvaluator.evaluateValue(computedExpression.getExpression(), keyToValueMap);
            if (computedValue == Constants.NA_DOUBLE) {
                ((TextView) computedExpression.getView()).setText("-");
            } else {
                DecimalFormat formatter = new DecimalFormat("0.0000");
                ((TextView) computedExpression.getView()).setText(String.valueOf(formatter.format(computedValue)));
            }
        }
    }

    private void setAlignment(String aligned, TextView textView) {
        if (aligned != null) {
            if (aligned.equals(Constants.VIEW_ALIGNMENT_LEFT)) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            } else if (aligned.equals(Constants.VIEW_ALIGNMENT_RIGHT)) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            } else {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        } else {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }

    private TextView createAddendumTextButton(String text) {

        TextView textButton = new TextView(getActivity());
        textButton.setGravity(Gravity.CENTER);
        textButton.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        textButton.setBackground(getResources().getDrawable(R.drawable.round_button));
        textButton.setTextColor(getResources().getColor(R.color.white));
        textButton.setText(text);
        textButton.setTextSize(20);
        textButton.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams rowDeletionTextButtonParams = new RelativeLayout.LayoutParams(80, 80);
        rowDeletionTextButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rowDeletionTextButtonParams.setMargins(10, 8, 8, 10);
        textButton.setLayoutParams(rowDeletionTextButtonParams);
        return textButton;
    }

    private void setInputType(String dataType, EditText editText) {
        switch (dataType) {
            case "int":
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "double":
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case "email":
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case "string":
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "multiplelines":
                editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
            case "password":
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case "phone":
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                Utils.getInstance().setEditTextMaxLength(editText, 10);
                break;
            case "name":
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                Utils.getInstance().setAlphabetFilter(editText);
                break;
        }
    }

    private void renderFormButtons(List<FormButton> buttons, LinearLayout layout) {
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
                button.setText(StringUtils.getTranslatedString(formButton.mLabel));

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (formButton.mExpandable.mType) {
                            case 10:
                                // Cancel : Pop the current fragment and render previous values if present
                                Map<String, LinkedHashMap<Integer, Map<String, String>>> userTableValues =
                                        ((TabularFormActivity) getActivity()).getUserEnteredTableValues();
                                Map<String, String> userValues = ((TabularFormActivity) getActivity()).getmUserEnteredValues();

                                for (String key : userTableValues.keySet()) {
                                    if (mUserRenderedTablevalues.containsKey(key)) {
                                        LinkedHashMap<Integer, Map<String, String>> linkedHashMap = new LinkedHashMap<>();
                                        LinkedHashMap<Integer, Map<String, String>> value = mUserRenderedTablevalues.get(key);
                                        for (Integer key1 : value.keySet()) {
                                            Map<String, String> internalMap = new HashMap<>();
                                            internalMap.putAll(value.get(key1));
                                            linkedHashMap.put(key1, internalMap);
                                        }
                                        ((TabularFormActivity) getActivity()).addToTableValueMap(key, linkedHashMap);
                                    }
                                }

                                for (String fieldKey : userValues.keySet()){
                                    if (mUserRenderedValues.containsKey(fieldKey)){
                                        String str = mUserRenderedValues.get(fieldKey);
                                        ((TabularFormActivity) getActivity()).addToUserEnteredValues(fieldKey, str);
                                    }
                                }
                                ((TabularFormActivity) getActivity()).popCurrentFragment();
                                break;

                            case 11:
                                // Edit (New Fragment) new fields from sub-form, in 'Expandables' object
                                ClientValidationResponse onContinueValidationResponse = new ClientValidationResponse().ok();

                                ((TabularFormActivity) getActivity()).showProgressBar();
                                ((TabularFormActivity) getActivity()).disableUI();
                                ((TabularFormActivity) getActivity()).allowBackPress = false;

                                validate(mForm, onContinueValidationResponse, -1, null);
                                ((TabularFormActivity) getActivity()).hideProgressBar();
                                ((TabularFormActivity) getActivity()).enableUI();
                                if (onContinueValidationResponse.mIsValid) {
                                    for (String fieldKey : addendumKeyToValueMap.keySet()) {
                                        LinkedHashMap<Integer, Map<String, String>> indexToKeyToValueMap = addendumKeyToValueMap.get(fieldKey);
                                        ((TabularFormActivity) getActivity()).addToTableValueMap(fieldKey, indexToKeyToValueMap);
                                    }
                                    expandableItemClicked(formButton.mExpandable, layout, null, null);
                                } else {
                                    if (onContinueValidationResponse.mMessage == null) {
                                        onContinueValidationResponse.mMessage = "";
                                    }
                                    for (String key : onContinueValidationResponse.mKeyToErrorMessage.keySet()) {
                                        onContinueValidationResponse.mMessage += "\n" + onContinueValidationResponse.mKeyToErrorMessage.get(key);
                                    }
                                    createDialog(onContinueValidationResponse.mMessage, false);
                                }

                                break;

                            case 12:
                                // Preview the form
                                break;

                            case 13:
                                // Submit the form
                                // add addendum view data to TabularFormActivity tableValueMap

                                submitClicked = true;
                                if (rowAdditionTextButton != null) {
                                    rowAdditionTextButton.callOnClick();
                                }
                                submitClicked = false;

                                for (String fieldKey : addendumKeyToValueMap.keySet()) {
                                    LinkedHashMap<Integer, Map<String, String>> indexToKeyToValueMap = addendumKeyToValueMap.get(fieldKey);
                                    ((TabularFormActivity) getActivity()).addToTableValueMap(fieldKey, indexToKeyToValueMap);
                                }
                                ClientValidationResponse clientValidationResponse = new ClientValidationResponse().ok();

                                validate(mForm, clientValidationResponse, -1, null);
                                System.out.println("VALIDATION RESULT " + clientValidationResponse.mIsValid + " " + clientValidationResponse.mKeyToErrorMessage + " " + clientValidationResponse.mMessage);

                                ((TabularFormActivity) getActivity()).showProgressBar();
                                ((TabularFormActivity) getActivity()).disableUI();
                                ((TabularFormActivity) getActivity()).allowBackPress = false;

                                if (clientValidationResponse.mIsValid) {
                                    // deleting media from storage and db (removed by the user)
                                    ((TabularFormActivity) getActivity()).deletedMediaFromDBAndStorage();

                                    // Check if data is available for submission
                                    boolean dataPresent = checkIfDataIsPresentForSubmission();
                                    if (!dataPresent) {
                                        ((TabularFormActivity) getActivity()).hideProgressBar();
                                        Toast.makeText(getActivity(), getResources().getString(R.string.NO_DATA_FOR_SUBMISSION), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
//                                // Create the final submission array from the submission maps
                                    JSONArray submissionArray = ((TabularFormActivity) getActivity()).createSubmissionArray();
                                    printSubmissionArray(submissionArray);
                                    // Creating ContentValues for inserting into DB
                                    ContentValues values = mDBObjectCreationUtils.createFormSubmissionEntry(submissionArray, formButton,
                                            ((TabularFormActivity) getActivity()).getAppId(),
                                            ((TabularFormActivity) getActivity()).getUpdateForm().mProjectFormId,
                                            ((TabularFormActivity) getActivity()).getUpdateForm().mMetaDataInstanceId,
                                            mProject);

                                    Utils.logInfo(LogTags.PROJECT_SUBMIT, "Content Values created for project submission -- " + values.toString());


                                    // List of UUIDs that are part of this submission
                                    List<String> mediaUUids = new ArrayList<>();

                                    mediaUUids.addAll(((TabularFormActivity) getActivity()).getImageUUIDsFromSubmissionArray(submissionArray));

                                    for (String uuid : mediaUUids) {
                                        Utils.logInfo(LogTags.PROJECT_SUBMIT, "Media Uuid for project submission -- " + uuid);
                                    }

                                    Utils.logInfo(LogTags.PROJECT_SUBMIT, "XXXX: RealTieme Project submission from FORM -- ");

                                    ArrayDataSubmissionThread pst = new ArrayDataSubmissionThread(values, mediaUUids, (TabularFormActivity) getActivity());
                                    pst.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    if (clientValidationResponse.mMessage == null) {
                                        clientValidationResponse.mMessage = "";
                                    }
                                    for (String key : clientValidationResponse.mKeyToErrorMessage.keySet()) {
                                        clientValidationResponse.mMessage += "\n" + clientValidationResponse.mKeyToErrorMessage.get(key);
                                    }
                                    createDialog(clientValidationResponse.mMessage, false);
                                }

                                break;

                            case 14:
                                // Save the current fields of the fragment

                                ClientValidationResponse onSaveValidationResponse = new ClientValidationResponse().ok();

                                ((TabularFormActivity) getActivity()).showProgressBar();
                                ((TabularFormActivity) getActivity()).disableUI();
                                ((TabularFormActivity) getActivity()).allowBackPress = false;

                                if(mTag == null || mTag.isEmpty()) {
                                    validate(mForm, onSaveValidationResponse, -1, null);
                                } else {
                                    List<String> tagInfo = Arrays.asList
                                            (mTag.trim().split("##"));

                                    validate(mForm, onSaveValidationResponse,  Integer.parseInt(tagInfo.get(1)), tagInfo.get(0));
                                }

                                ((TabularFormActivity) getActivity()).hideProgressBar();
                                ((TabularFormActivity) getActivity()).enableUI();
                                if (onSaveValidationResponse.mIsValid) {

                                    // deleting media from storage and db (removed by the user)
                                    ((TabularFormActivity) getActivity()).deletedMediaFromDBAndStorage();

                                    for (String fieldKey : addendumKeyToValueMap.keySet()) {
                                        LinkedHashMap<Integer, Map<String, String>> indexToKeyToValueMap = addendumKeyToValueMap.get(fieldKey);
                                        ((TabularFormActivity) getActivity()).addToTableValueMap(fieldKey, indexToKeyToValueMap);
                                    }

                                    ((TabularFormActivity) getActivity()).popCurrentFragment();
                                } else {
                                    if (onSaveValidationResponse.mMessage == null) {
                                        onSaveValidationResponse.mMessage = "";
                                    }
                                    for (String key : onSaveValidationResponse.mKeyToErrorMessage.keySet()) {
                                        onSaveValidationResponse.mMessage += "\n" + onSaveValidationResponse.mKeyToErrorMessage.get(key);
                                    }
                                    createDialog(onSaveValidationResponse.mMessage, false);
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

    private void createDialog(String message, boolean shouldFinishActivity) {
        // create a Dialog component
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        ((TabularFormActivity) getActivity()).enableUI();
        ((TabularFormActivity) getActivity()).allowBackPress = true;

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.project_submission_alert_dialog);

        TextView txt = dialog.findViewById(R.id.project_submission_message);

        txt.setText(message);

        Button dialogButton = dialog.findViewById(R.id.project_submission_close);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                if (shouldFinishActivity) {
                    getActivity().finish();
                }
//                ((TabularFormActivity) getActivity()).hideProgressBar();
            }
        });

        dialog.show();
        ((TabularFormActivity) getActivity()).hideProgressBar();

    }

    private void expandableItemClicked(ExpandableComponent component, LinearLayout layout, String tag, Map<String, String> fragmentTransitionInfo) {

        if (component != null && component.mSubForm != null && !component.mSubForm.isEmpty()) {

            Form form = ((TabularFormActivity) getActivity()).getFormFromConfiguration(component.mSubForm);

            if (form != null) {

                switch (component.mType) {
                    case 0: // Render on a new form
                        ((TabularFormActivity) getActivity()).addNewFormFragment(form, tag, fragmentTransitionInfo);
                        break;
                    case 11: // Render on a new form
                    ((TabularFormActivity) getActivity()).addNewFormFragment(form, tag, fragmentTransitionInfo);
                    break;
                    case 1: // Render on the same page
                        renderFormFields(form.mFormFields, layout, tag);
                        break;
                }
            }
        }
    }

    private void printSubmissionArray(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Utils.logInfo(jsonArray.get(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void validate(Form form, ClientValidationResponse clientValidationResponse, int index, String parentKey) {

        if (form != null && form.mFormFields != null && !form.mFormFields.isEmpty()) {

            for (FormField formField : form.mFormFields) {

                if (formField.mUiType != null) {

                    switch (formField.mUiType) {

                        case Constants.TABULAR_FORM_TABLE_FIELD:

                            // Have to iterate through all rows and all the cells for that row
                            validateTable(formField, clientValidationResponse);

                            break;

                        case Constants.TABULAR_FORM_SPINNER_FIELD:

                            validateField(formField, clientValidationResponse, index, parentKey);

                            break;

                        case Constants.TABULAR_FORM_EDITTEXT_FIELD:

                            validateField(formField, clientValidationResponse, index, parentKey);

                            break;

                        case Constants.TABULAR_FORM_NEW_PAGE_TABLE_FIELD:

                            validateTable(formField, clientValidationResponse);
                            break;

                        case Constants.TABULAR_FORM_GEOTAGGED_IMAGE_FUSED_FIELD:
                            validateField(formField, clientValidationResponse, index, parentKey);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }


    private void validateTable(FormField formField, ClientValidationResponse clientValidationResponse) {

        int index = 0;

        if (formField != null && formField.mTableStructure != null && !formField.mTableStructure.isEmpty()) {

            String jsonArrayString = ((TabularFormActivity) getActivity()).getValueForKey(formField.mIdentifier);

            if (jsonArrayString == null || jsonArrayString.isEmpty()) {
                return;
            }
            JSONArray jsonArray = null;

            try {
                jsonArray = new JSONArray(jsonArrayString);

                // Iterating through rows
                for (int i = 0; i < formField.mTableStructure.size(); i++) {


                    // Check for repeat rows only
                    if (formField.mTableStructure.get(i).mRepeat) {


                        List<FormField> rowComponents = new ArrayList<>();
                        rowComponents.addAll(formField.mTableStructure.get(i).mRowComponents);

                        // Iterating through the JSON Array
                        for (int j = 0; j < jsonArray.length(); j++) {

                            // For each JSON Object iterate through all the row components
                            for (FormField rowComponent : rowComponents) {

                                validateRowComponent(rowComponent, clientValidationResponse, index, formField.mIdentifier);
                            }
                            index++;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateRowComponent(FormField rowComponent, ClientValidationResponse clientValidationResponse, int index, String parentKey) {

        if (rowComponent != null && rowComponent.mUiType != null && !rowComponent.mUiType.isEmpty()) {

            switch (rowComponent.mUiType) {

                case Constants.TABULAR_FORM_TEXT_FIELD:
                    break;

                case Constants.TABULAR_FORM_RADIO_FIELD:

                    validateField(rowComponent, clientValidationResponse, index, parentKey);

                    break;

                default:
                    break;
            }
        }
    }

    private void validateField(FormField field, ClientValidationResponse clientValidationResponse, int index, String parentKey) {

        String value = null;
        Map<String, String> keyToValueMap = getKeyToValueMap(parentKey, index);

        value = keyToValueMap.get(field.mIdentifier);
        AppValidationService appValidationService = new AppValidationService();
        // Have to check for mandatory validation
        if (field.mValidation != null && field.mValidation.mMandatory) {

            if (value == null || value.isEmpty()) {
                // Validation failed
                // Add to validation object
                clientValidationResponse.mIsValid = false;
                String keyToDisplay = field.mIdentifier;
                if (keyToDisplay.contains("$")) {
                    String[] comps = keyToDisplay.split("\\$");
                    keyToDisplay = comps[comps.length - 1];
                    keyToDisplay = keyToDisplay.replaceAll("_", " ");
                    keyToDisplay = keyToDisplay.substring(0, 1).toUpperCase() + keyToDisplay.substring(1);
                }
                if (parentKey == null)
                    clientValidationResponse.mKeyToErrorMessage.put(field.mIdentifier, field.mLabel + " " + getResources().getString(R.string.MANDATORY_FIELD));
                else
                    clientValidationResponse.mKeyToErrorMessage.put(field.mIdentifier, parentKey + "-" + field.mLabel + " " + getResources().getString(R.string.MANDATORY_FIELD));
            }
        }

        if (value != null) {
            // Check for SPEL expression
            appValidationService.validateEnteredField(field.mIdentifier, field.mValidation, value, field.mDatatype, keyToValueMap, clientValidationResponse);

            // Have to check for any expandable components
            if (field.mMultipleValues != null && !field.mMultipleValues.isEmpty()) {

                for (MultipleValues multipleValue : field.mMultipleValues) {

                    if (value.equals(multipleValue.mValue)) {

                        if (multipleValue.mExpandable != null) {

                            // Get form using this subform Id and call validate function
                            Form form = ((TabularFormActivity) getActivity())
                                    .getFormFromConfiguration(multipleValue.mExpandable.mSubForm);

                            // Validating the sub-form
                            validate(form, clientValidationResponse, index, parentKey);
                        }
                    }
                }
            }

        }
    }

    private Map<String, String> getKeyToValueMap(String parentKey, int index) {
        Map<String, String> keyToValueMap = new HashMap<>();
        if (parentKey == null && index < 0) {
            keyToValueMap = ((TabularFormActivity) getActivity()).getmUserEnteredValues();

        } else {
            if (((TabularFormActivity) getActivity()).getUserEnteredTableValues().containsKey(parentKey) && ((TabularFormActivity) getActivity()).getUserEnteredTableValues().get(parentKey).containsKey(index)) {
                if (((TabularFormActivity) getActivity()).getUserEnteredTableValues().get(parentKey).get(index) != null)
                    keyToValueMap = ((TabularFormActivity) getActivity()).getUserEnteredTableValues().get(parentKey).get(index);
            }
        }
        return keyToValueMap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

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
                                // Create thumbnails
                                createNewImageThumbnail(formImage, mCurrentImageLinearLayout, mCurrentImageUuids);

                                Map<String, String> additional_prop_map = formImage.getAdditionalProps();
                                String ext_proj_id = mProject.mExtProjectId;
                                additional_prop_map.put("ext_proj_id", ext_proj_id);
                                formImage.setAdditionalProps(additional_prop_map);

                                // Adding image to Form database
                                if (UAAppContext.getInstance().getDBHelper().getFormMedia(formImage.getmUUID(),
                                        formImage.getmAppId(), formImage.getmUserId()) == null) {
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
                            FormMedia formImage = UAAppContext.getInstance().getDBHelper().getFormMedia(uuid,
                                    ((TabularFormActivity) getActivity()).getAppId(), UAAppContext.getInstance().getUserID());
                            if (formImage != null) {
                                createNewImageThumbnail(formImage, mCurrentImageLinearLayout, mCurrentImageUuids);
                                count = 1;
                            }
                        }
                        if (count == 0) {
                            mCurrentImageUuids.setText("");
                            ((TabularFormActivity) getActivity()).showErrorMessageAndFinishActivity
                                    (Constants.NO_IMAGES_TAKEN, false);
                        }
                    }
                } else {
                    String uuids = mCurrentImageUuids.getText().toString();
                    List<String> prevImgs;
                    ArrayList<String> prevImgsUUIDsWithLonLat = new ArrayList<>();
                    prevImgsUUIDsWithLonLat.addAll(Arrays.asList(uuids.split("\\s*,\\s*")));
                    prevImgs = Utils.getImageUUIDList(prevImgsUUIDsWithLonLat);
                    mCurrentImageLinearLayout.removeAllViews();

                    for (String uuid : prevImgs) {
                        FormMedia formImage = UAAppContext.getInstance().getDBHelper()
                                .getFormMedia(uuid, ((TabularFormActivity) getActivity()).getAppId()
                                        , UAAppContext.getInstance().getUserID());
                        if (formImage != null) {
                            createNewImageThumbnail(formImage, mCurrentImageLinearLayout, mCurrentImageUuids);
                        }
                    }
                    ((TabularFormActivity) getActivity()).showErrorMessageAndFinishActivity
                            (Constants.SOMETHING_WENT_WRONG, false);
                }
                break;
        }
    }

    /*
    This method creates a thumbnail for the images returned from the camera activity.
    These images can be viewed or removed from this thumbnail.
     */
    private void createNewImageThumbnail(FormMedia formImage, LinearLayout currentImageLayout, TextView imageUuids) {

        OpenCameraImageThumbnail imgThumbnail = new OpenCameraImageThumbnail(getActivity());
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
                ((TabularFormActivity)getActivity()).addToDeletedMediaUUIDs(formImage.getmUUID());

//                UAAppContext.getInstance().getDBHelper().deleteFormMedia(formImage.getmUUID());
//
//                String fileName = imgFile.getName().substring(0, imgFile.getName().indexOf("."));
//                if(formImage.getmUUID().equals(fileName)) {
//                    Utils.getInstance().deleteImageFromStorage(getActivity(), formImage.getLocalPath());
//                }
            }
        });

        currentImageLayout.addView(imgThumbnail, 0);
        mCurrentImageLinearLayout = currentImageLayout;
        mCurrentImageUuids = imageUuids;
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
                                    ((TabularFormActivity) getActivity()).setUserLatitude(location.getLatitude());
                                    ((TabularFormActivity) getActivity()).setUserLongitude(location.getLongitude());
                                    ((TabularFormActivity) getActivity()).setAccuracy(location.getAccuracy());
                                } else {
                                    System.out.println("LOCATIONUPDATE : Location is null!");
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
            intent = new Intent(getActivity(), NewCamera2Activity.class);
        } else {
            intent = new Intent(getActivity(), CameraActivity.class);
        }

        intent.putExtra("tag", tag);
        intent.putExtra("datatype", datatype);
        intent.putExtra("initialts", System.currentTimeMillis());
        intent.putExtra("projectId", mProject.mProjectId);
        intent.putExtra("max", max);
        intent.putExtra("key", key);
        intent.putExtra("appId", ((TabularFormActivity) getActivity()).getAppId());
        intent.putExtra("userId", UAAppContext.getInstance().getUserID());
        intent.putExtra("lat", lat);
        intent.putExtra("lon", lon);
        intent.putStringArrayListExtra("uuids", (ArrayList<String>) uuids);
        intent.putExtra("uitype", uiType);
        if (gpsValidation != null) {
            // Have to perform validation
            intent.putExtra("gps_validation", gpsValidation);
        }
        HashMap<String, String> submittedFieldsMap = convertJSONObjectToStringInMap(new HashMap<>());
        intent.putExtra("submittedFields", submittedFieldsMap);

        intent.putStringArrayListExtra("uuids", (ArrayList<String>) uuids);
        startActivityForResult(intent, Constants.FORM_GET_IMAGES);

    }

    private String getValueForKey(String fieldKey, String tag) {
        String value;
        if ((tag == null || tag.isEmpty()) && mAdditionalInfo != null && mAdditionalInfo.get(fieldKey) != null) {
            return mAdditionalInfo.get(fieldKey);
        }
        if (tag == null) {
            value = ((TabularFormActivity) getActivity())
                    .getValueForKey(fieldKey);
        } else {
            // The tag is ---> outerField##index
            // Split on ## and get the value from the table submission array

            List<String> fieldInfo = Arrays.asList
                    (tag.trim().split("##"));

            value = ((TabularFormActivity) getActivity())
                    .getValueForKey(fieldInfo.get(0),
                            Integer.parseInt(fieldInfo.get(1)),
                            fieldKey);
        }
        return value;
    }

    private void renderFormHeaders(List<Header> headers, LinearLayout layout, String tag) {

        Utils.logInfo("HEADERS SUBMISSION ARRAY : : Key  -  " + headers.size());
        // Iterating through all the form headers
        for (Header header : headers) {
            Utils.logInfo("HEADERS SUBMISSION ARRAY : : ELEMENT  -  " + header);
            // Checking with all the ProjectListValues
            if (header != null) {
                String value = null;
                if (header.mIdentifier != null) {
                    value = getValueForKey(header.mIdentifier, tag);
                }
                if (header.mDisplay) {
                    View view = loadHeaderView(layout, header, true, false, false, value);
                    layout.addView(view);
                }
                if (header.mSubmittable) {
                    Utils.logInfo("ADD SUBMIIATBLE HEADER FIELD TO SUBMISSION ARRAY : : Key  -  " + header.mIdentifier + " Value - " + value);
                    ((TabularFormActivity) getActivity()).addToUserEnteredValues(header.mIdentifier, value);
                    Utils.logInfo("ADDED SUBMIIATBLE HEADER FIELD TO SUBMISSION ARRAY : : Key  -  " + header.mIdentifier + " Value - " + ((TabularFormActivity) getActivity()).getValueForKey(header.mIdentifier));
                }
            }
        }
    }

    private View loadHeaderView(LinearLayout layout, Header header, boolean isFromProjectList,
                                boolean isImageComponent, boolean isVideo, String valueText) {
        View view = mInflater.inflate(R.layout.form_header_element, layout, false);

        RelativeLayout staticIconLayout = view.findViewById(R.id.form_header_static_icon_layout);
        ImageView staticIcon = view.findViewById(R.id.form_header_static_icon);
        TextView label = view.findViewById(R.id.form_header_label);
        TextView value = view.findViewById(R.id.form_header_value);
        HorizontalScrollView imageScrollView = view
                .findViewById(R.id.form_header_image_group_scroll_view);
        LinearLayout imageGroupLayout = view.findViewById(R.id.form_header_image_group);

        RelativeLayout headerLabelLayout = view.findViewById(R.id.form_header_label_layout);
        RelativeLayout headerValueLayout = view.findViewById(R.id.form_header_value_layout);

        String labelText = StringUtils.getTranslatedString(header.mValue);

        if (labelText == null || labelText.isEmpty()) {
            headerLabelLayout.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) value.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            value.setLayoutParams(layoutParams);
        } else {
            label.setText(StringUtils.getTranslatedString(labelText));
        }

//        headerLabelLayout.setVisibility(View.GONE);
        if (header.mIconUrl != null) {
//            staticIconLayout.setVisibility(View.VISIBLE);
//            staticIcon.setImageResource(R.drawable.mi_tank_logo);
        }

        if (isFromProjectList && value != null && valueText != null && !valueText.isEmpty()) {
            value.setText(valueText);
        } else {
            if (isImageComponent) {
                value.setVisibility(View.GONE);
                imageScrollView.setVisibility(View.VISIBLE);
//                loadPreviewImages(valueText, imageGroupLayout);
            } else if (isVideo) {
                value.setVisibility(View.GONE);
                imageScrollView.setVisibility(View.VISIBLE);
//                loadPreviewVideo(valueText, imageGroupLayout);
            } else if (value != null) {
                value.setText(StringUtils.getTranslatedString(valueText));
            }
        }

        return view;
    }

    public void addKeyToValueMapAtIndex(String fieldKey, Map<String, String> keyToValueMap, int index) {
        if (addendumKeyToValueMap == null) {
            addendumKeyToValueMap = new HashMap<>();
        }
        if (!addendumKeyToValueMap.containsKey(fieldKey) || addendumKeyToValueMap.get(fieldKey) == null) {
            addendumKeyToValueMap.put(fieldKey, new LinkedHashMap<>());
        } else {
            addendumKeyToValueMap.get(fieldKey).put(index, keyToValueMap);
        }

    }

    public void removeKeyToValueMapFromIndex(String fieldKey, int index) {
        if (addendumKeyToValueMap != null && addendumKeyToValueMap.containsKey(fieldKey) && addendumKeyToValueMap.get(fieldKey) != null) {
            addendumKeyToValueMap.get(fieldKey).remove(index);
        }
    }

    private Map<Integer, Map<String, String>> getAddendumFieldValue(String fieldKey) {
        LinkedHashMap<Integer, Map<String, String>> indexToKeyToValueMap = new LinkedHashMap<>();
        Map<String, LinkedHashMap<Integer, Map<String, String>>> fieldkeyToIndexToKeyToValueMap = ((TabularFormActivity) getActivity()).getUserEnteredTableValues();
        if (fieldkeyToIndexToKeyToValueMap == null || fieldkeyToIndexToKeyToValueMap.get(fieldKey) == null || fieldkeyToIndexToKeyToValueMap.get(fieldKey).isEmpty()) {
            // TODO:: JSON Object To Map
            String jsonArrayString = ((TabularFormActivity) getActivity()).getValueForKey(fieldKey);
//                                    String jsonArrayString = getValueFromProject(field.mIdentifier);
            Utils.logInfo("JSON ARRAY FOR KEY :: " + fieldKey + "VALUE :: " + jsonArrayString);
            JSONArray jsonArray = null;
            if (jsonArrayString == null || jsonArrayString.isEmpty()) {
                addendumKeyToValueMap.put(fieldKey, indexToKeyToValueMap);
                return indexToKeyToValueMap;
            }
            try {
                jsonArray = new JSONArray(jsonArrayString);

                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int k = 0; k < jsonArray.length(); k++) {
                        Map<String, String> keyToValueMap = new HashMap<>();

                        JSONObject jsonObject = jsonArray.getJSONObject(k);
                        Iterator<String> iter = jsonObject.keys();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            try {
                                String value = jsonObject.getString(key);
                                keyToValueMap.put(key, value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                // Something went wrong!
                            }
                        }

                        indexToKeyToValueMap.put(k, keyToValueMap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {

            Map<Integer, Map<String, String>> indexToKeyToValueMapFromFormActivity = fieldkeyToIndexToKeyToValueMap.get(fieldKey);
            for (int index : indexToKeyToValueMapFromFormActivity.keySet()) {
                Map<String, String> keyToValueMap = indexToKeyToValueMapFromFormActivity.get(index);
                indexToKeyToValueMap.put(index, keyToValueMap);
            }
        }
        addendumKeyToValueMap.put(fieldKey, indexToKeyToValueMap);
        return indexToKeyToValueMap;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

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
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() +
                    (Integer.parseInt(mSelectableDates.mFuture) * 86400000));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() +
                    (Integer.parseInt(mSelectableDates.mPast) * 86400000));

            return datePickerDialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            mDateText.setText(i2 + "/" + (i1 + 1) + "/" + (i));
        }
    }
    public void autoPopulateDependentFields(String sourceKey, String tag) {
        sourceKey = sourceKey  + (tag != null  && !tag.isEmpty()? "$$" + tag : "");
        ((TabularFormActivity)getActivity()).autoPopulateDependentFields(sourceKey);

    }
    public void autoPopulateField(String targetKey) {
        ((TabularFormActivity)getActivity()).autoPopulateField(targetKey);
    }
    private String getTranslatedValue(String key) {
        return StringUtils.getTranslatedString(key);
    }

    private boolean checkIfDataIsPresentForSubmission(){
        boolean present = true;
        int count = 0;
        Map<String, LinkedHashMap<Integer, Map<String, String>>> userEnteredTableValues =
                ((TabularFormActivity) getActivity()).getUserEnteredTableValues();
        if (userEnteredTableValues != null && !userEnteredTableValues.isEmpty()) {
            for (String key : userEnteredTableValues.keySet()) {
                if (userEnteredTableValues.get(key) != null && !userEnteredTableValues.get(key).isEmpty()) {
                    count++;
                }
            }
            if (count == 0) {
                present = false;
            }
        }
        return present;
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
}
