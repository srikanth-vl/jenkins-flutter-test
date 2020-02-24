package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class FormImage implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FormImage createFromParcel(Parcel in) {
            return new FormImage(in);
        }

        public FormImage[] newArray(int size) {
            return new FormImage[size];
        }
    };
    public String mUUID;
    public String mDatatype;
    public byte[] mBitmap;
    public long mTimeStamp;
    public String mLocalPath;
    public String mAppId;
    public String mUserId;
    public String mProjectId;
    public boolean mHasGeotag;
    public String mGeotag;

    public FormImage() {
    }

    public FormImage(String uuid, String key, String datatype, byte[] bitmap
            , long timestamp, String localPath, String appId, String userId
            , String projectId, boolean hasGeotag, String geotag) {
        this.mUUID = uuid;
        this.mDatatype = datatype;
        this.mBitmap = bitmap;
        this.mTimeStamp = timestamp;
        this.mLocalPath = localPath;
        this.mAppId = appId;
        this.mUserId = userId;
        this.mProjectId = projectId;
        this.mHasGeotag = hasGeotag;
        this.mGeotag = geotag;
    }

    // Parcelling part
    public FormImage(Parcel in) {
        this.mUUID = in.readString();
        this.mDatatype = in.readString();
        this.mBitmap = new byte[in.readInt()];
        in.readByteArray(this.mBitmap);
        this.mTimeStamp = in.readLong();
        this.mLocalPath = in.readString();
        this.mAppId = in.readString();
        this.mUserId = in.readString();
        this.mProjectId = in.readString();
        this.mHasGeotag = in.readByte() != 0;
        this.mGeotag = in.readString();
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

    public byte[] getBitmap() {
        return mBitmap;
    }

    public void setBitmap(byte[] bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUUID);
        dest.writeString(this.mDatatype);
        dest.writeInt(mBitmap.length);
        dest.writeByteArray(mBitmap);
        dest.writeLong(this.mTimeStamp);
        dest.writeString(this.mLocalPath);
        dest.writeString(this.mAppId);
        dest.writeString(this.mUserId);
        dest.writeString(this.mProjectId);
        dest.writeByte((byte) (this.mHasGeotag ? 1 : 0));
        dest.writeString(this.mGeotag);
    }

    @Override
    public String toString() {
        return "FormImage{" +
                "mUUID='" + mUUID + '\'' +
                ", mDatatype='" + mDatatype + '\'' +
                ", mBitmap=" + Arrays.toString(mBitmap) +
                ", mTimeStamp=" + mTimeStamp +
                ", mLocalPath='" + mLocalPath + '\'' +
                ", mAppId='" + mAppId + '\'' +
                ", mUserId='" + mUserId + '\'' +
                ", mProjectId='" + mProjectId + '\'' +
                ", mHasGeotag=" + mHasGeotag +
                ", mGeotag='" + mGeotag + '\'' +
                '}';
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

    public boolean hasGeotag() {
        return mHasGeotag;
    }

    public void setHasGeotag(boolean hasGeotag) {
        this.mHasGeotag = hasGeotag;
    }

    public String getGeotag() {
        return mGeotag;
    }

    public void setGeotag(String geotag) {
        this.mGeotag = geotag;
    }
}
