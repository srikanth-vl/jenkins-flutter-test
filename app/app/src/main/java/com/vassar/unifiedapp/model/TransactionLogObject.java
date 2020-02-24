package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionLogObject {
    private String projectName;
    private String submissionTimestamp;
    private Map<String, String> fields;
    private String response;
    private Map<Integer, List<String>> mediaTypeToUUIDs;

    public TransactionLogObject() {
        this.mediaTypeToUUIDs = new HashMap<>();
        this.fields = new HashMap<>();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(String submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Map<Integer, List<String>> getMediaUUIDs() {
        return mediaTypeToUUIDs;
    }

    public void setMediaUUIDs(Map<Integer, List<String>> mediaUUIDs) {
        this.mediaTypeToUUIDs = mediaUUIDs;
    }
}
