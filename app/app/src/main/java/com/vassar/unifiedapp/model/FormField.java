package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormField {
    @JsonProperty("key")
    public String mIdentifier;
    @JsonProperty("label")
    public String mLabel;
    @JsonProperty("uitype")
    public String mUiType;
    @JsonProperty("datatype")
    public String mDatatype;
    @JsonProperty("editable")
    public boolean mEditable;
    @JsonProperty("display")
    public boolean mDisplay;
    @JsonProperty("selectablewindow")
    public SelectableDates mSelectableDates;
    @JsonProperty("validations")
    public Validation mValidation;
    @JsonProperty("multiplevalues")
    public ArrayList<MultipleValues> mMultipleValues;
    @JsonProperty("expandable")
    public ExpandableComponent mExpandable;
    @JsonProperty("default")
    public String mDefault;
    @JsonProperty("uom")
    public String mUOM;
    @JsonProperty("icon")
    public String mIcon;
    @JsonProperty("max_chars")
    public int mMaxChars;
    @JsonProperty("max")
    public int mMax;
    @JsonProperty("aligned")
    public String mAligned;
    @JsonProperty("entity")
    public String mEntity;
    @JsonProperty("source")
    public String mSource;
    @JsonProperty("parent")
    public String mParent;
    @JsonProperty("selection")
    public String mSelection;
    @JsonProperty("tablestructure")
    public List<TableRow> mTableStructure;
    @JsonProperty("weight")
    public float mWeight;
    @JsonProperty("label_style")
    public String mLabelStyle;
    @JsonProperty("foreground")
    public String mForeGroundColor;
    @JsonProperty("background")
    public String mBackgroundColor;
    @JsonProperty("filtering_param")
    public boolean mFilteringParam;
    @JsonProperty("filtering_key")
    public String mFilteringKey;
    @JsonProperty("computation_expr")
    public String mComputationExpression;
    @JsonProperty("label_color")
    public String mLabelColor;
    @JsonProperty("auto_populate")
    public AutoPopulateConfig mAutoPopulateConfig;
    @JsonProperty("gps_validation")
    public GpsValidation mGpsValidation;
    @JsonProperty("map_field_info")
    public MapFieldInfo mMapFieldInfo;

    @Override
    public String toString() {
        return "FormField{" +
                "mIdentifier='" + mIdentifier + '\'' +
                ", mLabel='" + mLabel + '\'' +
                ", mUiType='" + mUiType + '\'' +
                ", mDatatype='" + mDatatype + '\'' +
                ", mEditable=" + mEditable +
                ", mDisplay=" + mDisplay +
                ", mSelectableDates=" + mSelectableDates +
                ", mValidation=" + mValidation +
                ", mMultipleValues=" + mMultipleValues +
                ", mExpandable=" + mExpandable +
                ", mDefault='" + mDefault + '\'' +
                ", mUOM='" + mUOM + '\'' +
                ", mIcon='" + mIcon + '\'' +
                ", mMaxChars=" + mMaxChars +
                ", mMax=" + mMax +
                ", mAligned='" + mAligned + '\'' +
                ", mEntity='" + mEntity + '\'' +
                ", mSource='" + mSource + '\'' +
                ", mParent='" + mParent + '\'' +
                ", mSelection='" + mSelection + '\'' +
                ", mTableStructure=" + mTableStructure +
                ", mWeight=" + mWeight +
                ", mLabelStyle='" + mLabelStyle + '\'' +
                ", mForeGroundColor='" + mForeGroundColor + '\'' +
                ", mBackgroundColor='" + mBackgroundColor + '\'' +
                ", mFilteringParam=" + mFilteringParam +
                ", mFilteringKey='" + mFilteringKey + '\'' +
                ", mComputationExpression='" + mComputationExpression + '\'' +
                ", mLabelColor='" + mLabelColor + '\'' +
                ", mAutoPopulateConfig=" + mAutoPopulateConfig +
                ", mGpsValidation=" + mGpsValidation +
                ", mMapFieldInfo=" + mMapFieldInfo +
                '}';
    }
}
