package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfflineMapFile implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public OfflineMapFile createFromParcel(Parcel in) {
            return new OfflineMapFile(in);
        }

        public OfflineMapFile[] newArray(int size) {
            return new OfflineMapFile[size];
        }
    };
    @JsonProperty("file_name")
    public String fileName;
    @JsonProperty("file_url")
    public String fileUrl;
    @JsonProperty("file_storage_path")
    public String fileStoragePath;
    @JsonProperty("file_size")
    public String fileSize;
    @JsonProperty("file_additional_info")
    public Map<String, String> fileAdditionalInfo;

    public OfflineMapFile() {
    }

    public OfflineMapFile(String fileName, String fileUrl, String fileStoragePath, String fileSize, Map<String, String> additionalInfo) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileStoragePath = fileStoragePath;
        this.fileSize = fileSize;
        this.fileAdditionalInfo = additionalInfo;
    }

    // Parcelling part
    public OfflineMapFile(Parcel in) {
        this.fileName = in.readString();
        this.fileUrl = in.readString();
        this.fileStoragePath = in.readString();
        this.fileSize = in.readString();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            fileAdditionalInfo.put(key, value);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileStoragePath() {
        return fileStoragePath;
    }

    public void setFileStoragePath(String fileStoragePath) {
        this.fileStoragePath = fileStoragePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public Map<String, String> getFileAdditionalInfo() {
        return fileAdditionalInfo;
    }

    public void setFileAdditionalInfo(Map<String, String> fileAdditionalInfo) {
        this.fileAdditionalInfo = fileAdditionalInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileName);
        dest.writeString(this.fileUrl);
        dest.writeString(this.fileStoragePath);
        dest.writeString(this.fileSize);
        if (fileAdditionalInfo != null) {
            dest.writeInt(fileAdditionalInfo.size());
            for (Map.Entry<String, String> entry : fileAdditionalInfo.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
    }
}

