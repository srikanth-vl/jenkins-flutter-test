package com.vassar.unifiedapp.model;

public class ProjectTypeConfigurationFetchData {

    public String mProjectTypeId;
    public int mFormVersion;

    public ProjectTypeConfigurationFetchData(String projectTypeId, int formVersion) {
        this.mProjectTypeId = projectTypeId;
        this.mFormVersion = formVersion;
    }
}
