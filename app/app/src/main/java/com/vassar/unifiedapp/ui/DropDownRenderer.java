package com.vassar.unifiedapp.ui;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.vassar.unifiedapp.R;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.MultipleValues;
import com.vassar.unifiedapp.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DropDownRenderer {

    public Spinner loadDropDown(FormField formField, LayoutInflater mInflater, String tag, View spinnerElement, FragmentActivity activity) {

        RelativeLayout spinnerStaticIconLayout = (RelativeLayout) spinnerElement.findViewById(R.id.form_spinner_static_icon_layout);
        TextView spinnerLabel = (TextView) spinnerElement.findViewById(R.id.form_spinner_label);
        Spinner spinner = (Spinner) spinnerElement.findViewById(R.id.form_spinner);
        ImageView spinnerStaticIcon = (ImageView) spinnerElement.findViewById(R.id.form_spinner_static_icon);

        String actualLabel = formField.mLabel;
        String TranslatedLabel = getLabel(formField.mLabel);
        spinnerLabel.setText(TranslatedLabel);
        spinner.setTag(tag + "#" + formField.mIdentifier);

        ArrayAdapter<String> spinnerArrayAdapter = null;

        ArrayList<MultipleValues> spinnerMultipleValues = formField.mMultipleValues == null ? new ArrayList<>(): formField.mMultipleValues;
        String values[] = new String[spinnerMultipleValues.size()];
        for (int i = 0; i < spinnerMultipleValues.size(); i++) {
            values[i] = getLabel(spinnerMultipleValues.get(i).mValue);
        }

        //Check if there is a static icon
        if (formField.mIcon != null) {
            // Set icon
            spinnerStaticIconLayout.setVisibility(View.VISIBLE);
            spinnerStaticIcon.setImageDrawable(activity.getResources().getDrawable(R.drawable.mi_tank_logo));
        }

        spinnerArrayAdapter = new ArrayAdapter<String>
                (activity, R.layout.form_spinner_item_layout, values);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        return spinner;
    }

    public JSONObject getJsonObjectToSave(FormField formField, int position, String selectedValue) {
        JSONObject formFieldJsonToSave = new JSONObject();
        try {
            formFieldJsonToSave.put("key", formField.mIdentifier);
            formFieldJsonToSave.put("dt", formField.mDatatype);
            if(formField.mMultipleValues != null) {
                formFieldJsonToSave.put("val", formField.mMultipleValues.get(position).mValue);
            } else {
                formFieldJsonToSave.put("val", selectedValue);
            }
            formFieldJsonToSave.put("ui", formField.mUiType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return formFieldJsonToSave;
    }
    public String getLabel(String label) {
        if(label != null && !label.isEmpty()) {
            label = StringUtils.getTranslatedString(label);
        }
        return label;
    }
}
