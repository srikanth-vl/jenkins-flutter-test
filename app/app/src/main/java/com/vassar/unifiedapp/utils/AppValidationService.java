package com.vassar.unifiedapp.utils;

import com.vassar.unifiedapp.model.ClientValidationResponse;
import com.vassar.unifiedapp.model.Validation;
import com.vassar.unifiedapp.model.ValidationExpression;
import com.vassar.unifiedapp.validation.SPELExpressionValidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppValidationService {

    public void validateEnteredField(String mIdentifier, Validation mValidation, String value, String datatype, Map<String, String> keyToValueMap, ClientValidationResponse clientValidationResponse) {
        if(mValidation == null) {
            return;
        }
        ArrayList<ValidationExpression> spelValidationExpresionList = mValidation.mExpr;
        if(spelValidationExpresionList == null || spelValidationExpresionList.isEmpty()) {
            return;
        }
        if(mValidation.mMandatory &&
                (value == null || value.isEmpty() || value.equals("-"))) {
            consolidateClientValidationObject(mIdentifier, mIdentifier + " is a mandatory field. Please enter value.", false, clientValidationResponse);
        }

        for(ValidationExpression validationExpression : spelValidationExpresionList) {
            String spelExpression = validationExpression.mExpression;
            if (spelExpression != null && !spelExpression.isEmpty()) {

                SPELExpressionValidator spelExpressionValidator = new SPELExpressionValidator();
                Boolean isExpressionValid = spelExpressionValidator.validateSPELExpression(validationExpression.mExpression, keyToValueMap, datatype, clientValidationResponse);

                if (isExpressionValid != null && !isExpressionValid) {
                    consolidateClientValidationObject(mIdentifier, validationExpression.mErrorMessage, false, clientValidationResponse);
                }
            }
        }
    }

    public void consolidateClientValidationObject(String key, String errorMessage, boolean isValid, ClientValidationResponse clientValidationResponse) {
        if(clientValidationResponse == null) {
            clientValidationResponse = new ClientValidationResponse();
            clientValidationResponse.mIsValid = true;
        }
        if(!isValid) {
            clientValidationResponse.mIsValid = isValid;
            if(clientValidationResponse.mKeyToErrorMessage == null) {
                clientValidationResponse.mKeyToErrorMessage = new LinkedHashMap<>();
            }
            clientValidationResponse.mKeyToErrorMessage.put(key, errorMessage);
        }
    }

}
