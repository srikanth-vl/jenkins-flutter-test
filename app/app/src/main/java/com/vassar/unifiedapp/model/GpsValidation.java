package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsValidation implements Parcelable {

    public static final Creator<GpsValidation> CREATOR = new Creator<GpsValidation>() {
        @Override
        public GpsValidation createFromParcel(Parcel in) {
            return new GpsValidation(in);
        }

        @Override
        public GpsValidation[] newArray(int size) {
            return new GpsValidation[size];
        }
    };
    // bbox, circular
    @JsonProperty("type")
    private String type;
    // project, key
    @JsonProperty("source")
    private String source;
    @JsonProperty("key")
    private String key;
    @JsonProperty("radius")
    private double radius;
    @JsonProperty("key_type")
    private String keyType;

    public GpsValidation() {
    }

    private GpsValidation(Parcel in) {
        this.type = in.readString();
        this.source = in.readString();
        this.key = in.readString();
        this.radius = in.readDouble();
        this.keyType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.source);
        dest.writeString(this.key);
        dest.writeDouble(this.radius);
        dest.writeString(this.keyType);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    @Override
    public String toString() {
        return "GpsValidation{" +
                "type='" + type + '\'' +
                ", source='" + source + '\'' +
                ", key='" + key + '\'' +
                ", radius=" + radius +
                ", keyType='" + keyType + '\'' +
                '}';
    }
}
