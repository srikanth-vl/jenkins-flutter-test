package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.vassar.unifiedapp.utils.MediaSubType;
import com.vassar.unifiedapp.utils.MediaType;

import java.util.HashMap;
import java.util.Map;

public class FormMedia implements Parcelable {

    public static final Creator<FormMedia> CREATOR = new Creator<FormMedia>() {
        @Override
        public FormMedia createFromParcel(Parcel in) {
            return new FormMedia(in);
        }

        @Override
        public FormMedia[] newArray(int size) {
            return new FormMedia[size];
        }
    };
    private String mUUID;
    private byte[] mBitmap;
    private String mLocalPath;
    private String mAppId;
    private String mUserId;
    private String mProjectId;
    private boolean mHasGeotag;
    private double latitude;
    private double longitude;
    private double accuracy;
    private long mediaClickTimeStamp;
    private String mediaFileExtension;
    private int mediaActionType;
    private int mediaType;
    private int mediaSubType;
    private int mediaRequestStatus;
    private int mediaUploadRetries;
    private long mediaUploadTimestamp;
    private long formSubmissionTimestamp;
    private Map<String, String> additionalProps;

    public FormMedia() {
        this.additionalProps = new HashMap<>();
    }

    public FormMedia(String mUUID, byte[] mBitmap, String mLocalPath, String mAppId, String mUserId,
                     String mProjectId, boolean mHasGeotag, double latitude, double longitude, double accuracy,
                     long media_click_timeStamp, String media_file_extension, int mediaActionType,
                     int media_type, int media_sub_type, int media_request_status, int media_upload_retries,
                     long media_upload_timestamp, long form_submission_timestamp, Map<String, String> additionalProps) {
        this.mUUID = mUUID;
        this.mBitmap = mBitmap;
        this.mLocalPath = mLocalPath;
        this.mAppId = mAppId;
        this.mUserId = mUserId;
        this.mProjectId = mProjectId;
        this.mHasGeotag = mHasGeotag;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.mediaClickTimeStamp = media_click_timeStamp;
        this.mediaFileExtension = media_file_extension;
        this.mediaActionType = mediaActionType;
        this.mediaType = media_type;
        this.mediaSubType = media_sub_type;
        this.mediaRequestStatus = media_request_status;
        this.mediaUploadRetries = media_upload_retries;
        this.mediaUploadTimestamp = media_upload_timestamp;
        this.formSubmissionTimestamp = form_submission_timestamp;
        this.additionalProps = additionalProps;
    }

