package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
    @JsonProperty("projectid")
    public String mProjectId;
    @JsonProperty("lat")
    public String mLatitude;
    @JsonProperty("long")
    public String mLongitude;
    @JsonProperty("projectname")
    public String mProjectName;
    @JsonProperty("project_icon")
    public ProjectIconInfo mProjectIcon;
    @JsonProperty("last_sync_ts")
    public long mLastSyncTimestamp;
    @JsonProperty("last_sub_date")
    public String mLastSubDate;
    @JsonProperty("state")
    public String mState;
    @JsonProperty("ext_proj_id")
    public String mExtProjectId;
    @JsonProperty("fields")
    public ArrayList<ProjectListFieldModel> mFields;
    @JsonProperty("user_type")
    public String mUserType;
    @JsonProperty("validations")
    public Validation mValidation;
    @JsonProperty("bbox")
    public String mBBoxValidation;
    @JsonProperty("circle_validation")
    public String mCentroidValidation;
    @JsonProperty("attributes")
    public Map<String, String> mAttributes;
    public boolean mExpired = false;
    public boolean mAssigned = false;
    @JsonProperty("priority")
    public String mPriority;

    public Project() {
    }

    // Parcelling part
    public Project(Parcel in) {
        this.mProjectId = in.readString();
        this.mLatitude = in.readString();
        this.mLongitude = in.readString();
        this.mProjectName = in.readString();
        this.mProjectIcon = in.readParcelable(ProjectIconInfo.class.getClassLoader());
        this.mLastSyncTimestamp = in.readLong();
        this.mLastSubDate = in.readString();
        this.mState = in.readString();
        this.mExtProjectId = in.readString();
        in.readTypedList(this.mFields, ProjectListFieldModel.CREATOR);
        this.mUserType = in.readString();
        this.mValidation = in.readParcelable(Validation.class.getClassLoader());
        this.mExpired = in.readByte() != 0;
        this.mBBoxValidation = in.readString();
        this.mCentroidValidation = in.readString();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            mAttributes.put(key, value);
        }
    }

    public boolean isAssigned() {
        return mAssigned;
    }

    public void setAssigned(boolean assigned) {
        this.mAssigned = assigned;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mProjectId);
        dest.writeString(this.mLatitude);
        dest.writeString(this.mLongitude);
        dest.writeString(this.mProjectName);
        dest.writeString(this.mBBoxValidation);
        dest.writeString(this.mCentroidValidation);
        dest.writeLong(this.mLastSyncTimestamp);
        dest.writeString(this.mLastSubDate);
        dest.writeString(this.mState);
        dest.writeString(this.mExtProjectId);
        dest.writeTypedList(this.mFields);
        dest.writeString(this.mUserType);
        dest.writeParcelable(this.mValidation, flags);
        dest.writeParcelable(this.mProjectIcon, flags);
        dest.writeByte((byte) (this.mExpired ? 1 : 0));
        if (mAttributes != null) {
            dest.writeInt(mAttributes.size());
            for (Map.Entry<String, String> entry : mAttributes.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
    }

    public String getProjectName() {
        return mProjectName;
    }

    public void setProjectName(String projectName) {
        this.mProjectName = projectName;
    }

    public Long getLastSyncTimestamp() {
        return mLastSyncTimestamp;
    }

    public void setLastSyncTimestamp(long lastSyncTimestamp) {
        this.mLastSyncTimestamp = lastSyncTimestamp;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        this.mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        this.mLongitude = longitude;
    }

    public String getProjectId() {
        return mProjectId;
    }

    public void setProjectId(String projectId) {
        this.mProjectId = projectId;
    }

    public String getLastSubDate() {
        return mLastSubDate;
    }

    public void setLastSubDate(String lastSubDate) {
        this.mLastSubDate = lastSubDate;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public String getExtProjectId() {
        return mExtProjectId;
    }

    public void setExtProjectId(String extProjectId) {
        this.mExtProjectId = extProjectId;
    }

    public ArrayList<ProjectListFieldModel> getFields() {
        return mFields;
    }

    public void setFields(ArrayList<ProjectListFieldModel> fields) {
        this.mFields = fields;
    }

    public String getUserType() {
        return mUserType;
    }

    public void setUserType(String userType) {
        this.mUserType = userType;
    }

    public Validation getValidation() {
        return mValidation;
    }

    public void setValidation(Validation validation) {
        this.mValidation = validation;
    }

    public String getBBoxValidation() {
        return mBBoxValidation;
    }

    public void setBBoxValidation(String bBoxValidation) {
        this.mBBoxValidation = bBoxValidation;
    }

    public String getCentroidValidation() {
        return mCentroidValidation;
    }

    public void setCentroidValidation(String centroidValidation) {
        this.mCentroidValidation = centroidValidation;
    }

    public Map<String, String> getAttributes() {
        return mAttributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.mAttributes = attributes;
    }

    public boolean isExpired() {
        return mExpired;
    }

    public void setExpired(boolean expired) {
        this.mExpired = expired;
    }

    public ProjectIconInfo getProjectIcon() {
        return mProjectIcon;
    }

    public void setProjectIcon(ProjectIconInfo mProjectIcon) {
        this.mProjectIcon = mProjectIcon;
    }

    @Override
    public String toString() {
        return "Project{" +
                "mProjectId='" + mProjectId + '\'' +
                ", mLatitude='" + mLatitude + '\'' +
                ", mLongitude='" + mLongitude + '\'' +
                ", mProjectName='" + mProjectName + '\'' +
                ", mLastSyncTimestamp=" + mLastSyncTimestamp +
                ", mLastSubDate='" + mLastSubDate + '\'' +
                ", mState='" + mState + '\'' +
                ", mExtProjectId='" + mExtProjectId + '\'' +
                ", mFields=" + mFields +
                ", mUserType='" + mUserType + '\'' +
                ", mValidation=" + mValidation +
                ", mBBoxValidation='" + mBBoxValidation + '\'' +
                ", mCentroidValidation='" + mCentroidValidation + '\'' +
                ", mFilteringAttributes=" + mAttributes +
                ", mExpired=" + mExpired +
                ", mAssigned=" + mAssigned +
                '}';
    }
}
