package com.vassar.unifiedapp.model;

import java.util.LinkedHashMap;

public class ClientValidationResponse {

    public boolean mIsValid;
    public String mMessage;
    public LinkedHashMap<String, String> mKeyToErrorMessage;

    public ClientValidationResponse ok() {
        ClientValidationResponse clientValidationResponse = new ClientValidationResponse();
        clientValidationResponse.mIsValid = true;
        clientValidationResponse.mKeyToErrorMessage = new LinkedHashMap<>();
        return clientValidationResponse;
    }
}
