package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationExpression implements Parcelable {
    @JsonProperty("expr")
    public String mExpression;
    @JsonProperty("error_msg")
    public String mErrorMessage;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ValidationExpression createFromParcel(Parcel in) {
            return new ValidationExpression(in);
        }

        public ValidationExpression[] newArray(int size) {
            return new ValidationExpression[size];
        }
    };

    public ValidationExpression() {
    }

    // Parcelling part
    public ValidationExpression(Parcel in){
        this.mExpression = in.readString();
        this.mErrorMessage =  in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mExpression);
        dest.writeString(this.mErrorMessage);
    }
}
