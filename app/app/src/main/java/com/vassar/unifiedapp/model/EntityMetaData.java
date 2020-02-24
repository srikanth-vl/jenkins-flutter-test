package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityMetaData implements Parcelable {

    public static final Creator<EntityMetaData> CREATOR = new Creator<EntityMetaData>() {
        @Override
        public EntityMetaData createFromParcel(Parcel in) {
            return new EntityMetaData(in);
        }

        @Override
        public EntityMetaData[] newArray(int size) {
            return new EntityMetaData[size];
        }
    };
    @JsonProperty("super_app_id")
    private String superAppId;
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("parent_entity")
    private String parentName;
    @JsonProperty("entity_name")
    private String name;
    @JsonProperty("elements")
    private String entityList;
    @JsonProperty("insert_ts")
    private long timeStamp;
public EntityMetaData() {}
    protected EntityMetaData(Parcel in) {
        superAppId = in.readString();
        appId = in.readString();
        projectId = in.readString();
        userId = in.readString();
        parentName = in.readString();
        name = in.readString();
        timeStamp = in.readLong();
        entityList = in.readString();
    }

    public String getSuperAppId() {
        return superAppId;
    }

    public void setSuperAppId(String superAppId) {
        this.superAppId = superAppId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityList() {
        return entityList;
    }

    public void setEntityList(String entityList) {
        this.entityList = entityList;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(superAppId);
        dest.writeString(appId);
        dest.writeString(projectId);
        dest.writeString(userId);
        dest.writeString(parentName);
        dest.writeString(name);
        dest.writeLong(timeStamp);
        dest.writeString(entityList);
    }
}
