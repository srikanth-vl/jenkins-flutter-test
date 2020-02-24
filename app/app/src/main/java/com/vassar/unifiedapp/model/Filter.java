package com.vassar.unifiedapp.model;

public class Filter {

    public String mAppId;
    public String mUserId;
    public String mDimension;
    public String mDimensionName;
    public String mProjects;

    public Filter(String appId, String userId, String dimension,
                  String dimensionName, String projects) {
        this.mAppId = appId;
        this.mUserId = userId;
        this.mDimension = dimension;
        this.mDimensionName = dimensionName;
        this.mProjects = projects;
    }
}
