package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Form {
    @JsonProperty("formid")
    public String mFormId;
    @JsonProperty("name")
    public String mName;
    @JsonProperty("title")
    public String mTitle;
    @JsonProperty("subtitle")
    public String mSubtitle;
    @JsonProperty("header")
    public ArrayList<Header> mHeaders;
    @JsonProperty("fields")
    public ArrayList<FormField> mFormFields;
    @JsonProperty("buttons")
    public ArrayList<FormButton> mFormButtons;
    @JsonProperty("form_bridge")
    public FormBridge mFormBridge;
    @JsonProperty("mandatory_fields")
    public ArrayList<String> mMandatoryFields;


//    @Override
//    public String toString() {
//        return "Form {" +
//                "formid='" + mFormId + '\'' +
//                ", name=" + mName +
//                ", title=" + mTitle +
//                ", subtitle=" + mSubtitle +
//                ", header=" + mHeaders.toString() +
//                ", fields=" + mFormFields.toString() +
//                '}';
//    }
}
