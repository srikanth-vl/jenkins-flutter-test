package com.vassar.unifiedapp.ui;

import com.vassar.unifiedapp.model.ClientValidationResponse;
import com.vassar.unifiedapp.model.Form;
import com.vassar.unifiedapp.model.FormField;
import com.vassar.unifiedapp.model.MultipleValues;
import com.vassar.unifiedapp.model.ValidationExpression;
import com.vassar.unifiedapp.validation.SPELExpressionValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataValidationService {

    /**
     * This method is called for validating the fields of the current sub form
     */
    public ClientValidationResponse validateFormSubmission(ProjectFormActivity activity, ArrayList<FormField> formFields, HashMap<String, String> superFieldMap, HashMap<String, JSONObject> submittedFields) throws JSONException {

        ClientValidationResponse isValid = new ClientValidationResponse();
        isValid.mIsValid = true;

        if (formFields == null) {
            return isValid;
        }

        Map<String, JSONObject> keyToSubmittedDataMap = new HashMap<>();
        for(String key : submittedFields.keySet()) {
            int index = key.lastIndexOf("#");
            String newKey = key;
            if(index != -1)
                newKey = key.substring(index + 1);
            keyToSubmittedDataMap.put(newKey, submittedFields.get(key));
        }

        for (FormField formField : formFields) {

            if (formField.mValidation != null) {
                // Checking for mandatory fields
                if (formField.mValidation.mMandatory) {
                    if (!keyToSubmittedDataMap.containsKey(formField.mIdentifier)) {
                        isValid.mIsValid = false;
                        isValid.mMessage = formField.mLabel + " is a mandatory field. " + "Cannot be blank!";
                        return isValid;
                    } else {
                        JSONObject jsonObject = keyToSubmittedDataMap.get(formField.mIdentifier);
                        String value = jsonObject.getString("val");
                        if(value == null || value.isEmpty()) {
                            isValid.mIsValid = false;
                            isValid.mMessage = formField.mLabel + " is a mandatory field. " + "Cannot be blank!";
                            return isValid;
                        }
                    }
                }

                switch (formField.mUiType) {
                    case "textview":
                        if (formField.mExpandable != null) {
                            Form form = activity.getFormFromId(formField.mExpandable.mSubForm);
                            if (form != null && form.mFormFields != null) {
                                ClientValidationResponse isSubFormValid = validateFormSubmission(activity, form.mFormFields, superFieldMap, submittedFields);
                                if (!isSubFormValid.mIsValid) {
                                    return isSubFormValid;
                                }
                            }
                        }
                        break;

                    case "toggle":
                    case "dropdown":
                    case "checkbox":
                    case "radio":
                        List<MultipleValues> multipleValues = new ArrayList<>();
                        multipleValues.addAll(formField.mMultipleValues);

                        for (Map.Entry<String, JSONObject> entry : keyToSubmittedDataMap.entrySet()) {

                            String key = entry.getKey();

                            JSONObject jsonObject = entry.getValue();
                            String value = null;
                            if(jsonObject != null)
                                value = jsonObject.getString("val");

                            if (key != null && key.equals(formField.mIdentifier)) {
                                for (MultipleValues multipleValue : multipleValues) {
                                    if (value != null && value.equals(multipleValue.mValue)) {
                                        if (multipleValue.mExpandable != null) {
                                            Form form = activity.getFormFromId(multipleValue.mExpandable.mSubForm);
                                            if (form.mFormFields != null) {
                                                ClientValidationResponse isSubFormValid = validateFormSubmission(activity, form.mFormFields,
                                                        superFieldMap, submittedFields);
                                                if (!isSubFormValid.mIsValid) {
                                                    return isSubFormValid;
                                                }
                                            }
                                        } else {
                                            performSPELValidation(formField.mValidation.mExpr, formField.mDatatype, superFieldMap, isValid);
                                            if(!isValid.mIsValid) return isValid;
                                        }
                                    }
                                }
                            }
                        }
                        break;

                    case "image":
                    case "geotagimage":
                    case "geotag":
                    case "geotagimagefused":
                    case "video":
                        break;

                    case "edittext":
                    case "textbox":
                    case "date":
                    case "timepicker":
                        performSPELValidation(formField.mValidation.mExpr, formField.mDatatype, superFieldMap, isValid);
                        if(!isValid.mIsValid) return isValid;
                }
            }
        }
        return isValid;
    }

    private void performSPELValidation(ArrayList<ValidationExpression> expressionList, String dataType, HashMap<String, String> superFieldMap, ClientValidationResponse isValid) {

        if (expressionList == null || expressionList.isEmpty()) {
            return;
        }
        for (ValidationExpression validationExpression : expressionList) {
            SPELExpressionValidator spelExpressionValidator = new SPELExpressionValidator();
            if (validationExpression.mExpression != null && !validationExpression.mExpression.isEmpty()) {
                Boolean isExpressionValid = spelExpressionValidator
                        .validateSPELExpression(validationExpression.mExpression,
                                superFieldMap,
                                dataType);
                if (isExpressionValid != null && !isExpressionValid) {
                    isValid.mIsValid = false;
                    isValid.mMessage = validationExpression.mErrorMessage;
                }
            }
        }
    }

    public boolean validateMandatoryFields(Map<String, JSONObject> submittedFieldsMap, List<String> mandatoryFields) {
        try {
            List<String> submittedFields = new ArrayList<>();
            for (String key : submittedFieldsMap.keySet()) {
                int lastHashIndex = key.lastIndexOf("#");
                String finalKey;
                if (lastHashIndex == -1) {
                    finalKey = key;
                } else {
                    finalKey = key.substring(lastHashIndex + 1);
                }
                String value = submittedFieldsMap.get(key).getString("val");
                if (value != null && !value.isEmpty()) {
                    submittedFields.add(finalKey);
                }
            }
            mandatoryFields.removeAll(submittedFields);
            if (mandatoryFields.isEmpty()) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
