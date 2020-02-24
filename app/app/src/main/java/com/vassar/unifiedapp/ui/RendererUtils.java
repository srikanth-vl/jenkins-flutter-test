package com.vassar.unifiedapp.ui;

import android.text.InputType;
import android.view.View;

import com.vassar.unifiedapp.model.FormField;

import org.json.JSONException;
import org.json.JSONObject;

public class RendererUtils {

    public int getAlignment(String alignment) {
        if (alignment != null) {
            switch (alignment) {
                case "left":
                    return View.TEXT_ALIGNMENT_TEXT_START;
                case "center":
                    return View.TEXT_ALIGNMENT_CENTER;
                case "right":
                    return View.TEXT_ALIGNMENT_TEXT_END;
            }
        }
        return View.TEXT_ALIGNMENT_CENTER;
    }

    public int getInputType(String datatype) {
        switch (datatype) {
            case "int":
                return InputType.TYPE_CLASS_NUMBER;
            case "double":
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
            case "email":
                return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            case "string":
                return InputType.TYPE_CLASS_TEXT;
            case "multiplelines":
                return InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            case "password":
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
        }
        return InputType.TYPE_CLASS_TEXT;
    }

    public JSONObject getJsonObjectToSave(FormField formField, String value, String uom) {
        JSONObject formFieldJsonToSave = new JSONObject();
        try {
            formFieldJsonToSave.put("key", formField.mIdentifier);
            formFieldJsonToSave.put("dt", formField.mDatatype);
            formFieldJsonToSave.put("ui", formField.mUiType);
            formFieldJsonToSave.put("val", value);
            if (uom != null) {
                formFieldJsonToSave.put("uom", uom);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return formFieldJsonToSave;
    }
}
