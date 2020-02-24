package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FormVideo implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FormVideo createFromParcel(Parcel in) {
            return new FormVideo(in);
        }

        public FormVideo[] newArray(int size) {
            return new FormVideo[size];
        }
    };
    public String mUUID;
    public String mDatatype;
    public long mTimeStamp;
    public String mLocalPath;
    public String mAppId;
    public String mUserId;
    public String mProjectId;

    public FormVideo() {
    }

    public FormVideo(String uuid, String key, String datatype,
                     long timestamp, String localPath, String appId, String userId
            , String projectId) {
        this.mUUID = uuid;
        this.mDatatype = datatype;
        this.mTimeStamp = timestamp;
        this.mLocalPath = localPath;
        this.mAppId = appId;
        this.mUserId = userId;
        this.mProjectId = projectId;
    }

    // Parcelling part
    public FormVideo(Parcel in) {
        this.mUUID = in.readString();
        this.mDatatype = in.readString();
        this.mTimeStamp = in.readLong();
        this.mLocalPath = in.readString();
        this.mAppId = in.readString();
        this.mUserId = in.readString();
        this.mProjectId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUUID);
        dest.writeString(this.mDatatype);
        dest.writeLong(this.mTimeStamp);
        dest.writeString(this.mLocalPath);
        dest.writeString(this.mAppId);
        dest.writeString(this.mUserId);
        dest.writeString(this.mProjectId);
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String uuid) {
        this.mUUID = uuid;
    }

    public String getDatatype() {
        return mDatatype;
    }

    public void setDatatype(String datatype) {
        this.mDatatype = datatype;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public void setLocalPath(String localPath) {
        this.mLocalPath = localPath;
    }

    public String getAppId() {
        return mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getProjectId() {
        return mProjectId;
    }

    public void setProjectId(String projectId) {
        this.mProjectId = projectId;
    }

    @Override
    public String toString() {
        return "FormVideo{" +
                "mUUID='" + mUUID + '\'' +
                ", mDatatype='" + mDatatype + '\'' +
                ", mTimeStamp=" + mTimeStamp +
                ", mLocalPath='" + mLocalPath + '\'' +
                ", mAppId='" + mAppId + '\'' +
                ", mUserId='" + mUserId + '\'' +
                ", mProjectId='" + mProjectId + '\'' +
                '}';
    }
}
