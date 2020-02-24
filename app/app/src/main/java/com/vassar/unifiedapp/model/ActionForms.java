package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionForms {

    @JsonProperty("initial_form_id")
    public String mInitialFormId;
    @JsonProperty("form")
    public ArrayList<Form> mForms;

    @Override
    public String toString() {
        return "ActionForms{" +
                "mInitialFormId='" + mInitialFormId + '\'' +
                ", mForms=" + mForms +
                '}';
    }
}
