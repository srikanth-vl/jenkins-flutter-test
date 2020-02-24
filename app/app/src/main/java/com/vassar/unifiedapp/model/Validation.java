package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Validation implements Parcelable {
    @JsonProperty("mandatory")
    public boolean mMandatory;
    @JsonProperty("expr")
    public ArrayList<ValidationExpression> mExpr;
    @JsonProperty("api")
    public ArrayList<APIValidation> mApi;
    @JsonProperty("display")
    public Boolean mDisplay;

    public Validation() {
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Validation createFromParcel(Parcel in) {
            return new Validation(in);
        }

        public Validation[] newArray(int size) {
            return new Validation[size];
        }
    };

    // Parcelling part
    public Validation(Parcel in){
        this.mMandatory = in.readByte() != 0;
        in.readTypedList(this.mExpr, ValidationExpression.CREATOR);
        in.readTypedList(this.mApi, APIValidation.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (this.mMandatory ? 1 : 0));
        dest.writeTypedList(this.mExpr);
        dest.writeTypedList(this.mApi);
    }
}
