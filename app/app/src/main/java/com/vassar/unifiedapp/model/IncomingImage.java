package com.vassar.unifiedapp.model;

public class IncomingImage {

    private String mImageType;
    private String mImageLocalPath;
    private String mImageUrl;

    public IncomingImage(String imageType, String imageLocalPath, String imageUrl) {
        this.mImageType = imageType;
        this.mImageLocalPath = imageLocalPath;
        this.mImageUrl = imageUrl;
    }

    public String getImageType() {
        return mImageType;
    }

    public void setImageType(String imageType) {
        this.mImageType = imageType;
    }

    public String getImageLocalPath() {
        return mImageLocalPath;
    }

    public void setImageLocalPath(String imageLocalPath) {
        this.mImageLocalPath = imageLocalPath;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "IncomingImage{" +
                "mImageType='" + mImageType + '\'' +
                ", mImageLocalPath='" + mImageLocalPath + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                '}';
    }
}