    protected FormMedia(Parcel in) {
        mUUID = in.readString();
        mBitmap = in.createByteArray();
        mLocalPath = in.readString();
        mAppId = in.readString();
        mUserId = in.readString();
        mProjectId = in.readString();
        mHasGeotag = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
        accuracy = in.readDouble();
        mediaClickTimeStamp = in.readLong();
        mediaFileExtension = in.readString();
        mediaActionType = in.readInt();
        mediaType = in.readInt();
        mediaSubType = in.readInt();
        mediaRequestStatus = in.readInt();
        mediaUploadRetries = in.readInt();
        mediaUploadTimestamp = in.readLong();
        formSubmissionTimestamp = in.readLong();
        int size = in.readInt();
        this.additionalProps = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            this.additionalProps.put(key, value);
        }
    }

    public int getMediaActionType() {
        return mediaActionType;
    }

    public void setMediaActionType(int mediaActionType) {
        this.mediaActionType = mediaActionType;
    }

    public Map<String, String> getAdditionalProps() {
        return additionalProps;
    }

    public void setAdditionalProps(Map<String, String> additionalProps) {
        this.additionalProps = additionalProps;
    }

    public String getmUUID() {
        return mUUID;
    }

    public void setmUUID(String mUUID) {
        this.mUUID = mUUID;
    }

    public byte[] getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(byte[] mBitmap) {
        this.mBitmap = mBitmap;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public void setmLocalPath(String mLocalPath) {
        this.mLocalPath = mLocalPath;
    }

    public String getmAppId() {
        return mAppId;
    }

    public void setmAppId(String mAppId) {
        this.mAppId = mAppId;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmProjectId() {
        return mProjectId;
    }

    public void setmProjectId(String mProjectId) {
        this.mProjectId = mProjectId;
    }

    public boolean ismHasGeotag() {
        return mHasGeotag;
    }

    public void setmHasGeotag(boolean mHasGeotag) {
        this.mHasGeotag = mHasGeotag;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getMediaClickTimeStamp() {
        return mediaClickTimeStamp;
    }

    public void setMediaClickTimeStamp(long mediaClickTimeStamp) {
        this.mediaClickTimeStamp = mediaClickTimeStamp;
    }

    public String getMediaFileExtension() {
        return mediaFileExtension;
    }

    public void setMediaFileExtension(String mediaFileExtension) {
        this.mediaFileExtension = mediaFileExtension;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int media_type) {
        this.mediaType = media_type;
    }

    public int getMediaSubType() {
        return mediaSubType;
    }

    public void setMediaSubType(int media_sub_type) {
        this.mediaSubType = media_sub_type;
    }

    public int getMediaRequestStatus() {
        return mediaRequestStatus;
    }

    public void setMediaRequestStatus(int media_request_status) {
        this.mediaRequestStatus = media_request_status;
    }

    public int getMediaUploadRetries() {
        return mediaUploadRetries;
    }

    public void setMediaUploadRetries(int media_upload_retries) {
        this.mediaUploadRetries = media_upload_retries;
    }

    public long getMediaUploadTimestamp() {
        return mediaUploadTimestamp;
    }

    public void setMediaUploadTimestamp(long media_upload_timestamp) {
        this.mediaUploadTimestamp = media_upload_timestamp;
    }

    public long getFormSubmissionTimestamp() {
        return formSubmissionTimestamp;
    }

    public void setFormSubmissionTimestamp(long form_submission_timestamp) {
        this.formSubmissionTimestamp = form_submission_timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUUID);
        dest.writeByteArray(mBitmap);
        dest.writeString(mLocalPath);
        dest.writeString(mAppId);
        dest.writeString(mUserId);
        dest.writeString(mProjectId);
        dest.writeByte((byte) (mHasGeotag ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(accuracy);
        dest.writeLong(mediaClickTimeStamp);
        dest.writeString(mediaFileExtension);
        dest.writeInt(mediaActionType);
        dest.writeInt(mediaType);
        dest.writeInt(mediaSubType);
        dest.writeInt(mediaRequestStatus);
        dest.writeInt(mediaUploadRetries);
        dest.writeLong(mediaUploadTimestamp);
        dest.writeLong(formSubmissionTimestamp);
        if (additionalProps == null || additionalProps.isEmpty()) {
            dest.writeInt(0);
        } else {
            dest.writeInt(additionalProps.size());
            for (Map.Entry<String, String> entry : additionalProps.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
    }

    public String getMediaTypeName(int type) {
        String mediaType = "";

        if (type == MediaType.IMAGE.getValue()) {
            mediaType = "image";
        } else if (type == MediaType.VIDEO.getValue()) {
            mediaType = "video";
        } else if (type == MediaType.AUDIO.getValue())
            mediaType = "audio";

        else if (type == MediaType.BLOB.getValue())
            mediaType = "blob";

        else if (type == MediaType.TEXT.getValue())
            mediaType = "text";

        else if (type == MediaType.PDF.getValue())
            mediaType = "pdf";

        else if (type == MediaType.OTHERS.getValue())
            mediaType = "others";

        return mediaType;
    }

    public String getMediaSubTypeName(int mediaSubType) {
        String mediaSubTypeName = "";

        if (mediaSubType == MediaSubType.FULL.getValue())
            mediaSubTypeName = "full";

        else if (mediaSubType == MediaSubType.PREVIEW.getValue())
            mediaSubTypeName = "preview";

        else if (mediaSubType == MediaSubType.THUMBNAIL.getValue())
            mediaSubTypeName = "thumbnail";

        return mediaSubTypeName;
    }
}
