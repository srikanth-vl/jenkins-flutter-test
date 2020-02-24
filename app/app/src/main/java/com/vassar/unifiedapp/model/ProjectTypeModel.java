package com.vassar.unifiedapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectTypeModel implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ProjectTypeModel createFromParcel(Parcel in) {
            return new ProjectTypeModel(in);
        }

        public ProjectTypeModel[] newArray(int size) {
            return new ProjectTypeModel[size];
        }
    };
    @JsonProperty("parent_app_id")
    public String mParentAppId;
    @JsonProperty("name")
    public String mName;
    @JsonProperty("icon")
    public String mIcon;
    @JsonProperty("app_id")
    public String mAppId;
    @JsonProperty("client_expiry_interval")
    public String mClientExpiryDays;
    @JsonProperty("alert_interval")
    public String mExpiryAlertThreshold;
    @JsonProperty("sort_type")
    public String mSortType;
    @JsonProperty("order")
    public Integer mOrder;
    @JsonProperty("attributes")
    public List<String> mFilteringAttributes;
    @JsonProperty("grouping_attributes")
    public List<String> mGroupingAttributes;
    @JsonProperty("display_project_icon")
    public Boolean mDisplayProjectIcon;
    @JsonProperty("filtering_form")
    public ActionForms mFilteringForm;
    @JsonProperty("map_configuration")
    public MapConfiguration mMapConfiguration;
    @JsonProperty("group_entities")
    public List<Map<String, String>> mGroupEntitiesList;
    @JsonProperty("external_api_list")
    public Map<String, Object> externalApiList;
    @JsonProperty("desc")
    public String mDesc;
    @JsonProperty("formatter")
    Map<String, String> mFormatter;
    @JsonProperty("dashboard_analytics_attribute_hierarchy")
    List<String> dashboardAnalyticsAttrbutes;
    @JsonProperty("proj_icon_config")
    public ProjectIconInfo mProjectIconInfo = null;
    @JsonProperty("status")
    public String mStatus;
    public ProjectTypeModel() {
    }

    public ProjectTypeModel(String parentAppId, String name, String icon, String appId,
                            String clientExpiryInterval, String expiryAlertThreshold) {
        this.mParentAppId = parentAppId;
        this.mName = name;
        this.mIcon = icon;
        this.mAppId = appId;
        this.mClientExpiryDays = clientExpiryInterval;
        this.mExpiryAlertThreshold = expiryAlertThreshold;
    }

    // Parcelling part
    public ProjectTypeModel(Parcel in) {
        this.mParentAppId = in.readString();
        this.mName = in.readString();
        this.mIcon = in.readString();
        this.mAppId = in.readString();
        this.mClientExpiryDays = in.readString();
        this.mExpiryAlertThreshold = in.readString();
        this.mSortType = in.readString();
        this.mOrder = in.readInt();
        if (this.mFilteringAttributes != null && this.mFilteringAttributes.size() > 0) {
            in.readStringList(this.mFilteringAttributes);
        } else {
            in.readStringList(new ArrayList<>());
        }
        this.mDisplayProjectIcon = in.readInt() != 0;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Boolean getDisplayProjectIcon() {
        return mDisplayProjectIcon;
    }

    public void setDisplayProjectIcon(Boolean displayProjectIcon) {
        this.mDisplayProjectIcon = displayProjectIcon;
    }

    public Integer getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        this.mOrder = order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mParentAppId);
        dest.writeString(this.mName);
        dest.writeString(this.mIcon);
        dest.writeString(this.mAppId);
        dest.writeString(this.mClientExpiryDays);
        dest.writeString(this.mExpiryAlertThreshold);
        dest.writeString(this.mSortType);
        if (this.mOrder == null)
            dest.writeInt(0);
        else
            dest.writeInt(this.mOrder);
        dest.writeStringList(this.mFilteringAttributes);
        dest.writeInt(this.mDisplayProjectIcon == null
                || !this.mDisplayProjectIcon ? 0 : 1);
    }
}
